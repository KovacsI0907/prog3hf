package ImageProcessingAlgorithms;

import ParallelImageProcessing.ImageTile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MedianFilter extends TileProcessingAlgorithm{

    int kernelSize;

    public MedianFilter(MedianFilterParams params, ImageTile tile) {
        super(params, tile);
        kernelSize = params.kernelSize;
    }

    @Override
    public ImageTile produceOutput() {
        long[][] outputData = new long[tile.height][tile.width];

        for(int y = 0;y<tile.height;y++){
            for(int x = 0;x<tile.width;x++){
                outputData[y][x] = getMedianPixel(x,y, tile);
            }
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
