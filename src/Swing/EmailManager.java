package Swing;

import jakarta.mail.*;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.event.MessageCountListener;

import javax.swing.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


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

    public void startListening() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    Store store = session.getStore("imaps");
                    store.connect(username, password);

                    Folder inbox = store.getFolder("INBOX");
                    inbox.open(Folder.READ_WRITE);

                    Thread keepAliveThread = new Thread(new KeepAliveRunnable(inbox), "IdleConnectionKeepAlive");
                    keepAliveThread.start();

                    inbox.addMessageCountListener(new MessageCountListener() {
                        @Override
                        public void messagesAdded(MessageCountEvent event) {
                            Message[] messages = event.getMessages();
                            for (Message message : messages) {
                                try {
                                    // Process or handle the received email as needed
                                    System.out.println("Received email: " + message.getSubject());
                                } catch (MessagingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void messagesRemoved(MessageCountEvent messageCountEvent) {

                        }
                    });

                    long startTime = System.currentTimeMillis();
                    long durationMillis = TimeUnit.MINUTES.toMillis(1);

                    while (!stopListening && System.currentTimeMillis() - startTime < durationMillis) {
                        inbox.isOpen();
                    }

                    stopListening = true;  // Ensure that the background thread stops listening

                    if (keepAliveThread.isAlive()) {
                        keepAliveThread.interrupt();
                    }
                    inbox.close();
                    store.close();

                } catch (MessagingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public void stopListening() {
        stopListening = true;  // Set the flag to stop listening
    }
}
