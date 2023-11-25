package ParallelImageProcessing;
import PngInput.PngLoader;

import java.io.File;
import java.io.IOException;

public class ImageProcessingContext {
    public final TilingContext tilingContext;
    public final File imageFile;
    public final int imageWidth;
    public final int imageHeight;

    public ImageProcessingContext(File imageFile, int paddingSize) throws IOException {
        this.imageFile = imageFile;
        PngLoader pngLoader = new PngLoader(imageFile);
        this.tilingContext = new TilingContext(determineTileHeight(pngLoader.imageInfo.width, paddingSize), paddingSize, pngLoader, this);
        imageWidth = tilingContext.imageLoader.imageInfo.width;
        imageHeight = tilingContext.imageLoader.imageInfo.height;
    }

    private int determineTileHeight(int width, int paddingSize) {
        //t*w = 130k
        //approximately 1MB tiles
        int h = 100000 / (width+2*paddingSize);
        if(h<2*paddingSize){
            h = 2*paddingSize;
        }
        return h;
    }
}
