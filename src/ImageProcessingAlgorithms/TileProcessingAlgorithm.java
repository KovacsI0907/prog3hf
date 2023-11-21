package ImageProcessingAlgorithms;

import ParallelImageProcessing.ImageTile;

public abstract class TileProcessingAlgorithm {
    public abstract ImageTile produceOutput(ImageTile tile);
}
