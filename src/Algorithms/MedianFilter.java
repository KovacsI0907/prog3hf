package Algorithms;

import PngLoader.ImageTile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedianFilter extends TileProcessingAlgorithm{

    public enum KERNEL_SIZE {
        THREE,
        FIVE,
        SEVEN,
        NINE
    }

    int kernelSize;

    public MedianFilter(ImageTile input, KERNEL_SIZE kernelSize){
        super(input);

        this.kernelSize = switch(kernelSize){
            case THREE -> 3;
            case FIVE -> 5;
            case SEVEN -> 7;
            case NINE -> 9;
        };

        if(input.paddingSize < this.kernelSize / 2)
            throw new RuntimeException("Padding too small for this algorithm");
    }

    @Override
    public void produceOutput() {
        long[][] outputData = new long[input.height][input.width];

        for(int y = 0;y<input.height;y++){
            for(int x = 0;x<input.width;x++){
                outputData[y][x] = getMedianPixel(x,y);
            }
        }

        output = new ImageTile(input.width, input.height, 0, outputData);
    }

    private long getMedianPixel(int x, int y) {
        List<Long> pixels = new ArrayList<>();
        for(int i = y-kernelSize/2;i <= y+kernelSize/2;i++){
            for(int j = x-kernelSize/2;j <= x+kernelSize/2;j++){
                pixels.add(input.getPixelRelativeOriginal(j,i));
            }
        }
        Collections.sort(pixels);
        return pixels.get(pixels.size()/2);
    }
}
