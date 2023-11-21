package ParallelImageProcessing;

import ImageProcessingAlgorithms.TileProcessingAlgorithm;

import java.util.Queue;

public class ProcessingTask implements Runnable {
    public final ImageTile tile;
    public final TileProcessingAlgorithm algorithm;
    public final Thread outputWriterThread;
    public final Queue<ImageTile> processedTiles;

    public ProcessingTask(ImageTile tile, TileProcessingAlgorithm algorithm, Thread outputWriterThread, Queue<ImageTile> processedTiles) {
        this.tile = tile;
        this.algorithm = algorithm;
        this.outputWriterThread = outputWriterThread;
        this.processedTiles = processedTiles;
    }

    @Override
    public void run() {
        processedTiles.add(algorithm.produceOutput(tile));

        if(outputWriterThread.getState() == Thread.State.WAITING || outputWriterThread.getState() == Thread.State.TIMED_WAITING){
            outputWriterThread.interrupt();
        }
    }
}
