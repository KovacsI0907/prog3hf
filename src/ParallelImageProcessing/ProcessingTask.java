package ParallelImageProcessing;

import ImageProcessingAlgorithms.TileProcessingAlgorithm;

import javax.sound.midi.Soundbank;
import java.util.Queue;

public class ProcessingTask implements Runnable {
    public final ImageTile tile;
    public final TileProcessingAlgorithm algorithm;
    public final Thread outputWriterThread;
    public final Thread processingSchedulerThread;
    public final Queue<ImageTile> processedTiles;

    public ProcessingTask(ImageTile tile, TileProcessingAlgorithm algorithm, Thread processingSchedulerThread, Thread outputWriterThread, Queue<ImageTile> processedTiles) {
        this.tile = tile;
        this.algorithm = algorithm;
        this.outputWriterThread = outputWriterThread;
        this.processingSchedulerThread = processingSchedulerThread;
        this.processedTiles = processedTiles;
    }

    @Override
    public void run() {
        try {
            processedTiles.add(algorithm.produceOutput());
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(tile.image.imageFile.getName());
        }
        if(outputWriterThread.getState() == Thread.State.TIMED_WAITING){
            outputWriterThread.interrupt();
        }

        if(processingSchedulerThread.getState() == Thread.State.TIMED_WAITING){
            processingSchedulerThread.interrupt();
        }
    }
}
