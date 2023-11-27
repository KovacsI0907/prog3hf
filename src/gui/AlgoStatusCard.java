package gui;

import ImageProcessingAlgorithms.AlgorithmParameters;
import ParallelImageProcessing.ImageProcessingContext;
import ParallelImageProcessing.ImageProcessingScheduler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.Kernel;
import java.io.File;
import java.util.Deque;

public class AlgoStatusCard extends JPanel implements ActionListener {
    public final Logger logger;
    public final JProgressBar mainProgressBar;
    NextBackPanel nextPrevPanel;

    public AlgoStatusCard(JPanel cardsContainer, Logger logger){
        super(new BorderLayout());

        JPanel middleBox = new JPanel();
        middleBox.setLayout(new BoxLayout(middleBox, BoxLayout.Y_AXIS));
        this.logger = logger;
        mainProgressBar = new JProgressBar();
        middleBox.add(mainProgressBar);
        middleBox.add(logger);
        this.add(middleBox, BorderLayout.CENTER);

        nextPrevPanel = new NextBackPanel(cardsContainer, GuiConstants.CHOOSE_INPUT_CARD, null);
        nextPrevPanel.nextButton.setText("New Task");
        nextPrevPanel.nextButton.setEnabled(false);
        nextPrevPanel.backButton.setEnabled(false);
        nextPrevPanel.nextButton.addActionListener(this);

        this.add(nextPrevPanel, BorderLayout.SOUTH);
    }
    public void startScheduler(Deque<ImageProcessingContext> imageProcessingContexts, AlgorithmParameters parameters, String algorithmID){
        ImageProcessingScheduler scheduler = new ImageProcessingScheduler(imageProcessingContexts, UserPreferences.getInstance().threadCount, 60, new File("output"), parameters, algorithmID, this);
        Thread schedulerThread = new Thread(scheduler);
        schedulerThread.start();
        Thread memoryMonitor = new Thread(() -> {
            while(true){
                int currentUsage = (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024*1024));
                System.out.println("MEM: " + currentUsage + "/" + UserPreferences.getInstance().memorySize);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        memoryMonitor.start();
        logger.logGreen("Starting scheduler with " + UserPreferences.getInstance().threadCount + " threads and " + UserPreferences.getInstance().memorySize + "MBs of memory");

        mainProgressBar.setMaximum(imageProcessingContexts.size());
        mainProgressBar.setStringPainted(true);
        mainProgressBar.setValue(0);
    }

    public void updateMainProgressBar() {
        mainProgressBar.setValue(mainProgressBar.getValue() + 1);
    }

    public void allowNextTask() {
        nextPrevPanel.nextButton.setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(actionEvent.getSource() == nextPrevPanel.nextButton){
            nextPrevPanel.nextButton.setEnabled(false);
        }
    }
}
