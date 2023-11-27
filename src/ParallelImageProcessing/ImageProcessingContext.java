package ParallelImageProcessing;
import PngInput.PngLoader;
import gui.UserPreferences;

import java.io.File;
import java.io.IOException;

public class ImageProcessingContext {

    public static final int TILE_SIZE_TARGET = 4;
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
        int targetSize = TILE_SIZE_TARGET*1024*1024 / 8; // bytes/bytesPerPixel -> 5MB / 8
        // (w+2p)(h+2p) = pixels
        // wh + 2pw + 2ph + 4p^2 = pixels
        // h(w+2p) = pixels - 2pw + 4p^2
        // h = (pixels -2pw + 4p^2) / (w+2p)
        int height = (targetSize - 2*paddingSize*width + 4*paddingSize*paddingSize) / (width + 2*paddingSize);
        if(height < paddingSize * 1.25){
            height = (int)(paddingSize * 1.25);
        }
        return height;
    }

    public static int memoryRequiredForSmoothOperation(int numThreads){
        int spaceForProcessing = ImageProcessingContext.TILE_SIZE_TARGET * (numThreads + 1) * 3; //Tile size * (threads + safety) * multiplier for processing
        int spaceForLoadAndSave = 2 * 2 * ImageProcessingContext.TILE_SIZE_TARGET; //(Save + Load) * 2 tiles * tile size
        int minMem = spaceForLoadAndSave + spaceForProcessing;
        return minMem*10/9;
    }
    public static boolean canLoadNextTile() {
        UserPreferences pref = UserPreferences.getInstance();
        int spaceForProcessing = ImageProcessingContext.TILE_SIZE_TARGET * (pref.threadCount) * 2; //Tile size * (threads + safety) * multiplier for processing
        int maxUsage = (int) (pref.memorySize * 0.85) - spaceForProcessing;
        int currentUsage = (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024*1024));
        int headroom = maxUsage - currentUsage;

        if(headroom <= ImageProcessingContext.TILE_SIZE_TARGET){
            System.gc();
        }

        return headroom >= ImageProcessingContext.TILE_SIZE_TARGET;
    }
}
