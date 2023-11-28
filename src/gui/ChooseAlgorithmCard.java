package gui;

import ImageProcessingAlgorithms.*;
import ParallelImageProcessing.ImageTile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 * Az algoritmmus kiválasztására szolgáló panel, ez használja, a Param kártyákat is
 */
public class ChooseAlgorithmCard extends JPanel implements ActionListener, InputVerifier {
    String[] algorithms = new String[]{GuiConstants.BILATERAL_FILTER, GuiConstants.MEDIAN_FILTER, GuiConstants.SOBEL_OPERATOR};
    HashMap<String, JPanel> paramCards;
    JComboBox<String> algoList = new JComboBox<>(algorithms);
    CardLayout paramCardsLayout = new CardLayout();
    JPanel paramCardsPanel = new JPanel(paramCardsLayout);

    BilateralParamCard bilateralParamCard = new BilateralParamCard();
    MedianParamCard medianParamCard = new MedianParamCard();
    SobelParamCard sobelParamCard = new SobelParamCard();
    NextBackPanel nextPrevPanel;
    VerifierDocumentListener verifierDocumentListener;

    FinalizeCard finalizeCard;

    public ChooseAlgorithmCard(JPanel cardsContainer, FinalizeCard finalizeCard) {
        super(new BorderLayout());

        verifierDocumentListener = new VerifierDocumentListener(this);
        this.finalizeCard = finalizeCard;

        nextPrevPanel = new NextBackPanel(cardsContainer, GuiConstants.FINALIZE_CHOICES_CARD, GuiConstants.CHOOSE_INPUT_CARD);

        algoList.setSelectedIndex(0);
        algoList.addActionListener(this);
        paramCardsPanel.add(bilateralParamCard, GuiConstants.BILATERAL_FILTER);
        paramCardsPanel.add(medianParamCard, GuiConstants.MEDIAN_FILTER);
        paramCardsPanel.add(sobelParamCard, GuiConstants.SOBEL_OPERATOR);

        bilateralParamCard.intensitySigmaField.getDocument().addDocumentListener(verifierDocumentListener);
        bilateralParamCard.kernelSizeField.getDocument().addDocumentListener(verifierDocumentListener);
        bilateralParamCard.spatialSigmaField.getDocument().addDocumentListener(verifierDocumentListener);
        medianParamCard.kernelSizeField.getDocument().addDocumentListener(verifierDocumentListener);
        sobelParamCard.clampingThreshold.getDocument().addDocumentListener(verifierDocumentListener);

        paramCardsLayout.show(paramCardsPanel, (String)algoList.getSelectedItem());

        //Create labels for info
        JLabel mainLabel = new JLabel("Select Algorithm and parameters", JLabel.CENTER);
        this.add(mainLabel, BorderLayout.NORTH);
        Font plainFont = new Font(mainLabel.getFont().getName(), Font.PLAIN, mainLabel.getFont().getSize());
        JPanel middleContainer = new JPanel();
        middleContainer.setLayout(new BoxLayout(middleContainer, BoxLayout.Y_AXIS));
        JLabel comment1 = new JLabel("Choose an algorithm:");
        middleContainer.add(comment1);
        middleContainer.add(algoList);
        JLabel comment2 = new JLabel("Choose parameters:");
        middleContainer.add(comment2);
        middleContainer.add(paramCardsPanel);
        this.add(middleContainer, BorderLayout.CENTER);
        comment1.setAlignmentX(CENTER_ALIGNMENT);
        comment2.setAlignmentX(CENTER_ALIGNMENT);
        this.add(nextPrevPanel, BorderLayout.SOUTH);
        comment1.setFont(plainFont);
        comment2.setFont(plainFont);
        comment1.setBorder(new EmptyBorder(10,0,0,0));
        comment2.setBorder(new EmptyBorder(0,0,20,0));
        algoList.setBorder(new EmptyBorder(10,0,20,0));
        nextPrevPanel.nextButton.setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == algoList) {
            paramCardsLayout.show(paramCardsPanel, (String)algoList.getSelectedItem());
            if(verify()){
                action();
            }else{
                falseAction();
            }
        }
    }

    public TileProcessingAlgorithm getAlgorithm(ImageTile tile){
        if(!verify()){
            throw new IllegalArgumentException("Invalid parameters");
        }
        return switch ((String)(algoList.getSelectedItem())){
            case GuiConstants.BILATERAL_FILTER -> new BilateralFilter(bilateralParamCard.getParams(), tile);
            case GuiConstants.MEDIAN_FILTER -> new MedianFilter(medianParamCard.getParams(), tile);
            case GuiConstants.SOBEL_OPERATOR -> new SobelOperator(sobelParamCard.getParams(), tile);
            default -> throw new IllegalArgumentException("Invalid parameters");
        };
    }

    public AlgorithmParameters getParameters() {
        if(!verify()){
            throw new IllegalArgumentException("Invalid parameters");
        }
        return switch ((String) (algoList.getSelectedItem())) {
            case GuiConstants.BILATERAL_FILTER -> bilateralParamCard.getParams();
            case GuiConstants.MEDIAN_FILTER -> medianParamCard.getParams();
            case GuiConstants.SOBEL_OPERATOR -> sobelParamCard.getParams();
            default -> throw new IllegalArgumentException("Invalid parameters");
        };
    }

    public String getAlgorithmID(){
        if(!verify()){
            throw new IllegalArgumentException("Invalid parameters");
        }
        return switch ((String) (algoList.getSelectedItem())) {
            case GuiConstants.BILATERAL_FILTER -> AlgorithmFactory.AlgorithmIDs.BILATERAL_FILTER;
            case GuiConstants.MEDIAN_FILTER -> AlgorithmFactory.AlgorithmIDs.MEDIAN_FILTER;
            case GuiConstants.SOBEL_OPERATOR -> AlgorithmFactory.AlgorithmIDs.SOBEL_OPERATOR;
            default -> throw new IllegalArgumentException("Invalid parameters");
        };
    }
    @Override
    public boolean verify() {
        return switch ((String) (algoList.getSelectedItem())) {
            case GuiConstants.BILATERAL_FILTER -> bilateralParamCard.verify();
            case GuiConstants.MEDIAN_FILTER -> medianParamCard.verify();
            case GuiConstants.SOBEL_OPERATOR -> sobelParamCard.verify();
            default -> false;
        };
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
