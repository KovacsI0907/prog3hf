package ParallelImageProcessing;

import ImageProcessingAlgorithms.TileProcessingAlgorithm;
import gui.Logger;

import javax.sound.midi.Soundbank;
import java.util.Queue;

public class ProcessingTask implements Runnable {
    public final ImageTile tile;
    public final TileProcessingAlgorithm algorithm;
    public final Thread outputWriterThread;
    public final Thread processingSchedulerThread;
    public final Queue<ImageTile> processedTiles;
    public final  Logger logger;

    public ProcessingTask(ImageTile tile, TileProcessingAlgorithm algorithm, Thread processingSchedulerThread, Thread outputWriterThread, Queue<ImageTile> processedTiles, Logger logger) {
        this.tile = tile;
        this.algorithm = algorithm;
        this.outputWriterThread = outputWriterThread;
        this.processingSchedulerThread = processingSchedulerThread;
        this.processedTiles = processedTiles;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            logger.log("Processing " + tile.image.imageFile.getName() + "/#" + tile.tileIndex);
            processedTiles.add(algorithm.produceOutput());
        }catch (Exception e){
            logger.logRed("Error processing " + tile.image.imageFile.getName() + "/#" + tile.tileIndex + ":");
            logger.logRed(e.getMessage());
            //TODO release resources
        }
        if(outputWriterThread.getState() == Thread.State.TIMED_WAITING){
            outputWriterThread.interrupt();
        }

        if(processingSchedulerThread.getState() == Thread.State.TIMED_WAITING){
            processingSchedulerThread.interrupt();
        }
    }
}
