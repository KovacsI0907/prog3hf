package PngOutput;

import ParallelImageProcessing.ImageProcessingContext;
import ParallelImageProcessing.ImageTile;
import gui.AlgoStatusCard;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * Külön szálon is futni tudó png fájlkiíró kezelő.
 * Az ImageTiledWriter-eket ütemezi.
 * A képeket a következős sorrendben írja:
 * Az íródik ami közelebb áll ahhoz hogy kész legyen
 */
public class OutputWriter implements Runnable{
    BlockingQueue<ImageTile> tilesSharedQueue;
    private PriorityQueue<ImageTiledWriter> imageWriters;
    private boolean endOfStream;
    public static final int WAIT_MILLIS = 1000;
    public final AlgoStatusCard algoStatusCard;
    public final List<ImageProcessingContext> stoppedImageList;

    public OutputWriter(BlockingQueue<ImageTile> imageTiles, AlgoStatusCard algoStatusCard, List<ImageProcessingContext> stoppedImageList){
        this.tilesSharedQueue = imageTiles;
        this.stoppedImageList = stoppedImageList;
        endOfStream = false;
        imageWriters = new PriorityQueue<>(Comparator.comparingInt(ImageTiledWriter::tilesLeft));
        this.algoStatusCard = algoStatusCard;
    }

    /**
     * Veszi a schedulerrel közösen használt queueból az írásra készenálló képdarabokat és
     * beosztja őket a megfelelő képíróhoz. Ezután megpróbál minden képíróval kiírata addig amíg lehet.
     * Ezután TIMED_WAITINGBE megy, ha nincs több teendője. A várakozásból a feldolgozást végző szálak
     * ébresztik fel, amikor egy újabb képdarabot raknak a közös queueba.
     */
    @Override
    public void run() {
        do{
            try {
                fillWriters();
            } catch (IOException e) {
                algoStatusCard.logger.logRed("Couldn't write header for output image");
            }

            PriorityQueue<ImageTiledWriter> newQueue = new PriorityQueue<>(Comparator.comparingInt(ImageTiledWriter::tilesLeft));
            while(!imageWriters.isEmpty()){
                ImageTiledWriter writer = imageWriters.poll();
                try {
                    while(writer.tryWriteNextTile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if(writer.isFinished()){
                    try {
                        writer.close();
                        algoStatusCard.logger.logGreen("Finished " + writer.outputFile.getName());
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

    /**
     * Veszi a várakozó képdarabokat és beosztja öket a megfelelő writerhez.
     * Ha a képdarab megszakított képhez tartozik akkor csak eldobja azt
     * @throws IOException Ha nem sikerül terminálni a megszakított kép darabját
     */
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

    /**
     * Megkeresi a képdarabhoz tartozó írót és beleteszi abba.
     * Ha még nincs író ehhez a képhez, akkor csinál egy újat
     * @param tile A tile amit íróba akarunk rakni
     * @throws IOException
     */
    private void putTileIntoWriter(ImageTile tile) throws IOException {

        for(ImageTiledWriter writer : imageWriters){
            if(writer.imageProcessingContext == tile.image){
                writer.tilesReady.add(tile);
                return;
            }
        }

        ImageTiledWriter writer = new ImageTiledWriter(tile.image);
        writer.tilesReady.add(tile);
        imageWriters.add(writer);
    }

    /**
     * Ellenőrzi, hogy a kép context olyan képhez tartozik-e amit már termináltunk
     * @param ipc A kép context amit vizsgálunk
     * @return a kép terminált-e
     */
    private boolean imageIsDead(ImageProcessingContext ipc){
        synchronized (stoppedImageList){
            return stoppedImageList.contains(ipc);
        }
    }


    public void setEndOfStream(){
        endOfStream = true;
    }
}
