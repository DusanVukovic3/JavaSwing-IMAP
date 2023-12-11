package Forms;

import Model.User;
import Swing.EmailManager;
import Swing.YourSwingWorker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

public class WelcomeForm extends JFrame {
    private final EmailManager emailManager;
    private final JButton checkEmailButton;
    private final JButton stopEmailButton;
    private SwingWorker<Void, String> emailRetrievalWorker;
    private final DefaultTableModel tableModel;
    private JTable emailTable;
    private final JTextArea emailContentArea;
    private final JSplitPane splitPane;

    public WelcomeForm(JFrame parent, User user) {
        super("Welcome Form");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel welcomeLabel = new JLabel("Welcome, " + user.getEmail() + "!");
        add(welcomeLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        tableModel.addColumn("Date");
        tableModel.addColumn("Subject");
        tableModel.addColumn("Sender");

        emailTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(emailTable);

        // Set up the split pane
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, null);
        splitPane.setDividerLocation(400); // Adjust the initial divider location
        add(splitPane, BorderLayout.CENTER);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        emailTable.setRowSorter(sorter);
        emailTable.setDefaultEditor(Object.class, null);

        emailTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // Single-click
                    int selectedRow = emailTable.getSelectedRow();
                    if (selectedRow != -1) {
                        displayEmailContent(selectedRow);
                    }
                }
            }
        });

        JPanel buttonPanel = new JPanel();
        checkEmailButton = new JButton("Check New Emails");
        checkEmailButton.addActionListener(e -> checkNewEmails(user));
        buttonPanel.add(checkEmailButton);

        stopEmailButton = new JButton("Stop Retrieval");
        stopEmailButton.addActionListener(e -> stopEmailRetrieval());
        buttonPanel.add(stopEmailButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Initialize the email content area
        emailContentArea = new JTextArea();
        emailContentArea.setEditable(false);
        JScrollPane contentScrollPane = new JScrollPane(emailContentArea);

        pack();
        setMinimumSize(new Dimension(1000, 800));
        setLocationRelativeTo(parent);
        setVisible(true);

        emailManager = new EmailManager();
    }

    private void checkNewEmails(User user) {
        checkEmailButton.setEnabled(false);
        stopEmailButton.setEnabled(true);

        tableModel.setRowCount(0);
        emailContentArea.setText("");
        splitPane.setRightComponent(null);

        emailManager.resetStopRetrieval();
        YourSwingWorker emailRetrievalWorker = new YourSwingWorker() {
            @Override
            protected Void doInBackground() {
                System.out.println("Checking new emails...");
                emailManager.handleEmailRetrieval("imap.gmail.com", user.getEmail(), user.getPassword(), this);
                return null;
            }

            @Override
            protected void process(java.util.List<Vector<String>> chunks) {
                for (Vector<String> chunk : chunks) {
                    tableModel.addRow(chunk);
                }
            }

            @Override
            protected void done() {
                checkEmailButton.setEnabled(true);
                stopEmailButton.setEnabled(false);
            }
        };
        emailRetrievalWorker.execute();
    }

    private void displayEmailContent(int selectedRow) {
        String subject = (String) tableModel.getValueAt(selectedRow, 1);
        String content = getEmailContentFromServer(subject);

        SwingUtilities.invokeLater(() -> {
            emailContentArea.setText(content);

            // Create a scroll pane and set its preferred size
            JScrollPane scrollPane = new JScrollPane(emailContentArea);
            scrollPane.setPreferredSize(new Dimension(400, 800));

            // Add the scroll pane to the content area
            splitPane.setRightComponent(scrollPane);
        });
    }

    private void stopEmailRetrieval() {
        System.out.println("Stopping email retrieval...");
        emailManager.stopEmailRetrieval();

        if (emailRetrievalWorker != null && !emailRetrievalWorker.isDone()) {
            emailRetrievalWorker.cancel(true);
        }
    }

    private String getEmailContentFromServer(String subject) {
        // Implement a method to retrieve email content from the server based on the subject.
        // Return the email content as a string.
        return "This is the content of the email with subject: " + subject;
    }
}
