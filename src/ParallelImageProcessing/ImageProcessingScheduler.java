package ParallelImageProcessing;

import ImageProcessingAlgorithms.*;
import PngOutput.OutputWriter;
import gui.AlgoStatusCard;

import java.io.File;
import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.*;

public class ImageProcessingScheduler implements Runnable{
    public final Deque<ImageProcessingContext> imagesToLoad;
    private final ThreadPoolExecutor threadPool;
    private final Thread outputWriterThread;
    private final BlockingQueue tilesSharedQueue;
    private final OutputWriter outputWriter;

    private long startTime;
    private long endTime;

    private final int MAX_THREADS;
    private final int MAX_LOADED_TILES;
    private final int SLEEP_MILLIS = 1000;
    private final int OVERALL_TIMEOUT_MINUTES;
    public  final AlgorithmParameters algorithmParameters;
    public final String algorithmID;
    public final AlgoStatusCard algoStatusCard;

    public ImageProcessingScheduler(Deque<ImageProcessingContext> imagesToLoad, int maxThreads, int maxLoaded, int timeoutForAllTasksMinutes, File outputDirectory, AlgorithmParameters algorithmParameters, String algorithmID, AlgoStatusCard algoStatusCard){
        this.imagesToLoad = imagesToLoad;
        MAX_THREADS = maxThreads;
        MAX_LOADED_TILES = maxLoaded;
        OVERALL_TIMEOUT_MINUTES = timeoutForAllTasksMinutes;
        this.algorithmParameters = algorithmParameters;
        this.algorithmID = algorithmID;
        this.algoStatusCard = algoStatusCard;
        this.threadPool = new ThreadPoolExecutor(MAX_THREADS, MAX_THREADS, 1, TimeUnit.MINUTES, new LinkedBlockingDeque<>());
        this.tilesSharedQueue = new LinkedBlockingDeque();
        outputWriter = new OutputWriter(this.tilesSharedQueue, outputDirectory, algoStatusCard);
        outputWriterThread = new Thread(outputWriter);
    }
    public void start() {
        startTime = System.currentTimeMillis();
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
                freeResourcesOf(ipc);
            }

            if(loadedTilesNum() >= MAX_LOADED_TILES && !imagesToLoad.isEmpty()){
                try {
                    Thread.sleep(SLEEP_MILLIS);
                } catch (InterruptedException ignored) {
                    //continue loading
                }
            }
        }

        threadPool.shutdown();
        boolean terminated = false;
        do{
            try {
                terminated = threadPool.awaitTermination(OVERALL_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                if(!terminated){
                    throw new RuntimeException("Tasks did not finish in over " + OVERALL_TIMEOUT_MINUTES + " minutes");
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

        endTime = System.currentTimeMillis();
        long fullTime = endTime-startTime;
        algoStatusCard.logger.logGreen("Finished tasks");
        algoStatusCard.logger.log("Tasks took " + Math.round(fullTime/1000f) + " seconds to complete");
        algoStatusCard.allowNextTask();
    }

    private void freeResourcesOf(ImageProcessingContext ipc) {
        //TODO
        algoStatusCard.logger.logRed("RESOURCE FREEING NOT IMPLEMENTED YET");
        algoStatusCard.updateMainProgressBar();
    }

    private boolean loadTilesOfImage(ImageProcessingContext ipc) throws IOException {
        while(ipc.tilingContext.hasNextTile() && loadedTilesNum() < MAX_LOADED_TILES){
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

    private int loadedTilesNum() {
        return threadPool.getActiveCount()  + threadPool.getQueue().size();
    }

    @Override
    public void run() {
        this.start();
    }
}
