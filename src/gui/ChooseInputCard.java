package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Az input kiválasztására szolgáló panel
 */
public class ChooseInputCard extends JPanel implements ActionListener, InputVerifier {
    JLabel selectedLocationLabel = new JLabel("");
    JButton selectButton = new JButton("Select");
    JFileChooser fileChooser = new JFileChooser();

    NextBackPanel nextPrevPanel;

    public File chosenFile = null;

    FinalizeCard finalizeCard;

    public ChooseInputCard(JPanel cardsContainer, FinalizeCard finalizeCard) {
        super(new BorderLayout());

        this.finalizeCard = finalizeCard;

        nextPrevPanel = new NextBackPanel(cardsContainer, GuiConstants.CHOOSE_ALGORITHM_CARD, null);

        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(".png");
            }

            @Override
            public String getDescription() {
                return "PNG Files and Directories";
            }
        });

        //Create labels for info
        JLabel mainLabel = new JLabel("Choose files to process", JLabel.CENTER);
        this.add(mainLabel, BorderLayout.NORTH);
        Font plainFont = new Font(mainLabel.getFont().getName(), Font.PLAIN, mainLabel.getFont().getSize());
        JPanel middleContainer = new JPanel();
        middleContainer.setLayout(new BoxLayout(middleContainer, BoxLayout.Y_AXIS));
        JLabel label1 = new JLabel("If you want to process a single file, choose any png.");
        label1.setFont(plainFont);
        label1.setBorder(new EmptyBorder(10,0,0,0));
        JLabel label2 = new JLabel("If you want to process all pngs in a folder, choose a folder.");
        label2.setFont(plainFont);
        label2.setBorder(new EmptyBorder(0,0,20,0));
        label1.setAlignmentX(CENTER_ALIGNMENT);
        label2.setAlignmentX(CENTER_ALIGNMENT);
        selectedLocationLabel.setAlignmentX(CENTER_ALIGNMENT);
        selectButton.setAlignmentX(CENTER_ALIGNMENT);
        middleContainer.add(label1);
        middleContainer.add(label2);
        middleContainer.add(selectButton);
        middleContainer.add(selectedLocationLabel);
        this.add(middleContainer, BorderLayout.CENTER);
        selectedLocationLabel.setAlignmentX(CENTER_ALIGNMENT);
        this.add(nextPrevPanel, BorderLayout.SOUTH);
        selectButton.addActionListener(this);
        nextPrevPanel.nextButton.setEnabled(false);
        nextPrevPanel.backButton.setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {

        //Handle open button action.
        if (e.getSource() == selectButton) {
            int returnVal = fileChooser.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                selectedLocationLabel.setText("Selected: " + file.getParentFile().getName() + "/" + file.getName());
                chosenFile = file;
                if(verify()){
                    action();
                }else {
                    falseAction();
                }
            } else {
                selectedLocationLabel.setText("Selection unsuccesful");
            }
        }
    }

    @Override
    public boolean verify() {
        if(chosenFile == null){
            return false;
        }
        if (chosenFile.isFile()) {
            String fileName = chosenFile.getName();
            return fileName.toLowerCase().endsWith(".png");
        }else{
            return chosenFile.isDirectory();
        }
    }

    @Override
    public void action() {
        nextPrevPanel.nextButton.setEnabled(true);
        finalizeCard.updateConfigText();
    }

    @Override
    public void falseAction() {
        nextPrevPanel.nextButton.setEnabled(false);
        finalizeCard.updateConfigText();
    }
}
