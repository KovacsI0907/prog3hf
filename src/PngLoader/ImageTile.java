package PngLoader;

import java.awt.image.BufferedImage;

public class ImageTile {
    final PngInfo imageInfo;
    final int upperLeftX;
    final int upperLeftY;

    final int width;
    final int height;

    //TODO optimize for smaller channel sizes
    //we use int because it fits a 16 bit unsigned value
    //first 2 dimensions for X and Y
    //third dimension for storing all channels
    // (it's a 2d array of pixel channels)
    long[][] pixelValues;

    public ImageTile(int upperLeftX, int upperLeftY, int width, int height, PngInfo imageInfo, long[][] pixelValues) {
        if(pixelValues.length != height ||
                pixelValues[0].length != width
        ){
            throw new RuntimeException("Invalid tile size or pixelValues array");
        }

        this.imageInfo = imageInfo;
        this.pixelValues = pixelValues;
        this.upperLeftX = upperLeftX;
        this.width = width;
        this.upperLeftY = upperLeftY;
        this.height = height;
    }

    public BufferedImage getAsImage(){
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);

        if(imageInfo.bitDepth == 8){
            if(imageInfo.colorType == 6 || imageInfo.colorType == 2 || imageInfo.colorType == 0 || imageInfo.colorType == 4){
                for(int y = 0;y<height;y++) {
                    for(int x = 0;x<width;x++){
                        image.setRGB(x,y, (int)pixelValues[y][x]);
                    }
                }
            }else{
                throw new RuntimeException("Not implemented yet");
            }
        }else{
            throw new RuntimeException("Not implemented yet");
        }

        return image;
    }
}
