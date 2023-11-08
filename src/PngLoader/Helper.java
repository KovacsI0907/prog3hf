package PngLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class Helper {

    static void readExactlyNUBytes(InputStream is, int n, UnsignedByte[] destBuf, int offset) throws IOException {
        //TODO optimize for memory usage
        byte[] bytes = new byte[n];
        readExactlyNBytes(is, n, bytes, 0);
        for(int i = 0;i<bytes.length;i++){
            destBuf[offset + i] = new UnsignedByte(bytes[i]);
        }
    }

    //converts a uint32(represented by a byte[4]) to a java long
    static long uint32BytesToLong(byte[] byteArr, int offset) {
        if(offset + 4 > byteArr.length)
            return -1;
        long result = 0L;

        //read 32bit unsigned int into java long
        for (int i = 0; i < 4; i++) {
            result = (result << 8) | (byteArr[i + offset] & 0xFF);
        }

        return result;
    }

    public static byte[] readExactlyNBytes(InputStream is, int n) throws IOException {
        byte[] buffer = new byte[n];
        if(is.readNBytes(buffer, 0, buffer.length) != buffer.length)
            throw new IOException("Error reading from stream: read less bytes than expected");
        return buffer;
    }

    public static void readExactlyNBytes(InputStream is, int n, byte[] destBuf, int offset) throws IOException {
        if(is.readNBytes(destBuf, offset, n) != n) {
            throw new IOException("Error reading from stream: read less bytes than expected");
        }
    }

    public static long readUint32(InputStream is) throws IOException {
        return uint32BytesToLong(readExactlyNBytes(is, 4), 0);
    }

    public static String getChunkType(byte[] arr) throws RuntimeException{
        if(arr.length != 4)
            throw new RuntimeException("Invalid chunk type length");
        return new String(arr, StandardCharsets.US_ASCII);
    }

    public static String readChunkType(InputStream is) throws IOException {
        return getChunkType(readExactlyNBytes(is, 4));
    }

    public static String byteToBinaryString(byte b){
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }
}
