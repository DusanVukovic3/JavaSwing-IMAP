package Swing;

import javax.mail.Session;
import java.util.Properties;


public class EmailManager {
    private final String username;
    private final String password;
    private final Session session;
    private boolean stopListening = false;

    public EmailManager(String username, String password) {
        this.username = username;
        this.password = password;

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", "your-imap-host");
        props.put("mail.imaps.port", "993");

        session = Session.getDefaultInstance(props);
    }



}
