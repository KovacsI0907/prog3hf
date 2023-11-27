package gui;

import ParallelImageProcessing.ImageProcessingContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class FinalizeCard extends JPanel implements ActionListener{

    NextBackPanel nextPrevPanel;
    JTextArea configTextArea;

    private ChooseInputCard chooseInputCard = null;
    private ChooseAlgorithmCard chooseAlgorithmCard = null;
    private AlgoStatusCard algoStatusCard;
    JLabel errorLabel = new JLabel("");
    JLabel errorLabel2 = new JLabel("");
    Logger logger;
    public FinalizeCard(JPanel cardsContainer, AlgoStatusCard algoStatusCard, Logger logger) {
        super(new BorderLayout());

        this.logger = logger;
        this.algoStatusCard = algoStatusCard;

        nextPrevPanel = new NextBackPanel(cardsContainer, GuiConstants.PROCESSING_STATUS_CARD, GuiConstants.CHOOSE_ALGORITHM_CARD);
        nextPrevPanel.nextButton.setEnabled(hasEnoughMemory());
        nextPrevPanel.nextButton.setText("Start");
        nextPrevPanel.nextButton.addActionListener(this);

        //Create labels for info
        JLabel mainLabel = new JLabel("Review configuration", JLabel.CENTER);
        mainLabel.setBorder(new EmptyBorder(0, 0, 30, 0));
        configTextArea = new JTextArea( 10, 30);
        configTextArea.setBackground(new Color(230,230,230,255));
        configTextArea.setEditable(false);
        this.add(mainLabel, BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.add(configTextArea);
        this.add(panel, BorderLayout.CENTER);
        this.add(nextPrevPanel, BorderLayout.SOUTH);
    }

    public void updateConfigText() {
        try{
            String text = "The algorithm will run on:\n";
            if(chooseInputCard.chosenFile.isDirectory()){
                text += "All .png files in " + chooseInputCard.chosenFile.getParentFile().getName() + "/" + chooseInputCard.chosenFile.getName() + "\n\n";
            }else{
                text += chooseInputCard.chosenFile.getName() + "\n\n";
            }
            text += chooseAlgorithmCard.getParameters().toString();
            configTextArea.setText(text);
        }catch (Exception e){
            configTextArea.setText("Some inputs might be invalid");
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == nextPrevPanel.nextButton) {
            Deque<ImageProcessingContext> imageProcessingContexts = new ArrayDeque<>();
            File input = chooseInputCard.chosenFile;
            if(input.isDirectory()){
                File[] files = input.listFiles();
                for(File file : files){
                    if(file.getName().endsWith(".png")){
                        try {
                            imageProcessingContexts.add(new ImageProcessingContext(file, chooseAlgorithmCard.getParameters().paddingSizeNecessary));
                        } catch (IOException ex) {
                            logger.logRed("Error opening " + file.getName());
                        }
                    }
                }
            }else{
                try {
                    imageProcessingContexts.add(new ImageProcessingContext(input, chooseAlgorithmCard.getParameters().paddingSizeNecessary));
                } catch (IOException ex) {
                    logger.logRed("Error opening " + input.getName() + ":");
                    logger.logRed(ex.getMessage());
                }
            }

            algoStatusCard.startScheduler(imageProcessingContexts, chooseAlgorithmCard.getParameters(), chooseAlgorithmCard.getAlgorithmID());
        }
    }

    public void setChooseInputCard(ChooseInputCard chooseInputCard) {
        this.chooseInputCard = chooseInputCard;
    }

    public void setChooseAlgorithmCard(ChooseAlgorithmCard chooseAlgorithmCard) {
        this.chooseAlgorithmCard = chooseAlgorithmCard;
    }

    private boolean hasEnoughMemory() {
        return Runtime.getRuntime().maxMemory()/1024/1024 > UserPreferences.getInstance().memorySize;
    }

    public void updateMemorySizeError() {
        nextPrevPanel.nextButton.setEnabled(hasEnoughMemory());
    }
}
