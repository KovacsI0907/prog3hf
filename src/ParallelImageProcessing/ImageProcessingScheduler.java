package ParallelImageProcessing;

import ImageProcessingAlgorithms.*;
import PngOutput.OutputWriter;
import gui.AlgoStatusCard;

import java.io.File;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class ImageProcessingScheduler implements Runnable{
    public final Deque<ImageProcessingContext> imagesToLoad;
    private final ThreadPoolExecutor threadPool;
    private final Thread outputWriterThread;
    private final BlockingQueue<ImageTile> tilesSharedQueue;
    private final OutputWriter outputWriter;
    public final List<ImageProcessingContext> stoppedImageList = new LinkedList<>();
    private final int overallTimeoutMinutes;
    public  final AlgorithmParameters algorithmParameters;
    public final String algorithmID;
    public final AlgoStatusCard algoStatusCard;

    public ImageProcessingScheduler(Deque<ImageProcessingContext> imagesToLoad, int maxThreads, int timeoutForAllTasksMinutes, File outputDirectory, AlgorithmParameters algorithmParameters, String algorithmID, AlgoStatusCard algoStatusCard){
        this.imagesToLoad = imagesToLoad;
        overallTimeoutMinutes = timeoutForAllTasksMinutes;
        this.algorithmParameters = algorithmParameters;
        this.algorithmID = algorithmID;
        this.algoStatusCard = algoStatusCard;
        this.threadPool = new ThreadPoolExecutor(maxThreads, maxThreads, 1, TimeUnit.MINUTES, new LinkedBlockingDeque<>());
        this.tilesSharedQueue = new LinkedBlockingDeque<>();

        outputWriter = new OutputWriter(this.tilesSharedQueue, outputDirectory, algoStatusCard, stoppedImageList);
        outputWriterThread = new Thread(outputWriter);
    }
    public void start() {
        long startTime = System.currentTimeMillis();
        algoStatusCard.logger.log("Starting " + imagesToLoad.size() + " task(s)");
        outputWriterThread.start();

        while(!imagesToLoad.isEmpty()){
            ImageProcessingContext ipc = imagesToLoad.poll();
            //if image not finished
            try {
                if (!loadTilesOfImage(ipc)) {
                    imagesToLoad.offerFirst(ipc);
                }
            }catch (IOException e){
                algoStatusCard.logger.logRed("Error loading part of " + ipc.imageFile.getName());
                stopProcessingImage(ipc);
            }

            if(!ImageProcessingContext.canLoadNextTile() || !imagesToLoad.isEmpty()){
                try {
                    int sleepMillis = 1000;
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException ignored) {
                    //continue loading
                }
            }
        }

        threadPool.shutdown();
        boolean terminated = false;
        do{
            try {
                terminated = threadPool.awaitTermination(overallTimeoutMinutes, TimeUnit.MINUTES);
                if(!terminated){
                    throw new RuntimeException("Tasks did not finish in over " + overallTimeoutMinutes + " minutes");
                }
            } catch (InterruptedException ignored) {
                //at this point there are no images left to load, so we continue
            }
        }while(!terminated);

        outputWriter.setEndOfStream();
        if(outputWriterThread.getState() == Thread.State.TIMED_WAITING){
            outputWriterThread.interrupt();
        }

        while (outputWriterThread.isAlive()) {
            try {
                outputWriterThread.join();
            } catch (InterruptedException ignored) {
                //at this point there are no images left to load, so we continue waiting
            }
        }

        long endTime = System.currentTimeMillis();
        long fullTime = endTime - startTime;
        algoStatusCard.logger.logGreen("Finished tasks");
        algoStatusCard.logger.log("Tasks took " + Math.round(fullTime/1000f) + " seconds to complete");
        algoStatusCard.allowNextTask();
        algoStatusCard.isRunning = false;
    }

    private void stopProcessingImage(ImageProcessingContext ipc) {
        synchronized (stoppedImageList){
            stoppedImageList.add(ipc);
        }
        algoStatusCard.updateMainProgressBar();
    }

    private boolean loadTilesOfImage(ImageProcessingContext ipc) throws IOException {
        while(ipc.tilingContext.hasNextTile() && ImageProcessingContext.canLoadNextTile()){
            ImageTile tile = ipc.tilingContext.getNextTile();
            ProcessingTask pt = new ProcessingTask(tile,
                    AlgorithmFactory.getAlgorithm(algorithmID, algorithmParameters, tile),
                    outputWriterThread,
                    Thread.currentThread(),
                    tilesSharedQueue, algoStatusCard.logger);
            threadPool.submit(pt);
        }

        return !ipc.tilingContext.hasNextTile();
    }


    @Override
    public void run() {
        this.start();
    }
}
