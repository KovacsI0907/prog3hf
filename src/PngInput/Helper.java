package PngInput;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Segédosztály olyan beolvasó és konverziós függvényeknek, amik a package többi osztályának is kellenek
 */
public final class Helper {

    /**
     * Beolvas n darab UnsignedByte-ot, ha nem sikerül, akkor kivételt dob
     * @param is A stream amiről olvas
     * @param n A beolvasandó byteok száma
     * @param destBuf A puffer amibe a beolvasott elemek mennek
     * @param offset Eltolás a pufferben
     * @throws IOException Ha a folyamban kevesebb byte van mint n
     */
    static void readExactlyNUBytes(InputStream is, int n, UnsignedByte[] destBuf, int offset) throws IOException {
        byte[] bytes = new byte[n];
        readExactlyNBytes(is, n, bytes, 0);
        for(int i = 0;i<bytes.length;i++){
            destBuf[offset + i] = new UnsignedByte(bytes[i]);
        }
    }

    /**
     * Átalakít egy 4 byteból álló 32 bites előjel nélküli integert Java longba
     * @param byteArr bemeneti puffer
     * @param offset eltolás a pufferben
     * @return az érték egy long változóban
     */
    static long uint32BytesToLong(byte[] byteArr, int offset) {
        if(offset + 4 > byteArr.length){
            throw new IllegalArgumentException();
        }

        long result = 0L;
        //read 32bit unsigned int into java long
        for (int i = 0; i < 4; i++) {
            result = (result << 8) | (byteArr[i + offset] & 0xFF);
        }

        return result;
    }

    /**
     * Beolvas n darab byteot, ha nem sikerül, akkor kivételt dob
     * @param is A stream amiről olvas
     * @param n A beolvasandó byteok száma
     * @return A beolvaasott byteok tömbje
     * @throws IOException Ha kevesebb mint N byteot sikerült beolvasni
     */
    public static byte[] readExactlyNBytes(InputStream is, int n) throws IOException {
        byte[] buffer = new byte[n];
        if(is.readNBytes(buffer, 0, buffer.length) != buffer.length)
            throw new IOException("Error reading from stream: read less bytes than expected");
        return buffer;
    }

    /**
     * Beolvas n darab byte-ot, ha nem sikerül, akkor kivételt dob
     * @param is A stream amiről olvas
     * @param n A beolvasandó byteok száma
     * @param destBuf A puffer amibe a beolvasott elemek mennek
     * @param offset Eltolás a pufferben
     * @throws IOException Ha a folyamban kevesebb byte van mint n
     */
    public static void readExactlyNBytes(InputStream is, int n, byte[] destBuf, int offset) throws IOException {
        if(is.readNBytes(destBuf, offset, n) != n) {
            throw new IOException("Error reading from stream: read less bytes than expected");
        }
    }

    /**
     * Beolvas 4 bájtot egy longba
     * @param is A stream amiről olvas
     * @return A 4 bájt értéke egy longban
     * @throws IOException Ha nincs 4 bájtnyi adat a streamben
     */
    public static long readUint32(InputStream is) throws IOException {
        return uint32BytesToLong(readExactlyNBytes(is, 4), 0);
    }

    /**
     * Beolvas 4 bájtot és átalakítja stringgé
     * @param is A stream amiről olvas
     * @return A 4 bájt stringként (ASCII)
     * @throws IOException Ha nincs 4 bájtnyi adat a streamben
     */
    public static String readChunkType(InputStream is) throws IOException {
        return new String(readExactlyNBytes(is, 4), StandardCharsets.US_ASCII);
    }
}
