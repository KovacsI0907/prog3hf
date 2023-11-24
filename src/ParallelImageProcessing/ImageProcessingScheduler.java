package ParallelImageProcessing;

import ImageProcessingAlgorithms.MedianFilter;
import PngOutput.OutputWriter;

import java.io.IOException;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.*;

public class ImageProcessingScheduler {
    public final Deque<ImageProcessingContext> imagesToLoad;
    private final ThreadPoolExecutor threadPool;
    private final Thread outputWriterThread;
    private final BlockingQueue tilesSharedQueue;
    private final OutputWriter outputWriter;

    private final int MAX_THREADS;
    private final int MAX_LOADED_TILES;
    private final int SLEEP_MILLIS = 1000;
    private final int OVERALL_TIMEOUT_MINUTES;

    public ImageProcessingScheduler(Deque<ImageProcessingContext> imagesToLoad, int maxThreads, int maxLoaded, int timeoutForAllTasksMinutes){
        this.imagesToLoad = imagesToLoad;
        MAX_THREADS = maxThreads;
        MAX_LOADED_TILES = maxLoaded;
        OVERALL_TIMEOUT_MINUTES = timeoutForAllTasksMinutes;
        this.threadPool = new ThreadPoolExecutor(MAX_THREADS, MAX_THREADS, 1, TimeUnit.MINUTES, new LinkedBlockingDeque<>());
        this.tilesSharedQueue = new LinkedBlockingDeque();
        outputWriter = new OutputWriter(this.tilesSharedQueue);
        outputWriterThread = new Thread(outputWriter);
    }
    public void start() throws IOException {
        outputWriterThread.start();

        while(!imagesToLoad.isEmpty()){
            ImageProcessingContext ipc = imagesToLoad.poll();
            //if image not finished
            if(!loadTilesOfImage(ipc)){
                imagesToLoad.offerFirst(ipc);
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


        try {
            while (outputWriterThread.isAlive()) {
                outputWriterThread.join();
            }
        } catch (InterruptedException ignored) {
            //at this point there are no images left to load, so we continue waiting
        }
    }

    private boolean loadTilesOfImage(ImageProcessingContext ipc) throws IOException {
        while(ipc.tilingContext.hasNextTile() && loadedTilesNum() < MAX_LOADED_TILES){
            ProcessingTask pt = new ProcessingTask(ipc.tilingContext.getNextTile(),
                    new MedianFilter(MedianFilter.KERNEL_SIZE.THREE),
                    outputWriterThread,
                    Thread.currentThread(),
                    tilesSharedQueue);
            threadPool.submit(pt);
        }

        return !ipc.tilingContext.hasNextTile();
    }

    private int loadedTilesNum() {
        return threadPool.getActiveCount()  + threadPool.getQueue().size() + outputWriter.tilesInBufferNum();
    }
}
