package ParallelImageProcessing;

import java.awt.image.BufferedImage;

/**
 * Képdarabot tároló osztály.
 * Általános formában tárolja a képeket, nem függ konkrét kiterjesztéstől (pl. png, jpeg)
 */
public class ImageTile {
    public final int height;
    public final int width;
    public final int paddingSize;

    public final ImageProcessingContext image;
    public final int tileIndex;

    //we use int because it fits a 16 bit unsigned value
    //first 2 dimensions for X and Y
    //third dimension for storing all channels
    // (it's a 2d array of pixel channels)
    private long[][] pixelValues;

    /**
     * Inicializál egy képdarabot az adott adattömbből
     * @param width
     * @param height
     * @param paddingSize
     * @param image
     * @param tileIndex
     * @param pixelValues
     */
    public ImageTile(int width, int height, int paddingSize, ImageProcessingContext image, int tileIndex, long[][] pixelValues) {
        this.image = image;
        this.tileIndex = tileIndex;
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

    public long[] getRow(int y) {
        return pixelValues[y];
    }

    public int getTileIndex(){
        return tileIndex;
    }

    /**
     * a get* típusú függvények egy pixel adott csatornáját adják vissza
     * a set* ugyanezt állítja
     * a get*RO a padding-el rendelkező képek esetén a paddinget nem számítva értelmezi a koordínátákat
     * @param x
     * @param y
     * @return
     */
    public int getRed(int x, int y) {
        return (int) (pixelValues[y][x] >>> 16 & 0xFF);
    }

    public int getGreen(int x, int y) {
        return (int) (pixelValues[y][x] >>> 8 & 0xFF);
    }

    public int getBlue(int x, int y) {
        return (int) (pixelValues[y][x] & 0xFF);
    }

    public int getAlpha(int x, int y) {
        return (int) (pixelValues[y][x] >>> 24 & 0xFF);
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

    public int getRedRO(int x, int y) {
        return (int) (pixelValues[y + paddingSize][x + paddingSize] >>> 16 & 0xFF);
    }

    public int getGreenRO(int x, int y) {
        return (int) (pixelValues[y + paddingSize][x + paddingSize] >>> 8 & 0xFF);
    }

    public int getBlueRO(int x, int y) {
        return (int) (pixelValues[y + paddingSize][x + paddingSize] & 0xFF);
    }

    public int getAlphaRO(int x, int y) {
        return (int) (pixelValues[y + paddingSize][x + paddingSize] >>> 24 & 0xFF);
    }
}
