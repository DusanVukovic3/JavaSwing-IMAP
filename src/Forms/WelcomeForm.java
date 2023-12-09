package Forms;

import Model.User;
import Swing.EmailManager;
import javax.swing.*;
import java.awt.*;

public class WelcomeForm extends JFrame {
    public WelcomeForm(JFrame parent, User user) {
        super("Welcome Form");
        setLayout(new FlowLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel welcomeLabel = new JLabel("Welcome, " + user.getEmail() + "!");
        add(welcomeLabel);

        JButton checkEmailButton = new JButton("Check New Emails");
        checkEmailButton.addActionListener(e -> checkNewEmails(user));
        add(checkEmailButton);

        pack();
        setMinimumSize(new Dimension(400, 500));
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void checkNewEmails(User user) {
        System.out.println("Checking new emails...");
        EmailManager emailManager = new EmailManager(user.getEmail(), user.getPassword());
        emailManager.startListening();
        System.out.println("Email check completed.");
    }
}
