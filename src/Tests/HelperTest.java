package Tests;

import PngInput.Helper;
import PngInput.UnsignedByte;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class HelperTest {

    @Test
    public void testReadExactlyNUBytes() throws IOException {
        byte[] testData = {1, 2, 3, 4};
        InputStream inputStream = new ByteArrayInputStream(testData);

        UnsignedByte[] result = new UnsignedByte[4];
        Helper.readExactlyNUBytes(inputStream, 4, result, 0);

        for (int i = 0; i < testData.length; i++) {
            assertEquals(new UnsignedByte(testData[i]), result[i]);
        }
    }

    @Test
    public void testUint32BytesToLong() {
        byte[] testData = {0, 0, 0, 1};
        long result = Helper.uint32BytesToLong(testData, 0);
        assertEquals(1L, result);
    }

    @Test
    public void testReadExactlyNBytes() throws IOException {
        byte[] testData = {1, 2, 3, 4};
        InputStream inputStream = new ByteArrayInputStream(testData);

        byte[] result = Helper.readExactlyNBytes(inputStream, 4);

        for (int i = 0; i < testData.length; i++) {
            assertEquals(testData[i], result[i]);
        }
    }

    @Test
    public void testReadUint32() throws IOException {
        byte[] testData = {0, 0, 0, 1};
        InputStream inputStream = new ByteArrayInputStream(testData);

        long result = Helper.readUint32(inputStream);

        assertEquals(1L, result);
    }

    @Test
    public void testReadChunkType() throws IOException {
        byte[] testData = "abcd".getBytes();
        InputStream inputStream = new ByteArrayInputStream(testData);

        String result = Helper.readChunkType(inputStream);

        assertEquals("abcd", result);
    }

    @Test
    public void testReadChunkTypeThrowsIOException() {
        byte[] testData = "abc".getBytes();
        InputStream inputStream = new ByteArrayInputStream(testData);

        assertThrows(IOException.class, () -> Helper.readChunkType(inputStream));
    }
}