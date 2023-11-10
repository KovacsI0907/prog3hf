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
    int[][][] pixelValues;

    public ImageTile(int upperLeftX, int upperLeftY, int width, int height, PngInfo imageInfo, int[][][] pixelValues) {
        if(pixelValues.length != upperLeftX + width ||
                pixelValues[0].length != upperLeftY + height ||
                pixelValues[0][0].length != imageInfo.numChannels
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

        if(imageInfo.colorType != 6 || imageInfo.bitDepth != 8){
            throw new RuntimeException("Not implemented yet");
        }

        for(int y = 0;y<height;y++) {
            for(int x = 0;x<width;x++){
                int[] pixvals = pixelValues[x][y];
                image.setRGB(x,y, getARGBInt(pixvals[0], pixvals[1], pixvals[2], pixvals[3]));
            }
        }

        return image;
    }

    private int getARGBInt(int r, int g, int b, int a){
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
