package ParallelImageProcessing;

import java.awt.image.BufferedImage;

public class ImageTile {
    public final int height;
    public final int width;
    public final int paddingSize;

    //TODO optimize for smaller channel sizes
    //we use int because it fits a 16 bit unsigned value
    //first 2 dimensions for X and Y
    //third dimension for storing all channels
    // (it's a 2d array of pixel channels)
    long[][] pixelValues;

    public ImageTile(int width, int height, int paddingSize, long[][] pixelValues) {
        if(pixelValues.length != height + 2*paddingSize ||
                pixelValues[0].length != width + 2*paddingSize
        ){
            throw new RuntimeException("Invalid tile size or pixelValues array");
        }

        this.pixelValues = pixelValues;
        this.width = width;
        this.height = height;
        this.paddingSize = paddingSize;
    }

    public BufferedImage getAsImage(){
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);

        for(int y = 0;y<height;y++) {
            for(int x = 0;x<width;x++){
                image.setRGB(x,y, (int)pixelValues[y+paddingSize][x+paddingSize]);
            }
        }

        return image;
    }

    public BufferedImage getAsImageWithPadding(){
        BufferedImage image = new BufferedImage(this.width + 2*paddingSize, this.height + 2*paddingSize, BufferedImage.TYPE_INT_ARGB);

        for(int y = 0;y<height + 2*paddingSize;y++) {
            for(int x = 0;x<width + 2*paddingSize;x++){
                image.setRGB(x,y, (int)pixelValues[y][x]);
            }
        }

        return image;
    }

    public int getRed(int x, int y) {
        return (int) (pixelValues[y][x] >>> 16 | 0xFF);
    }

    public int getGreen(int x, int y) {
        return (int) (pixelValues[y][x] >>> 8 | 0xFF);
    }

    public int getBlue(int x, int y) {
        return (int) (pixelValues[y][x] | 0xFF);
    }

    public int getAlpha(int x, int y) {
        return (int) (pixelValues[y][x] >>> 24 | 0xFF);
    }

    public void setRed(int x, int y, int value) {
        pixelValues[y][x] = (pixelValues[y][x] & 0xFF00FFFFL) | ((value & 0xFFL) << 16);
    }

    public void setGreen(int x, int y, int value) {
        pixelValues[y][x] = (pixelValues[y][x] & 0xFFFF00FFL) | ((value & 0xFFL) << 8);
    }

    public void setBlue(int x, int y, int value) {
        pixelValues[y][x] = (pixelValues[y][x] & 0xFFFFFF00L) | (value & 0xFFL);
    }

    public void setAlpha(int x, int y, int value) {
        pixelValues[y][x] = (pixelValues[y][x] & 0x00FFFFFFL) | ((value & 0xFFL) << 24);
    }

    public void setPixel(int x, int y, long pixelValue) {
        pixelValues[y][x] = pixelValue;
    }
    public long getPixelRelativeOriginal(int x, int y){
        return pixelValues[y + paddingSize][x + paddingSize];
    }

    public long getPixelIncludePadding(int x, int y){
        return pixelValues[y][x];
    }
}
