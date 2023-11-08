package PngLoader;

public class ImageTile {
    final PngInfo imageInfo;
    final int upperLeftX;
    final int upperLeftY;
    final int lowerRightX;
    final int lowerRightY;

    //size of padding around edges of the tile
    //this is needed so that algorithms don't produce "tiled looking" results
    final int paddingSize;

    //TODO optimize for smaller channel sizes
    //we use int because it fits a 16 bit unsigned value
    //first 2 dimensions for X and Y
    //third dimension for storing all channels
    // (it's a 2d array of pixel channels)
    int[][][] pixelValues;

    public ImageTile(int upperLeftX, int upperLeftY, int lowerRightX, int lowerRightY, PngInfo imageInfo, int paddingSize int[][][] pixelValues) {
        if(pixelValues.length != lowerRightX - upperLeftX + paddingSize ||
                pixelValues[0].length != lowerRightY - upperLeftY + paddingSize ||
                pixelValues[0][0].length != imageInfo.numChannels
        ){
            throw new RuntimeException("Invalid tile size or pixelValues array");
        }

        this.imageInfo = imageInfo;
        this.pixelValues = pixelValues;
        this.paddingSize = paddingSize;
        this.upperLeftX = upperLeftX;
        this.lowerRightX = lowerRightX;
        this.upperLeftY = upperLeftY;
        this.lowerRightY = lowerRightY;
    }
}
