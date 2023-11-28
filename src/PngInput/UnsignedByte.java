package PngInput;

/**
 * Előjelnélküli byte osztály
 * Nincs rá szigorúan szükség, de megkönnyíti a fejlesztést, mert a compiler tudja ellenőrízni (cserébe nem memóriahatékony)
 */
public class UnsignedByte {
    final short value;

    public UnsignedByte(short unsignedByteAsShort){
        this.value = unsignedByteAsShort;
        if(this.value > 255 || this.value < 0)
            throw new RuntimeException("Unsigned byte not within valid range");
    }

    public UnsignedByte(int unsignedByteAsInt){
        this.value = (short) unsignedByteAsInt;
        if(this.value > 255 || this.value < 0)
            throw new RuntimeException("Unsigned byte not within valid range");
    }

    public UnsignedByte(byte defaultByte){
        this.value = (short)(defaultByte & 0xff);
    }

    @Override
    public String toString() {
        return Integer.toString(this.value);
    }

    public static UnsignedByte fromIntMod256(int num){
        return new UnsignedByte(num%256);
    }
}
