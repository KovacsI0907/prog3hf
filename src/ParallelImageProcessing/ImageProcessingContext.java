package ParallelImageProcessing;
import PngInput.PngLoader;

import java.io.File;
import java.io.IOException;

public class ImageProcessingContext {
    public final TilingContext tilingContext;
    public final File imageFile;
    public final int imageWidth;
    public final int imageHeight;

    public ImageProcessingContext(File imageFile) throws IOException {
        this.imageFile = imageFile;
        this.tilingContext = new TilingContext(100, 1, new PngLoader(imageFile), this);
        imageWidth = tilingContext.imageLoader.imageInfo.width;
        imageHeight = tilingContext.imageLoader.imageInfo.height;
    }
}
