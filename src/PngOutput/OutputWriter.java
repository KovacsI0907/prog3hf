package PngOutput;

import ParallelImageProcessing.ImageTile;

import java.util.Queue;

public class OutputWriter implements Runnable{
    Queue<ImageTile> imageTiles;
    boolean endOfStream;
    public final int WAIT_MILLIS = 1000;

    public OutputWriter(Queue<ImageTile> imageTiles){
        this.imageTiles = imageTiles;
        endOfStream = false;
    }
    @Override
    public void run() {
        while(!(endOfStream && imageTiles.isEmpty())){
            if(imageTiles.isEmpty()){
                try{
                    Thread.sleep(WAIT_MILLIS);
                } catch (InterruptedException e) {
                    if(imageTiles.isEmpty()){
                        if(!endOfStream) {
                            throw new RuntimeException("Something terrible has happened");
                        }
                    }
                }
            }else{
                writeTile(imageTiles.poll());
            }
        }
    }

    private void writeTile(ImageTile tile){
        System.out.println("Written tile: " + tile.toString());
    }

    public void setEndOfStream(){
        endOfStream = true;
    }
}
