package Forms;

import Model.User;
import Swing.EmailManager;
import javax.swing.*;
import java.awt.*;

public class WelcomeForm extends JFrame {
    private final EmailManager emailManager;
    private JButton checkEmailButton;
    private JButton stopEmailButton;
    private SwingWorker<Void, String> emailRetrievalWorker;

    public WelcomeForm(JFrame parent, User user) {
        super("Welcome Form");
        setLayout(new FlowLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel welcomeLabel = new JLabel("Welcome, " + user.getEmail() + "!");
        add(welcomeLabel);

        checkEmailButton = new JButton("Check New Emails");
        checkEmailButton.addActionListener(e -> checkNewEmails(user));
        add(checkEmailButton);

        stopEmailButton = new JButton("Stop Retrieval");
        stopEmailButton.addActionListener(e -> stopEmailRetrieval());
        add(stopEmailButton);

        pack();
        setMinimumSize(new Dimension(400, 500));
        setLocationRelativeTo(parent);
        setVisible(true);

        // Create EmailManager instance
        emailManager = new EmailManager();
    }

    private void checkNewEmails(User user) {
        checkEmailButton.setEnabled(false);
        stopEmailButton.setEnabled(true);

        emailManager.resetStopRetrieval();
        emailRetrievalWorker = new SwingWorker<Void, String>() {  //Novi swingWorker svaki put kada se dugme checkEmails klikne
            @Override
            protected Void doInBackground() throws Exception {
                System.out.println("Checking new emails...");
                emailManager.handleEmailRetrieval("imap.gmail.com", user.getEmail(), user.getPassword());
                return null;
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