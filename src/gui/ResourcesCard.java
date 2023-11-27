package gui;

import ParallelImageProcessing.ImageProcessingContext;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ResourcesCard extends JPanel implements ActionListener, ChangeListener {

    JButton backButton = new JButton("Back");
    JButton saveButton = new JButton("Save");
    JPanel cardsPanel;
    JSpinner threadsSpinner;
    JSpinner memorySpinner;
    Logger logger;

    FinalizeCard finalizeCard;

    JLabel errorLabel = new JLabel("");
    public ResourcesCard(JPanel cardsPanel, FinalizeCard finalizeCard, Logger logger){
        super(new BorderLayout());

        this.logger = logger;
        this.finalizeCard = finalizeCard;

        this.cardsPanel = cardsPanel;
        backButton.addActionListener(this);
        saveButton.addActionListener(this);

        SpinnerModel threadSpinnerModel = new SpinnerNumberModel(UserPreferences.getInstance().threadCount, 1, 512, 1);
        threadsSpinner = new JSpinner(threadSpinnerModel);
        threadsSpinner.addChangeListener(this);

        SpinnerModel memorySpinnerModel = new SpinnerNumberModel(UserPreferences.getInstance().memorySize, 64, (int)(0.8*Runtime.getRuntime().maxMemory()/1024/1024), 32);
        memorySpinner = new JSpinner(memorySpinnerModel);
        memorySpinner.addChangeListener(this);

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
        middleBox.add(errorLabel);

        JPanel lowerBox = new JPanel();
        lowerBox.add(backButton);
        lowerBox.add(saveButton);

        this.add(mainLabel, BorderLayout.NORTH);
        this.add(middleBox, BorderLayout.CENTER);
        this.add(lowerBox, BorderLayout.SOUTH);

        saveButton.setEnabled(verify());
        errorLabel.setForeground(Color.RED);
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

    public boolean verify() {
        return ImageProcessingContext.memoryRequiredForSmoothOperation((int)threadsSpinner.getValue()) < (int)memorySpinner.getValue();
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        if(verify()){
            saveButton.setEnabled(true);
            errorLabel.setText("");
        }else{
            saveButton.setEnabled(false);
            errorLabel.setText("Memory limit too low for this many threads");
        }

        finalizeCard.updateMemorySizeError();
    }
}
