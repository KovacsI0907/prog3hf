package ImageProcessingAlgorithms;

import ParallelImageProcessing.ImageTile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MedianFilter extends TileProcessingAlgorithm{

    public enum KERNEL_SIZE {
        THREE,
        FIVE,
        SEVEN,
        NINE
    }

    int kernelSize;

    public MedianFilter(KERNEL_SIZE kernelSize){
        this.kernelSize = switch(kernelSize){
            case THREE -> 3;
            case FIVE -> 5;
            case SEVEN -> 7;
            case NINE -> 9;
        };
    }

    @Override
    public ImageTile produceOutput(ImageTile tile) {
        long[][] outputData = new long[tile.height][tile.width];

        for(int y = 0;y<tile.height;y++){
            for(int x = 0;x<tile.width;x++){
                outputData[y][x] = getMedianPixel(x,y, tile);
            }
        }

        Random random = new Random();

        try {
            Thread.sleep(random.nextInt(0,200) + tile.height*tile.width/100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new ImageTile(tile.width, tile.height, 0, tile.image, tile.tileIndex, outputData);
    }

    private long getMedianPixel(int x, int y, ImageTile tile) {
        List<Long> pixels = new ArrayList<>();
        for(int i = y-kernelSize/2;i <= y+kernelSize/2;i++){
            for(int j = x-kernelSize/2;j <= x+kernelSize/2;j++){
                pixels.add(tile.getPixelRelativeOriginal(j,i));
            }
        }
        Collections.sort(pixels);
        return pixels.get(pixels.size()/2);
    }
}
