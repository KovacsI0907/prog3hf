package Algorithms;

import PngLoader.ImageTile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedianFilter extends AlgoRunner{

    public enum KERNEL_SIZE {
        THREExTHREE,
        FIVExFIVE,
        SEVENxSEVEN,
        NINExNINE
    }

    int kernelSize;
    ImageTile output = null;

    public MedianFilter(ImageTile imageTile, KERNEL_SIZE kernelSize){

        super(imageTile);

        this.kernelSize = switch (kernelSize){
            case THREExTHREE -> 3;
            case FIVExFIVE -> 5;
            case SEVENxSEVEN -> 7;
            case NINExNINE -> 9;
        };

        if(imageTile.paddingSize < this.kernelSize/2){
            throw new RuntimeException("Padding size too small for kernel");
        }
    }
    @Override
    public void run() {
        long[][] outputData = new long[imageTile.height][imageTile.width];

        for(int y = 0;y<imageTile.height;y++){
            for(int x = 0;x<imageTile.width;x++){
                outputData[y][x] = getMedianPixel(x,y);
            }
        }

        output = new ImageTile(imageTile.upperLeftX,
                imageTile.upperLeftY,
                imageTile.width,
                imageTile.height,
                0,
                imageTile.imageInfo,
                outputData);
    }

    private long getMedianPixel(int x, int y) {
        List<Long> pixels = new ArrayList<>();
        for(int i = y-kernelSize/2;i <= y+kernelSize/2;i++){
            for(int j = x-kernelSize/2;j <= x+kernelSize/2;j++){
                pixels.add(imageTile.getPixelRelativeOriginal(j,i));
            }
        }
        Collections.sort(pixels);
        return pixels.get(pixels.size()/2);
    }

    public ImageTile getOutput(){
        if(output == null){
            throw new RuntimeException("Output not done yet");
        }

        return output;
    }
}
