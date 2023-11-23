package PngOutput;

import ParallelImageProcessing.ImageProcessingContext;
import ParallelImageProcessing.ImageProcessingScheduler;
import ParallelImageProcessing.ImageTile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class OutputWriter implements Runnable{
    BlockingQueue<ImageTile> imageTiles;
    public final HashMap<ImageProcessingContext, ImageTiledWriter> imageWriters;
    boolean endOfStream;
    public final int WAIT_MILLIS = 1000;

    public OutputWriter(BlockingQueue<ImageTile> imageTiles){
        this.imageTiles = imageTiles;
        endOfStream = false;
        imageWriters = new HashMap<>();
    }
    @Override
    public void run() {
        while(true){
            while(!imageTiles.isEmpty()){
                ImageTile tile = imageTiles.poll();
                if(imageWriters.containsKey(tile.image)){
                    imageWriters.get(tile.image).tilesReady.add(tile);
                }else{
                    try {
                        imageWriters.put(tile.image, new ImageTiledWriter(tile.image));
                        imageWriters.get(tile.image).tilesReady.add(tile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }


            List<ImageProcessingContext> toRemove = new ArrayList<>();
            for(ImageProcessingContext ipc : imageWriters.keySet()){
                ImageTiledWriter writer = imageWriters.get(ipc);
                try {
                    writer.tryWriteNextTile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if(writer.isFinished()){
                    try {
                        writer.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    toRemove.add(ipc);
                }
            }

            for(ImageProcessingContext ipc : toRemove){
                imageWriters.remove(ipc);
            }
        }
    }

    public void setEndOfStream(){
        endOfStream = true;
    }
}
