import Algorithms.MedianFilter;
import PngLoader.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

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
            File imageFile = new File("salt_and_pepper.png"); // Replace with the path to your image
            TilingContext tilingContext = new TilingContext(200, 0, new PngLoader(imageFile));
            ImageTile originalTile = tilingContext.getNextTile();
            originalTile = tilingContext.getNextTile();
            originalTile = tilingContext.getNextTile();
            //MedianFilter mf1 = new MedianFilter(originalTile, MedianFilter.KERNEL_SIZE.THREExTHREE);
            //mf1.run();
            Image image = originalTile.getAsImage(); //mf1.getOutput().getAsImage();
            label.setIcon(new ImageIcon(image));
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