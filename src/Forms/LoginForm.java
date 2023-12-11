package Forms;

import Model.User;
import Swing.GmailAuthentication;
import javax.swing.*;
import java.awt.*;

public class LoginForm extends JDialog {
    private JTextField tfEmail;
    private JPasswordField pfPassword;
    private JButton btnOk;
    private JButton btnCancel;
    private JPanel loginPanel;
    public User user;

    public LoginForm(JFrame parent) {
        super(parent);
        setTitle("Login to IMAP");
        setContentPane(loginPanel);
        setMinimumSize(new Dimension(450, 500));
        setModal(true);
        setLocationRelativeTo(parent); //da
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        btnOk.addActionListener(e -> {
            String email = tfEmail.getText();
            String password = String.valueOf(pfPassword.getPassword());

            user = getAuthenticatedUser(email, password);

            if (user != null) {
                user = new User(email, password);
                dispose();
                WelcomeForm welcomeForm = new WelcomeForm(null, user);
            }
            else {
                JOptionPane.showMessageDialog(LoginForm.this, "Email or password invalid", "Try again", JOptionPane.ERROR_MESSAGE);
            }
        });
        setVisible(true);

    }

    private User getAuthenticatedUser(String email, String password) {
        User user = GmailAuthentication.authenticate(email, password);
        return user;
    }

    public static void main(String[] args) {
        LoginForm loginForm = new LoginForm(null);
    }


}
