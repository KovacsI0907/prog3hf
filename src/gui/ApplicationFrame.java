package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ApplicationFrame extends JFrame implements ActionListener {
    private CardLayout cardLayout;
    private JPanel cardPanel;

    JMenuItem memOption = new JMenuItem("Memory and Multithreading");
    JMenuItem exitOption = new JMenuItem("Quit (Unsafe while running)");

    AlgoStatusCard algoStatusCard;

    public ApplicationFrame() {
        setTitle("Image Processor");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        JMenu optionsMenu = new JMenu("Menu");

        memOption.addActionListener(this);
        exitOption.addActionListener(this);

        optionsMenu.add(memOption);
        optionsMenu.add(exitOption);

        menuBar.add(optionsMenu);
        setJMenuBar(menuBar);


        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        Logger logger = new Logger();
        algoStatusCard = new AlgoStatusCard(cardPanel, logger);
        FinalizeCard finalizeCard = new FinalizeCard(cardPanel, algoStatusCard, logger);
        ChooseInputCard chooseInputCard = new ChooseInputCard(cardPanel, finalizeCard);
        ChooseAlgorithmCard chooseAlgorithmCard = new ChooseAlgorithmCard(cardPanel, finalizeCard);
        finalizeCard.setChooseInputCard(chooseInputCard);
        finalizeCard.setChooseAlgorithmCard(chooseAlgorithmCard);
        ResourcesCard resourcesCard = new ResourcesCard(cardPanel, finalizeCard, logger);

        cardPanel.add(chooseInputCard, GuiConstants.CHOOSE_INPUT_CARD);
        cardPanel.add(chooseAlgorithmCard, GuiConstants.CHOOSE_ALGORITHM_CARD);
        cardPanel.add(finalizeCard, GuiConstants.FINALIZE_CHOICES_CARD);
        cardPanel.add(algoStatusCard, GuiConstants.PROCESSING_STATUS_CARD);
        cardPanel.add(resourcesCard, GuiConstants.RESOURCES_CARD);

        cardLayout.show(cardPanel, GuiConstants.CHOOSE_INPUT_CARD);

        JScrollPane scrollPane = new JScrollPane(logger);

        setLayout(new BorderLayout());

        add(cardPanel, BorderLayout.CENTER);
        algoStatusCard.add(scrollPane);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(actionEvent.getSource() == memOption){
            cardLayout.show(cardPanel, GuiConstants.RESOURCES_CARD);
        }

        if(actionEvent.getSource() == exitOption){
            this.dispose();
            System.exit(0);
        }
    }
}
