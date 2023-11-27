package PngOutput;

import ParallelImageProcessing.ImageProcessingContext;
import ParallelImageProcessing.ImageTile;
import gui.AlgoStatusCard;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class OutputWriter implements Runnable{
    BlockingQueue<ImageTile> tilesSharedQueue;
    private PriorityQueue<ImageTiledWriter> imageWriters;
    boolean endOfStream;
    public final int WAIT_MILLIS = 1000;
    public final File outputFolder;
    public final AlgoStatusCard algoStatusCard;
    public final List<ImageProcessingContext> stoppedImageList;

    public OutputWriter(BlockingQueue<ImageTile> imageTiles, File outputFolder, AlgoStatusCard algoStatusCard, List<ImageProcessingContext> stoppedImageList){
        this.tilesSharedQueue = imageTiles;
        this.outputFolder = outputFolder;
        this.stoppedImageList = stoppedImageList;
        endOfStream = false;
        imageWriters = new PriorityQueue<>(Comparator.comparingInt(ImageTiledWriter::tilesLeft));
        this.algoStatusCard = algoStatusCard;
    }
    @Override
    public void run() {
        boolean canWriteNext = false;
        do{
            try {
                fillWriters();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage() + "\n Couldn't write header for output image");
            }

            PriorityQueue<ImageTiledWriter> newQueue = new PriorityQueue<>(Comparator.comparingInt(ImageTiledWriter::tilesLeft));
            while(!imageWriters.isEmpty()){
                ImageTiledWriter writer = imageWriters.poll();
                try {
                    writer.tryWriteAll();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if(writer.isFinished()){
                    try {
                        writer.close();
                        algoStatusCard.updateMainProgressBar();
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage() + "\nError occured when closing output image");
                    }
                }else {
                    newQueue.add(writer);
                }
            }

            imageWriters = newQueue;

            if(!endOfStream){
                try {
                    Thread.sleep(WAIT_MILLIS);
                } catch (InterruptedException ignored) {
                    //something has happened, continue working
                }
            }

            //to stop loop: endOfStream AND imageWriters empty AND tilesSharedQueue empty
        }while(!(endOfStream && imageWriters.isEmpty() && tilesSharedQueue.isEmpty()));
    }

    private void fillWriters() throws IOException {
        //check for dead writers
        Iterator<ImageTiledWriter> writerIterator = imageWriters.iterator();
        while(writerIterator.hasNext()){
            ImageTiledWriter writer = writerIterator.next();
            if(imageIsDead(writer.imageProcessingContext)){
                writer.terminate();
                writerIterator.remove();
            }
        }


        while(!tilesSharedQueue.isEmpty()){
            ImageTile tile = tilesSharedQueue.poll();
            if(!imageIsDead(tile.image)) {
                putTileIntoWriter(tile);
            }
        }
    }

    private void putTileIntoWriter(ImageTile tile) throws IOException {

        for(ImageTiledWriter writer : imageWriters){
            if(writer.imageProcessingContext == tile.image){
                writer.tilesReady.add(tile);
                return;
            }
        }

        ImageTiledWriter writer = new ImageTiledWriter(tile.image, outputFolder);
        writer.tilesReady.add(tile);
        imageWriters.add(writer);
    }

    private boolean imageIsDead(ImageProcessingContext ipc){
        synchronized (stoppedImageList){
            return stoppedImageList.contains(ipc);
        }
    }



    public void setEndOfStream(){
        endOfStream = true;
    }
}
