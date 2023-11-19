package Algorithms;

import PngLoader.ImageTile;

public abstract class TileProcessingAlgorithm {
    public final ImageTile input;
    protected ImageTile output;

    public TileProcessingAlgorithm(ImageTile input){
        this.input = input;
        this.output = null;
    }

    public abstract void produceOutput();

    public ImageTile getOutput() {
        if(output == null){
            throw new RuntimeException("Processing not done yet");
        }
        return output;
    }
}
