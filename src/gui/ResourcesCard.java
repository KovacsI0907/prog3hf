package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ResourcesCard extends JPanel implements ActionListener {

    JButton backButton = new JButton("Back");
    JButton saveButton = new JButton("Save");
    JPanel cardsPanel;
    JSpinner threadsSpinner;
    JSpinner memorySpinner;
    Logger logger;
    JFrame parent;
    public ResourcesCard(JPanel cardsPanel, Logger logger){
        super(new BorderLayout());

        this.parent = parent;
        this.logger = logger;

        this.cardsPanel = cardsPanel;
        backButton.addActionListener(this);
        saveButton.addActionListener(this);

        SpinnerModel threadSpinnerModel = new SpinnerNumberModel(4, 1, 512, 1);
        threadsSpinner = new JSpinner(threadSpinnerModel);

        SpinnerModel memorySpinnerModel = new SpinnerNumberModel(1024, 256, (int)(0.8*Runtime.getRuntime().maxMemory()/1024/1024), 32);
        memorySpinner = new JSpinner(memorySpinnerModel);

        JLabel mainLabel = new JLabel("Set resource limits", JLabel.CENTER);
        JLabel threadLabel = new JLabel("Maximum threads:");
        JLabel threadLabel2 = new JLabel("(should not be larger than number of logical cores in this machine):");
        JLabel memoryLabel = new JLabel("Maximum memory (MB):");
        JLabel memoryLabel2 = new JLabel("(capped at 80% of max JVM heap size)");

        Font plainFont = new Font(mainLabel.getFont().getName(), Font.PLAIN, mainLabel.getFont().getSize());
        threadLabel2.setFont(plainFont);
        memoryLabel2.setFont(plainFont);

        JPanel middleBox = new JPanel();
        middleBox.setLayout(new BoxLayout(middleBox, BoxLayout.Y_AXIS));

        middleBox.add(threadLabel);
        middleBox.add(threadLabel2);
        JPanel spinnerPanel1 = new JPanel();
        middleBox.add(spinnerPanel1);
        spinnerPanel1.add(threadsSpinner);
        middleBox.add(memoryLabel);
        middleBox.add(memoryLabel2);
        JPanel spinnerPanel2 = new JPanel();
        middleBox.add(spinnerPanel2);
        spinnerPanel2.add(memorySpinner);

        JPanel lowerBox = new JPanel();
        lowerBox.add(backButton);
        lowerBox.add(saveButton);

        this.add(mainLabel, BorderLayout.NORTH);
        this.add(middleBox, BorderLayout.CENTER);
        this.add(lowerBox, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(actionEvent.getSource() == backButton){
            CardLayout cl = (CardLayout) cardsPanel.getLayout();
            cl.show(cardsPanel, GuiConstants.CHOOSE_INPUT_CARD);
        }

        if(actionEvent.getSource() == saveButton){
            UserPreferences up;
            up = new UserPreferences((int)threadsSpinner.getValue(), (int)memorySpinner.getValue());
            try {
                UserPreferences.savePreferences(up);
                logger.logGreen("Preferences saved.");
            } catch (IOException e) {
                logger.logRed("Error saving preference file! Changes will be lost after restart.");
            }finally {
                UserPreferences.setInstance(up);
            }

            CardLayout cl = (CardLayout) cardsPanel.getLayout();
            cl.show(cardsPanel, GuiConstants.CHOOSE_INPUT_CARD);
        }
    }
}
