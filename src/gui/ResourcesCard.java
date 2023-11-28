package gui;

import ParallelImageProcessing.ImageProcessingContext;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * A memória limit és szál szám választó felület JPanel-je
 */
public class ResourcesCard extends JPanel implements ActionListener, InputVerifier {

    JButton backButton = new JButton("Back");
    JButton saveButton = new JButton("Save");
    JPanel cardsPanel;

    JTextField memoryField;
    JTextField threadsField;
    Logger logger;

    FinalizeCard finalizeCard;

    JLabel errorLabel = new JLabel("");
    public ResourcesCard(JPanel cardsPanel, FinalizeCard finalizeCard, Logger logger){
        super(new BorderLayout());

        this.logger = logger;
        this.finalizeCard = finalizeCard;

        this.memoryField = new JTextField(UserPreferences.getInstance().memorySize + "", 10);
        memoryField.getDocument().addDocumentListener(new VerifierDocumentListener(this));
        memoryField.setHorizontalAlignment(SwingConstants.CENTER);

        this.threadsField = new JTextField(UserPreferences.getInstance().threadCount + "", 10);
        threadsField.getDocument().addDocumentListener(new VerifierDocumentListener(this));
        threadsField.setHorizontalAlignment(SwingConstants.CENTER);

        this.cardsPanel = cardsPanel;
        backButton.addActionListener(this);
        saveButton.addActionListener(this);

        JLabel mainLabel = new JLabel("Set resource limits", JLabel.CENTER);
        JLabel threadLabel = new JLabel("Maximum threads:");
        JLabel threadLabel2 = new JLabel("(should not be larger than number of logical cores in this machine):");
        JLabel memoryLabel = new JLabel("Maximum memory (MB):");
        JLabel memoryLabel2 = new JLabel("(Don't forget to set JVM heap size (-Xmx)");

        Font plainFont = new Font(mainLabel.getFont().getName(), Font.PLAIN, mainLabel.getFont().getSize());
        threadLabel2.setFont(plainFont);
        memoryLabel2.setFont(plainFont);

        JPanel middleBox = new JPanel();
        middleBox.setLayout(new BoxLayout(middleBox, BoxLayout.Y_AXIS));

        middleBox.add(threadLabel);
        middleBox.add(threadLabel2);
        JPanel fieldPanel1 = new JPanel();
        middleBox.add(fieldPanel1);
        fieldPanel1.add(threadsField);

        middleBox.add(memoryLabel);
        middleBox.add(memoryLabel2);
        JPanel fieldPanel2 = new JPanel();
        middleBox.add(fieldPanel2);
        fieldPanel2.add(memoryField);
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
            up = new UserPreferences(Integer.parseInt(threadsField.getText()), Integer.parseInt(memoryField.getText()));
            try {
                UserPreferences.savePreferences(up);
                UserPreferences.setInstance(up);
                logger.logGreen("Preferences saved.");
            } catch (IOException e) {
                logger.logRed("Error saving preference file! Changes will be lost after restart.");
            }

            CardLayout cl = (CardLayout) cardsPanel.getLayout();
            cl.show(cardsPanel, GuiConstants.CHOOSE_INPUT_CARD);

            finalizeCard.updateMemorySizeError();
        }
    }

    @Override
    public boolean verify() {
        try{
            int mem = Integer.parseInt(memoryField.getText());
            int thread = Integer.parseInt(threadsField.getText());

            if(mem>=64 && mem < 1048576 && thread >= 1 && thread <= 512){
                return mem*1.1 > ImageProcessingContext.memoryRequiredForSmoothOperation(thread);
            }
        }catch (Exception ignored){
            return false;
        }
        return false;
    }

    @Override
    public void action() {
        saveButton.setEnabled(true);
        finalizeCard.nextPrevPanel.nextButton.setEnabled(true);
        errorLabel.setText("");
        finalizeCard.updateMemorySizeError();
    }

    @Override
    public void falseAction() {
        saveButton.setEnabled(false);
        finalizeCard.nextPrevPanel.nextButton.setEnabled(false);
        errorLabel.setText("Memory is not enough for this many cores");
        finalizeCard.updateMemorySizeError();
    }
}
