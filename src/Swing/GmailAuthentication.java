package Swing;

import Model.User;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

public class GmailAuthentication {
    public static User authenticate(String email, String password) {
        User user = null;

        Properties props = new Properties();
        props.put("mail.imap.host", "imap.gmail.com");
        props.put("mail.imap.port", "993");
        props.put("mail.imap.ssl.enable", "true");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });

        try {
            Store store = session.getStore("imap");
            store.connect();
            user = new User(email); // Replace this with actual User creation logic

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }
}
