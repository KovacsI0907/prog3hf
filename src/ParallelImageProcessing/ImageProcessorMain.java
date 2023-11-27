package ParallelImageProcessing;

import gui.ApplicationFrame;

import javax.swing.*;

public class ImageProcessorMain {
    public static void main(String[] args) {
        JFrame frame = new ApplicationFrame();
        frame.setVisible(true);
        System.out.println(Runtime.getRuntime().maxMemory()/1024/1024);
    }
}
