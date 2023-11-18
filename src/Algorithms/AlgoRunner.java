package Algorithms;

import PngLoader.ImageTile;

public abstract class AlgoRunner implements Runnable{

    public AlgoRunner(ImageTile imageTile){
        this.imageTile = imageTile;
    }
    protected ImageTile imageTile;
}
