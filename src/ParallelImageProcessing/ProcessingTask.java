package ParallelImageProcessing;

import ImageProcessingAlgorithms.TileProcessingAlgorithm;
import gui.Logger;

import javax.sound.midi.Soundbank;
import java.util.Queue;

/**
 * Ez az osztály becsomagol egy algoritmust, hogy többszálon futtatható legyen, valamint eelvégzi
 * a többszálúsággal járó értesítési feladatokat, hogy az algoritmusoknak ezzel ne kelljen foglalkozni.
 */
public class ProcessingTask implements Runnable {
    public final ImageTile tile;
    public final TileProcessingAlgorithm algorithm;
    public final Thread outputWriterThread;
    public final Thread processingSchedulerThread;
    public final Queue<ImageTile> processedTiles;
    public final  Logger logger;
    public final ImageProcessingScheduler imageProcessingScheduler;

    public ProcessingTask(ImageTile tile, TileProcessingAlgorithm algorithm, Thread processingSchedulerThread, ImageProcessingScheduler imageProcessingScheduler, Thread outputWriterThread, Queue<ImageTile> processedTiles, Logger logger) {
        this.tile = tile;
        this.algorithm = algorithm;
        this.outputWriterThread = outputWriterThread;
        this.processingSchedulerThread = processingSchedulerThread;
        this.processedTiles = processedTiles;
        this.logger = logger;
        this.imageProcessingScheduler = imageProcessingScheduler;
    }

    /**
     * Lefuttatja az algoritmust és értesíti a schedulert és az oz output writert ha kész van
     */
    @Override
    public void run() {
        try {
            logger.log("Processing " + tile.image.imageFile.getName() + "/#" + tile.tileIndex);
            processedTiles.add(algorithm.produceOutput());
        }catch (Exception e){
            logger.logRed("Error processing " + tile.image.imageFile.getName() + "/#" + tile.tileIndex + ":");
            logger.logRed(e.getMessage());
            imageProcessingScheduler.stopProcessingImage(tile.image);
        }
        if(outputWriterThread.getState() == Thread.State.TIMED_WAITING){
            outputWriterThread.interrupt();
        }

        if(processingSchedulerThread.getState() == Thread.State.TIMED_WAITING){
            processingSchedulerThread.interrupt();
        }
    }
}
