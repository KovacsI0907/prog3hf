package gui;

import ImageProcessingAlgorithms.SobelParams;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;

public class SobelParamCard extends JPanel implements InputVerifier{
    public final JTextField clampingThreshold;
    public SobelParamCard() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.clampingThreshold = new JTextField();
        clampingThreshold.setColumns(10);
        clampingThreshold.setHorizontalAlignment(SwingConstants.CENTER);


        JPanel panel = new JPanel();

        JLabel comment1 = new JLabel("Clamping threshold (integer 0-255):");

        Font plainFont = new Font(comment1.getFont().getName(), Font.PLAIN, comment1.getFont().getSize());

        comment1.setFont(plainFont);

        comment1.setAlignmentX(CENTER_ALIGNMENT);

        this.add(comment1);
        this.add(panel);
        panel.add(clampingThreshold);
    }

    public SobelParams getParams() {
        if(!verify()){
            throw new IllegalArgumentException("Invalid clamping threshold");
        }

        int i = Integer.parseInt(clampingThreshold.getText());
        return new SobelParams(i);
    }

    @Override
    public boolean verify() {
        try{
            int i = Integer.parseInt(clampingThreshold.getText());
            return i>=0 && i<=255;
        }catch (Exception ignored){
        }

        return false;
    }

    @Override
    public void action() {
        //no action needed here
    }

    @Override
    public void falseAction() {
        //no need for action here
    }
}