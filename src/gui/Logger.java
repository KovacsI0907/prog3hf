package gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class Logger extends JTextPane {
    public Logger() {
        setEditable(false);
        setPreferredSize(new Dimension(getPreferredSize().width, 100));
    }

    public void log(String message) {
        appendMessage(message, null);
    }

    public void logRed(String message) {
        appendMessage(message, Color.RED);
    }

    public void logGreen(String message) {
        appendMessage(message, Color.GREEN);
    }

    private void appendMessage(String message, Color color) {
        SimpleAttributeSet attributes = new SimpleAttributeSet();

        if (color != null) {
            StyleConstants.setForeground(attributes, color);
        }

        try {
            getStyledDocument().insertString(getStyledDocument().getLength(), message + "\n", attributes);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
}