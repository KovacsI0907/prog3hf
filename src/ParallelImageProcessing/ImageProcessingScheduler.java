package ParallelImageProcessing;

import ImageProcessingAlgorithms.MedianFilter;
import PngOutput.OutputWriter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.*;

public class ImageProcessingScheduler {
    public final Queue<ImageProcessingContext> imagesToLoad;
    private final ExecutorService executorService;
    private final Thread outputWriterThread;
    private final BlockingQueue processedTiles;
    private final OutputWriter outputWriter;

    public ImageProcessingScheduler(Queue<ImageProcessingContext> imagesToLoad){
        this.imagesToLoad = imagesToLoad;
        this.executorService = Executors.newFixedThreadPool(5);
        this.processedTiles = new LinkedBlockingDeque();
        outputWriter = new OutputWriter(this.processedTiles);
        outputWriterThread = new Thread(outputWriter);
    }
    public void start() throws IOException, InterruptedException {

        while(!imagesToLoad.isEmpty()){
            ImageProcessingContext image = imagesToLoad.poll();
            while(image.tilingContext.hasNextTile()){
                ProcessingTask task = new ProcessingTask(image.tilingContext.getNextTile(),
                        new MedianFilter(MedianFilter.KERNEL_SIZE.THREE),
                        outputWriterThread,
                        processedTiles);
                task.run();
                /*executorService.submit(new ProcessingTask(image.tilingContext.getNextTile(),
                        new MedianFilter(MedianFilter.KERNEL_SIZE.THREE),
                        outputWriterThread,
                        processedTiles));
                 */
            }
        }

        //executorService.shutdown();

        /*if(!executorService.awaitTermination(1, TimeUnit.HOURS)){
            throw new RuntimeException("Tasks did not finish within 1 hour");
        }else{
            outputWriter.setEndOfStream();
            outputWriterThread.interrupt();
        }*/

        outputWriter.run();
    }
}
