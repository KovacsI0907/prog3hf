package ParallelImageProcessing;

import ImageProcessingAlgorithms.TileProcessingAlgorithm;
import PngInput.PngLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessingContext {
    public TilingContext tilingContext;

    public ImageProcessingContext(File imageFile) throws IOException {
        this.tilingContext = new TilingContext(100, 1, new PngLoader(imageFile));
    }
}
