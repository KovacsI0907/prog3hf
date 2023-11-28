package gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

/**
 * Az algoritmus futása közben logoáshoz használt JTextPane.
 * Rendelkezik loglást segítő függvényekkel.
 */

public class Logger extends JTextPane {
    public Logger() {
        setEditable(false);
        setPreferredSize(new Dimension(getPreferredSize().width, 100));
    }

    /**
     * Új sor írása a logba fekete színnel
     * @param message A kiírandó üzenet
     */
    public void log(String message) {
        appendMessage(message, null);
    }

    /**
     * Új sor írása a logba piros színnel
     * @param message A kiírandó üzenet
     */
    public void logRed(String message) {
        appendMessage(message, Color.RED);
    }

    /**
     * Új sor írása a logba zöld színnel
     * @param message A kiírandó üzenet
     */
    public void logGreen(String message) {
        appendMessage(message, Color.GREEN);
    }

    /**
     * Üzenet hozzáfüzése az edigiekhez
     * @param message Az üzenet
     * @param color Az üzenet színe
     */
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