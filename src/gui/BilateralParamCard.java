package gui;

import ImageProcessingAlgorithms.BilateralFilterParams;
import ImageProcessingAlgorithms.MedianFilterParams;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

public class BilateralParamCard extends JPanel implements InputVerifier{
    public final JTextField spatialSigmaField;
    public final JTextField intensitySigmaField;
    public final JTextField kernelSizeField;
    public BilateralParamCard() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.spatialSigmaField = new JTextField();
        this.intensitySigmaField = new JTextField();
        this.kernelSizeField = new JTextField();

        spatialSigmaField.setColumns(10);
        intensitySigmaField.setColumns(10);
        kernelSizeField.setColumns(10);

        spatialSigmaField.setHorizontalAlignment(SwingConstants.CENTER);
        intensitySigmaField.setHorizontalAlignment(SwingConstants.CENTER);
        kernelSizeField.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel comment1 = new JLabel("Kernel Size (odd integer, greater than 1, smaller than 51):");
        JLabel comment2 = new JLabel("Spatial Normal Deviation (positive real):");
        JLabel comment3 = new JLabel("Intensity Normal Deviation (positive real):");

        Font plainFont = new Font(comment1.getFont().getName(), Font.PLAIN, comment1.getFont().getSize());

        comment1.setFont(plainFont);
        comment2.setFont(plainFont);
        comment3.setFont(plainFont);

        comment1.setAlignmentX(CENTER_ALIGNMENT);
        comment2.setAlignmentX(CENTER_ALIGNMENT);
        comment3.setAlignmentX(CENTER_ALIGNMENT);

        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel();

        panel1.add(kernelSizeField);
        panel2.add(spatialSigmaField);
        panel3.add(intensitySigmaField);

        this.add(comment1);
        this.add(panel1);
        this.add(comment2);
        this.add(panel2);
        this.add(comment3);
        this.add(panel3);
    }

    public BilateralFilterParams getParams() {
        if(!verify()){
            throw new IllegalArgumentException("Invalid parameters");
        }

        int ks = Integer.parseInt(kernelSizeField.getText());
        double ss = Double.parseDouble(spatialSigmaField.getText());
        double is = Double.parseDouble(intensitySigmaField.getText());

        return new BilateralFilterParams(is, ss, ks);
    }

    @Override
    public boolean verify() {
        try{
            int ks = Integer.parseInt(kernelSizeField.getText());
            double ss = Double.parseDouble(spatialSigmaField.getText());
            double is = Double.parseDouble(intensitySigmaField.getText());

            return ks >= 3 && ks<=50 && ks % 2 == 1
                    && ss > 0
                    && is > 0;
        }catch (Exception ignored){
        }

        return false;
    }

    @Override
    public void action() {
        //no need for action here
    }

    @Override
    public void falseAction() {
        //no need for action here
    }
}
