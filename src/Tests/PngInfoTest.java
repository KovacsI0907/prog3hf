package Tests;

import PngInput.PngInfo;
import org.junit.Test;

import static org.junit.Assert.*;

public class PngInfoTest {

    @Test
    public void testPngInfoConstructor() {
        PngInfo pngInfo = new PngInfo(100, 200, (byte) 8, (byte) 6, (byte) 0, (byte) 0, (byte) 0);

        assertEquals(100, pngInfo.width);
        assertEquals(200, pngInfo.height);
        assertEquals(8, pngInfo.bitDepth);
        assertEquals(6, pngInfo.colorType);
        assertEquals(0, pngInfo.compressionMethod);
        assertEquals(0, pngInfo.filterMethod);
        assertEquals(0, pngInfo.interlaceMethod);
        assertEquals(4, pngInfo.numChannels); // Check based on colorType value
    }

    @Test
    public void testPngInfoToString() {
        PngInfo pngInfo = new PngInfo(100, 200, (byte) 8, (byte) 6, (byte) 0, (byte) 0, (byte) 0);

        String expectedString = "width:       100\n" +
                "height:      200\n" +
                "bit-depth:   8\n" +
                "color-type:  6\n" +
                "compression: 0\n" +
                "filter:      0\n" +
                "interlace:   0\n";

        assertEquals(expectedString, pngInfo.toString());
    }

    @Test
    public void testValidateValid() {
        PngInfo pngInfo = new PngInfo(100, 200, (byte) 8, (byte) 6, (byte) 0, (byte) 0, (byte) 0);

        assertTrue(pngInfo.validate());
    }

    @Test
    public void testValidateInvalidWidth() {
        PngInfo pngInfo = new PngInfo(0, 200, (byte) 8, (byte) 6, (byte) 0, (byte) 0, (byte) 0);

        assertFalse(pngInfo.validate());
    }

    @Test
    public void testValidateInvalidCompression() {
        PngInfo pngInfo = new PngInfo(100, 200, (byte) 8, (byte) 6, (byte) 1, (byte) 0, (byte) 0);

        assertFalse(pngInfo.validate());
    }

    @Test
    public void testValidateInvalidInterlace() {
        PngInfo pngInfo = new PngInfo(100, 200, (byte) 8, (byte) 6, (byte) 0, (byte) 0, (byte) 1);

        assertFalse(pngInfo.validate());
    }

    @Test
    public void testValidateInvalidColorTypeBitDepthCombination() {
        PngInfo pngInfo = new PngInfo(100, 200, (byte) 8, (byte) 3, (byte) 0, (byte) 0, (byte) 0);

        assertFalse(pngInfo.validate());
    }
}
