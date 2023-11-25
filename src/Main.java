import ImageProcessingAlgorithms.MedianFilter;
import ParallelImageProcessing.*;
import PngInput.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.zip.CRC32;

public class Main {
    public static void main(String[] args) {
        // Create a JFrame
        JFrame frame = new JFrame("Image Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.getContentPane().setBackground(Color.BLACK);

        // Create a JLabel to display the image
        JLabel label = new JLabel();

        // Load the image
        try {
            Deque<ImageProcessingContext> images = new ArrayDeque<>();
            /*images.add(new ImageProcessingContext(new File("salt_and_pepper.png")));
            images.add(new ImageProcessingContext(new File("test.png")));
            images.add(new ImageProcessingContext(new File("test2.png")));
            images.add(new ImageProcessingContext(new File("test3.png")));
            images.add(new ImageProcessingContext(new File("test4.png")));*/

            File bulkInputFolder = new File("bulk_input");
            for(File image : bulkInputFolder.listFiles()){
                images.add(new ImageProcessingContext(image, 15));
            }
            ImageProcessingScheduler scheduler = new ImageProcessingScheduler(images, 6, 10000, 60, new File("output"));
            scheduler.start();
            /*
            TilingContext tilingContext = new TilingContext(200, 1, new PngLoader(imageFile));
            ImageTile originalTile = tilingContext.getNextTile();
            originalTile = tilingContext.getNextTile();
            ProcessingTask pt = new ProcessingTask(originalTile, new MedianFilter(originalTile, MedianFilter.KERNEL_SIZE.THREE));
            pt.run();
            Image image = pt.algorithm.getOutput().getAsImage();
            label.setIcon(new ImageIcon(image)); */
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add the label to the frame
        frame.add(label);

        // Center the frame on the screen
        frame.setLocationRelativeTo(null);

        // Make the frame visible
        frame.setVisible(true);
    }
}