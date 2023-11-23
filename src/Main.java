import ImageProcessingAlgorithms.MedianFilter;
import ParallelImageProcessing.*;
import PngInput.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

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
            Queue<ImageProcessingContext> images = new ArrayDeque<>();
            images.add(new ImageProcessingContext(new File("test_output_output.png")));
            //images.add(new ImageProcessingContext(new File("test.png")));
            //images.add(new ImageProcessingContext(new File("test2.png")));
            ImageProcessingScheduler scheduler = new ImageProcessingScheduler(images);
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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Add the label to the frame
        frame.add(label);

        // Center the frame on the screen
        frame.setLocationRelativeTo(null);

        // Make the frame visible
        frame.setVisible(true);
    }
}