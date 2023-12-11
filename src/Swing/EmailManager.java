package Swing;

import javax.mail.*;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;


public class EmailManager {
    private boolean stopRetrieval = false;
    public void handleEmailRetrieval(String host, String username, String password, YourSwingWorker worker) {

        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imap.host", "imap.gmail.com");
        properties.put("mail.imap.port", "993");

        try {
            Session session = Session.getInstance(properties);
            Store store = session.getStore();
            store.connect(host, username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            SearchTerm searchTerm = getSearchTerm();

            Message[] messages = inbox.search(searchTerm);

            for (Message message : messages) {
                if (Thread.interrupted() || stopRetrieval) {
                    return;
                }

                Address[] fromAddresses = message.getFrom();
                String sender = (fromAddresses != null && fromAddresses.length > 0)
                        ? fromAddresses[0].toString()
                        : "Unknown Sender";

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");
                String formattedDate = null;

                try {
                    Date originalDate = message.getReceivedDate();

                    if (originalDate != null) {
                        formattedDate = dateFormat.format(originalDate);
                    }

                } catch (MessagingException | NullPointerException e) {
                    e.printStackTrace();
                }

                System.out.println("DATE: " + formattedDate + "Subject: " + message.getSubject() + "Sender: " + sender);

                Vector<String> rowData = new Vector<>();    //punjenje tabele

                rowData.add(formattedDate);
                rowData.add(message.getSubject());
                rowData.add(sender);

                worker.publishData(rowData);
            }

            inbox.close(false);
            store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private static SearchTerm getSearchTerm() {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        cal.set(currentYear, Calendar.DECEMBER, 1, 0, 0, 0);    //Poruke novije od 01.12.2023.
        Date fromDate = cal.getTime();
        ReceivedDateTerm receivedDateTerm = new ReceivedDateTerm(ReceivedDateTerm.GE, fromDate);

        Flags seenFlag = new Flags(Flags.Flag.SEEN);
        FlagTerm unseenFlagTerm = new FlagTerm(seenFlag, false);

        return new AndTerm(receivedDateTerm, unseenFlagTerm);
    }

    public void stopEmailRetrieval() {
        stopRetrieval = true;
    }

    public void resetStopRetrieval() {
        stopRetrieval = false;
    }
}
