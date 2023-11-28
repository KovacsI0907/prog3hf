package PngInput;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Osztály a Png kéket tulajdonságainak tárolására (pl.: magasság, szélesség, stb.)
 */
public class PngInfo {
    public PngInfo(int width, int height, byte bitDepth, byte colorType, byte compressionMethod, byte filterMethod, byte interlaceMethod) {
        int bytesPerPixel;
        this.width = width;
        this.height = height;
        this.bitDepth = bitDepth;
        this.colorType = colorType;
        this.compressionMethod = compressionMethod;
        this.filterMethod = filterMethod;
        this.interlaceMethod = interlaceMethod;

        numChannels = switch(colorType) {
            case 0 -> 1;
            case 2 -> 3;
            case 4 -> 2;
            case 6 -> 4;
            default -> 0;
        };
    }

    final public int width;
    final public int height;
    final public byte bitDepth;
    final public byte colorType;
    final public byte compressionMethod;
    final public byte filterMethod;
    final public byte interlaceMethod;
    final public int numChannels;

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

    /**
     * Megadja, hogy az ezen osztály szerinti tulajdonságok támogatottak-e a programban
     * @return Támogatott-e a fájl
     */
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
