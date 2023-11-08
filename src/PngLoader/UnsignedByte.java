package PngLoader;


public class UnsignedByte {
    final short value;

    public UnsignedByte(short unsignedByteAsShort){
        this.value = unsignedByteAsShort;
        if(this.value > 255 || this.value < 0)
            throw new RuntimeException("Unsigned byte not within valid range");
    }

    public UnsignedByte(int unsignedByteAsInt){
        //TODO is this necessary???
        this.value = (short) unsignedByteAsInt;
        if(this.value > 255 || this.value < 0)
            throw new RuntimeException("Unsigned byte not within valid range");
    }

    public UnsignedByte(byte defaultByte){
        this.value = (short)(defaultByte & 0xff);
    }

    public UnsignedByte add(UnsignedByte unsignedByte){
        return new UnsignedByte((this.value + unsignedByte.value) % 256);
    }

    public UnsignedByte sub(UnsignedByte unsignedByte){
        int val = this.value - unsignedByte.value;
        if(val < 0){
            val += 256;
        }

        return new UnsignedByte(val);
    }
}
