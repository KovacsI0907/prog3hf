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

    public int getLuminance(int x, int y) {
        if (imageInfo.colorType == 0 || imageInfo.colorType == 4) {
            throw new RuntimeException("Image is not grayscale");
        }
        return (int) (pixelValues[y][x] | 0xFF);
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
}
