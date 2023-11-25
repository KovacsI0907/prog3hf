package ParallelImageProcessing;

import PngInput.PngLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TilingContext {
    public final int numTiles;
    public final int tileHeight;
    public final int paddingSize;
    // index of the tile that hasn't been read yet
    int currentTileIndex;

    public final PngLoader imageLoader;
    public final ImageProcessingContext image;
    List<long[]> paddingBuffer = new ArrayList<>();

    HashMap<Integer, long[]> loadedRows = new HashMap<>();

    public TilingContext(int tileHeight, int paddingSize, PngLoader imageLoader, ImageProcessingContext image) {
        this.image = image;
        this.numTiles = (int) Math.ceil((double) imageLoader.imageInfo.height / tileHeight);
        this.tileHeight = tileHeight;

        this.paddingSize = paddingSize;
        this.imageLoader = imageLoader;

        this.currentTileIndex = 0;
    }

    public ImageTile getNextTile() throws IOException {
        ImageTile tile;
        if(paddingSize == 0){
            tile = getTileWithoutPadding();
        }else{
            tile = getTileWithPadding();
        }
        currentTileIndex++;

        return tile;
    }

    public boolean hasNextTile() {
        return currentTileIndex != numTiles;
    }
    private long[] mirrorPadRow(long[] row) {
        long[] padded = new long[row.length + 2*paddingSize];

        //apply left pad
        for(int i = 0;i<paddingSize;i++){
            padded[i] = row[paddingSize-i];
        }

        //TODO fix this copy, it's terrible for performance
        //copy middle
        System.arraycopy(row, 0, padded, paddingSize, row.length);

        //apply right pad
        int helper = row.length+paddingSize-1;
        for(int i = 0;i<paddingSize;i++){
            padded[row.length + paddingSize + i] = padded[helper];
            helper--;
        }

        return padded;
    }

    public ImageTile getTileWithoutPadding() throws IOException {
        int rowsToLoad = tileHeight;
        if(currentTileIndex == numTiles-1){
            rowsToLoad = imageLoader.imageInfo.height - (numTiles-1) * tileHeight;
        }
        long[][] pixelValues = new long[rowsToLoad][imageLoader.imageInfo.width];

        for(int y = 0;y<rowsToLoad;y++){
            pixelValues[y] = imageLoader.decodeNextRow();
        }

        return new ImageTile(imageLoader.imageInfo.width, rowsToLoad, 0, image, currentTileIndex, pixelValues);
    }

    public ImageTile getTileWithPadding() throws IOException {
        if(currentTileIndex == 0 && numTiles == 1){
            return getLastAndFirstPaddedTile();
        }

        if(currentTileIndex == 0){
            return getFirstPaddedTile();
        }

        if(currentTileIndex == numTiles-1){
            return getLastPaddedTile();
        }

        //check if there will be enough rows to load
        int rowsLeft = image.imageHeight - currentTileIndex*tileHeight - paddingSize;
        long[][] pixelData;
        if(rowsLeft < tileHeight){
            for(int i = 0;i<rowsLeft;i++){
                paddingBuffer.addLast(imageLoader.decodeNextRow());
            }
            int toMirror = tileHeight-rowsLeft;
            List<long[]> mirrored = mirrorLower(toMirror);

            pixelData = new long[paddingBuffer.size() + mirrored.size()][];
            for(int i = 0; i< paddingBuffer.size(); i++){
                pixelData[i] = mirrorPadRow(paddingBuffer.get(i));
            }

            for(int i = 0;i<mirrored.size();i++){
                pixelData[i + paddingBuffer.size()] = mirrorPadRow(mirrored.get(i));
            }

            int lastTileHeight = image.imageHeight - (numTiles-1)*tileHeight;
            while(paddingBuffer.size() > lastTileHeight + paddingSize){
                paddingBuffer.removeFirst();
            }
        }else{
            for(int i = 0;i<tileHeight;i++){
                paddingBuffer.addLast(imageLoader.decodeNextRow());
            }

            pixelData = new long[paddingBuffer.size()][];
            for(int i = 0; i< paddingBuffer.size(); i++){
                pixelData[i] = mirrorPadRow(paddingBuffer.get(i));
            }

            while(paddingBuffer.size() > paddingSize*2){
                paddingBuffer.removeFirst();
            }
        }

        return new ImageTile(imageLoader.imageInfo.width, tileHeight, paddingSize, image, currentTileIndex, pixelData);
    }

    private List<long[]> mirrorLower(int howMany){
        List<long[]> result = new ArrayList<>();
        for(int i = paddingBuffer.size()-1;i>=paddingBuffer.size()-howMany;i--){
            result.addLast(paddingBuffer.get(i));
        }

        return result;
    }

    private ImageTile getFirstPaddedTile() throws IOException {
        int rowsToLoad = tileHeight + paddingSize;

        for(int i = 0;i<paddingSize;i++){
            long[] row = imageLoader.decodeNextRow();
            paddingBuffer.addFirst(row);
            paddingBuffer.addLast(row);
            rowsToLoad--;
        }

        for(int i = 0;i<rowsToLoad;i++){
            paddingBuffer.addLast(imageLoader.decodeNextRow());
        }

        long[][] pixelData = new long[paddingBuffer.size()][];

        for(int i = 0;i<pixelData.length;i++){
            pixelData[i] =mirrorPadRow(paddingBuffer.get(i));
        }

        while(paddingBuffer.size() > paddingSize*2){
            paddingBuffer.removeFirst();
        }

        return new ImageTile(imageLoader.imageInfo.width, tileHeight, paddingSize, image, currentTileIndex, pixelData);
    }

    private ImageTile getLastPaddedTile() throws IOException {
        int alreadyLoaded = (numTiles-1) * tileHeight + paddingSize;
        int rowsToLoad = imageLoader.imageInfo.height - alreadyLoaded;

        for(int i = 0;i<rowsToLoad;i++){
            paddingBuffer.addLast(imageLoader.decodeNextRow());
        }

        int helper = paddingBuffer.size()-1;
        for(int i = 0;i<paddingSize;i++){
            paddingBuffer.addLast(paddingBuffer.get(helper--));
        }

        long[][] pixelData = new long[paddingBuffer.size()][];
        for(int i = 0;i<pixelData.length;i++){
            pixelData[i] = mirrorPadRow(paddingBuffer.get(i));
        }
        //clean up
        paddingBuffer.clear();

        return new ImageTile(imageLoader.imageInfo.width, pixelData.length - 2*paddingSize, paddingSize, image, currentTileIndex, pixelData);
    }

    private ImageTile getLastAndFirstPaddedTile() throws IOException {
        List<long[]> endPadding = new ArrayList<>();
        for(int i = 0;i< image.imageHeight;i++){
            long[] row = imageLoader.decodeNextRow();
            if(i<paddingSize){
                paddingBuffer.addFirst(row);
            }
            paddingBuffer.addLast(row);
            if((i>=image.imageHeight-paddingSize)){
                endPadding.addFirst(row);
            }
        }
        paddingBuffer.addAll(endPadding);

        long[][] pixelData = new long[paddingBuffer.size()][];
        for(int i = 0;i<pixelData.length;i++){
            pixelData[i] = mirrorPadRow(paddingBuffer.get(i));
        }

        return new ImageTile(imageLoader.imageInfo.width, pixelData.length - 2*paddingSize, paddingSize, image, currentTileIndex, pixelData);
    }

}
