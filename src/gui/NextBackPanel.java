package gui;

import javax.swing.*;
import java.awt.*;

public class NextBackPanel extends JPanel {
    public final JButton nextButton;
    public final JButton backButton;

    public NextBackPanel(JPanel cardsPanel, String nextCardName, String prevCardName) {
        nextButton = new JButton("Next");
        backButton = new JButton("Back");
        nextButton.addActionListener((e) -> {
            CardLayout cl = (CardLayout) cardsPanel.getLayout();
            cl.show(cardsPanel, nextCardName);
        });
        backButton.addActionListener((e) -> {
            CardLayout cl = (CardLayout) cardsPanel.getLayout();
            cl.show(cardsPanel, prevCardName);
        });
        this.add(backButton);
        this.add(nextButton);
    }
}
