import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SwingExample extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JTextArea textArea;

    public SwingExample() {
        setTitle("Swing JFrame Example");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu optionsMenu = new JMenu("Options");

        // Placeholder buttons in the "Options" menu
        JMenuItem option1 = new JMenuItem("Option 1");
        JMenuItem option2 = new JMenuItem("Option 2");

        optionsMenu.add(option1);
        optionsMenu.add(option2);

        menuBar.add(optionsMenu);
        setJMenuBar(menuBar);

        // Card Layout Panel
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Example Panels with Navigation Buttons
        JPanel panel1 = createCardPanel("Example Data 1");
        JPanel panel2 = createCardPanel("Example Data 2");
        JPanel panel3 = createCardPanel("Example Data 3");

        cardPanel.add(panel1, "Panel 1");
        cardPanel.add(panel2, "Panel 2");
        cardPanel.add(panel3, "Panel 3");

        // Text Area with dummy text
        textArea = new JTextArea();
        textArea.setRows(5);
        textArea.setEditable(false);
        textArea.setText("This is some dummy text.\nYou can replace it with your own content.");

        JScrollPane scrollPane = new JScrollPane(textArea);

        // Set BorderLayout for the main frame
        setLayout(new BorderLayout());

        // Add components to the main frame
        add(cardPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // Set the frame to be visible
        setVisible(true);
    }

    private JPanel createCardPanel(String data) {
        JPanel cardPanel = new JPanel(new BorderLayout());

        // Example Data
        JPanel dataPanel = new JPanel();
        dataPanel.add(new JLabel(data));
        cardPanel.add(dataPanel, BorderLayout.CENTER);

        // Navigation Buttons
        JPanel buttonPanel = new JPanel();
        JButton nextButton = new JButton("Next");
        JButton backButton = new JButton("Back");

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.next(cardPanel.getParent());
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.previous(cardPanel.getParent());
            }
        });

        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);

        cardPanel.add(buttonPanel, BorderLayout.SOUTH);

        return cardPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingExample());
    }
}
