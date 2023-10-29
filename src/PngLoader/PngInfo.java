package PngLoader;

import java.util.Arrays;
import java.util.HashMap;

//Class for storing png parameters and validating whether this type of image is supported.
public class PngInfo {
    public PngInfo(long width, long height, byte bitDepth, byte colorType, byte compressionMethod, byte filterMethod, byte interlaceMethod) {
        this.width = width;
        this.height = height;
        this.bitDepth = bitDepth;
        this.colorType = colorType;
        this.compressionMethod = compressionMethod;
        this.filterMethod = filterMethod;
        this.interlaceMethod = interlaceMethod;
    }

    final long width;
    final long height;
    final byte bitDepth;
    final byte colorType;
    final byte compressionMethod;
    final byte filterMethod;
    final byte interlaceMethod;

    public String toString() {
        return
                "width:       " + width +
                        "\nheight:      " + height +
                        "\nbit-depth:   " + bitDepth +
                        "\ncolor-type:  " + colorType +
                        "\ncompression: " + compressionMethod +
                        "\nfilter:      " + filterMethod +
                        "\ninterlace:   " + interlaceMethod + "\n";
    }

    public boolean validate() {
        if(width < 1 || height < 1){
            return false;
        }

        if(compressionMethod != 0){
            return false;
        }

        if(filterMethod != 0){
            return false;
        }

        if(interlaceMethod != 0) //INTERLACING NOT SUPPORTED
        {
            return false;
        }

        HashMap<Integer, Integer[]> colType_BitDepths = new HashMap<>();
        colType_BitDepths.put(0, new Integer[]{1, 2, 4, 8, 16});
        colType_BitDepths.put(2, new Integer[]{8,16});
        //colType_BitDepths.put(3, new int[]{1,2,4,8}); PALETTE COLORS NOT SUPPORTED
        colType_BitDepths.put(4, new Integer[]{8,16});
        colType_BitDepths.put(6, new Integer[]{8,16});

        if(colType_BitDepths.containsKey((int)colorType)){
            Integer[] arr = colType_BitDepths.get((int)colorType);
            return Arrays.asList(arr).contains((int) bitDepth);
        }else{
            return false;
        }
    }
}
