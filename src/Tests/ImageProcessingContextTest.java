package Tests;

import ParallelImageProcessing.ImageProcessingContext;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class ImageProcessingContextTest {

    @Test
    public void testImageProcessingContextConstructor() throws IOException {
        File validImageFile = new File("src/Tests/Resources/test.png");
        int paddingSize = 5;

        ImageProcessingContext context = new ImageProcessingContext(validImageFile, paddingSize);

        assertNotNull(context.tilingContext);
        assertEquals(validImageFile, context.imageFile);
        assertTrue(context.imageWidth > 0);
        assertTrue(context.imageHeight > 0);
    }

    @Test(expected = RuntimeException.class)
    public void testImageProcessingContextConstructorInvalidSize() throws IOException {
        // Create an image with size less than paddingSize + 1 to trigger RuntimeException
        File invalidImageFile = new File("src/Tests/Resources/test.png");
        //instead of a small image i'm just going to use a huge padding
        int paddingSize = 2000;

        new ImageProcessingContext(invalidImageFile, paddingSize);
    }

    @Test(expected = RuntimeException.class)
    public void testImageProcessingContextConstructorCorruptedFile() throws IOException {
        // Provide a corrupted image file to test how it handles IOException
        File corruptedImageFile = new File("src/Tests/Resources/test_corrupted.png");
        int paddingSize = 5;

        new ImageProcessingContext(corruptedImageFile, paddingSize);
    }

    @Test
    public void testMemoryRequiredForSmoothOperation() {
        int numThreads = 4;
        int minMemory = ImageProcessingContext.memoryRequiredForSmoothOperation(numThreads);

        assertTrue(minMemory > 64);
    }

    @Test
    public void testCanLoadNextTile() {
        assertTrue(ImageProcessingContext.canLoadNextTile());
    }
}
