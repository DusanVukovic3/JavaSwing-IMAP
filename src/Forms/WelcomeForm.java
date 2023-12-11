package Forms;

import Model.User;
import Swing.EmailManager;
import Swing.YourSwingWorker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Vector;

public class WelcomeForm extends JFrame {
    private final EmailManager emailManager;
    private final JButton checkEmailButton;
    private final JButton stopEmailButton;
    private SwingWorker<Void, String> emailRetrievalWorker;
    private final DefaultTableModel tableModel;
    private JTable emailTable;

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
        add(scrollPane, BorderLayout.CENTER);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);    //sortiranje tabele
        emailTable.setRowSorter(sorter);

        JPanel buttonPanel = new JPanel();
        checkEmailButton = new JButton("Check New Emails");
        checkEmailButton.addActionListener(e -> checkNewEmails(user));
        buttonPanel.add(checkEmailButton);

        stopEmailButton = new JButton("Stop Retrieval");
        stopEmailButton.addActionListener(e -> stopEmailRetrieval());
        buttonPanel.add(stopEmailButton);

        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(600, 800));
        setLocationRelativeTo(parent);
        setVisible(true);

        emailManager = new EmailManager();
    }

    private void checkNewEmails(User user) {
        checkEmailButton.setEnabled(false);
        stopEmailButton.setEnabled(true);

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


    private void stopEmailRetrieval() {
        System.out.println("Stopping email retrieval...");
        emailManager.stopEmailRetrieval();

        if (emailRetrievalWorker != null && !emailRetrievalWorker.isDone()) {
            emailRetrievalWorker.cancel(true);
        }
    }


}