package Forms;

import javax.swing.*;
import java.awt.*;

public class EmailContentForm extends JDialog {
    private JTextArea emailTextArea;

    public EmailContentForm(Frame parent, String emailContent) {
        super(parent, "Email Content", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);

        emailTextArea = new JTextArea(emailContent);
        emailTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(emailTextArea);

        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }
}
