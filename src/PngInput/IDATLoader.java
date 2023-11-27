package PngInput;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class IDATLoader {

    private final InputStream is;
    private long currentPos;
    private long currentLength;
    private int debugCounter = 0;

    public IDATLoader(InputStream is) throws IOException {
        PngLogger.info("Initializing IDAT loader");
        this.is = is;
        currentPos = 0;
        currentLength = 0;
        startNewIDAT();
        PngLogger.info("IDAT loader initialized");
    }

    private void skipCRC() throws IOException {
        Helper.readExactlyNBytes(is, 4);
    }

    private boolean startNewIDAT() throws PngLoaderException, IOException {
        if(currentPos != currentLength){
            throw new PngLoaderException("Attempt to start new IDAT before finishing other");
        }

        debugCounter++;
        PngLogger.info("Starting new IDAT after: currentPos: " + currentPos + " currentLength: " + currentLength + "\nstarted " + debugCounter + " times");

        currentPos = 0;

        currentLength = Helper.readUint32(is);
        String chunkType = Helper.readChunkType(is);
        while(!(chunkType.equals("IDAT") || chunkType.equals("IEND"))){
            //skip uninteresting data
            Helper.readExactlyNBytes(is, (int)currentLength);
            skipCRC();

            //read next header
            currentLength = Helper.readUint32(is);
            chunkType = Helper.readChunkType(is);
        }

        return !chunkType.equals("IEND");
    }

    private long currentRemainingBytes() {
        return currentLength - currentPos;
    }


    public int loadNBytes(byte[] buffer, int n, int offset) throws IOException {
        int bytesLoaded = 0;

        while(bytesLoaded < n){
            if(n-bytesLoaded < currentRemainingBytes()){ //current chunk has more than enough bytes left
                Helper.readExactlyNBytes(is, n-bytesLoaded, buffer, offset+bytesLoaded);
                currentPos += n - bytesLoaded;
                bytesLoaded += n - bytesLoaded;
            }else{ //current chunk doesn't have enough bytes left
                int remainingFromCurrent = (int)currentRemainingBytes();
                Helper.readExactlyNBytes(is, remainingFromCurrent, buffer, offset+bytesLoaded);
                currentPos += remainingFromCurrent;
                bytesLoaded += remainingFromCurrent;

                //chunk is depleted continue to next one
                skipCRC();
                if(!startNewIDAT()) {
                    return bytesLoaded;
                }
            }
        }

        return bytesLoaded;
    }


}
