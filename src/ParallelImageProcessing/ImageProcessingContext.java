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

        if(pngLoader.imageInfo.width <= paddingSize+1 || pngLoader.imageInfo.height <= paddingSize+1){
            throw new RuntimeException("Image is to small for this kernel/padding size");
        }
    }

    private int determineTileHeight(int width, int paddingSize) {
        int targetSize = 10*1024*1024 / 8; // bytes/bytesPerPixel -> 10MB / 8
        // (w+2p)(h+2p) = pixels
        // wh + 2pw + 2ph + 4p^2 = pixels
        // h(w+2p) = pixels - 2pw + 4p^2
        // h = (pixels -2pw + 4p^2) / (w+2p)
        return (targetSize - 2*paddingSize*width + 4*paddingSize*paddingSize) / (width + 2*paddingSize);
    }
}
