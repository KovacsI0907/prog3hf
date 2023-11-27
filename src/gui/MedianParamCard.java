package gui;

import ImageProcessingAlgorithms.MedianFilterParams;
import ImageProcessingAlgorithms.SobelParams;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.InvalidParameterException;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.EventListener;

public class MedianParamCard extends JPanel implements InputVerifier {
    public final JTextField kernelSizeField;
    public MedianParamCard() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.kernelSizeField = new JTextField();

        kernelSizeField.setColumns(10);

        JLabel comment1 = new JLabel("Kernel Size (odd integer, greater than 1, smaller than 51):");

        Font plainFont = new Font(comment1.getFont().getName(), Font.PLAIN, comment1.getFont().getSize());

        comment1.setFont(plainFont);

        comment1.setAlignmentX(CENTER_ALIGNMENT);

        this.add(comment1);

        JPanel panel = new JPanel();
        panel.add(kernelSizeField);
        this.add(panel);
    }

    public MedianFilterParams getParams() {
        if(!verify()){
            throw new IllegalArgumentException("Invalid kernel size");
        }

        int i = Integer.parseInt(kernelSizeField.getText());
        return new MedianFilterParams(i);
    }


    @Override
    public boolean verify() {
        try{
            int i = Integer.parseInt(kernelSizeField.getText());
            if(i>=3 && i<=50 && i%2==1){
                return true;
            }
        }catch (Exception ignored){

        }

        return false;
    }

    @Override
    public void action() {
        //no action required
    }

    @Override
    public void falseAction() {
        //no need for action here
    }
}
