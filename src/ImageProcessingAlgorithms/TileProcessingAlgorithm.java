package ImageProcessingAlgorithms;

import ParallelImageProcessing.ImageTile;

public abstract class TileProcessingAlgorithm {
    public final AlgorithmParameters params;
    public final ImageTile tile;
    public TileProcessingAlgorithm(AlgorithmParameters params, ImageTile tile) {
        this.params = params;
        this.tile = tile;
        if(tile.paddingSize < params.paddingSizeNecessary){
            throw new IllegalArgumentException("Padding size of the given tile is too small for the given algorithm");
        }
    }

    public abstract ImageTile produceOutput();
}
