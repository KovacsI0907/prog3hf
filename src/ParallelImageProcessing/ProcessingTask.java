package ParallelImageProcessing;

import ImageProcessingAlgorithms.TileProcessingAlgorithm;

public class ProcessingTask implements Runnable {
    public final ImageTile tile;
    public final TileProcessingAlgorithm algorithm;

    public ProcessingTask(ImageTile tile, TileProcessingAlgorithm algorithm) {
        this.tile = tile;
        this.algorithm = algorithm;
        if(algorithm.input != tile) {
            throw new RuntimeException("Input of algorithm doesn't match given tile");
        }
    }

    @Override
    public void run() {
        algorithm.produceOutput();
    }
}
