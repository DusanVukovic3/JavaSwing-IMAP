package Swing;


import Model.EmailInfo;
import jakarta.mail.*;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.SearchTerm;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;


public class EmailManager {
    private boolean stopRetrieval = false;
    private StringBuilder emailContentBuilder = new StringBuilder();
    public void handleEmailRetrieval(String host, String username, String password, YourSwingWorker worker) throws IOException {
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

                System.out.println("KONTENT : " + message.getContentType() + " " + message.getFlags() );
                String content = processContent(message.getContent());

                EmailInfo email = new EmailInfo(formattedDate, message.getSubject(), sender, content);
                worker.publishEmailInfo(email);

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
        cal.set(currentYear, Calendar.DECEMBER, 9, 0, 0, 0);    //Poruke novije od 01.12.2023.
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

    private String processContent(Object o) throws MessagingException, IOException {
        StringBuilder emailContentBuilder = new StringBuilder();

        if (o instanceof String) {
            emailContentBuilder.append("Text content: ").append(o).append("\n");
        } else if (o instanceof Multipart) {
            Multipart mp = (Multipart) o;
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bodyPart = mp.getBodyPart(i);
                if (bodyPart.getContentType().toLowerCase().startsWith("text/plain")) {
                    emailContentBuilder.append("Text content: ").append(bodyPart.getContent()).append("\n");
                } else if (bodyPart.getContentType().toLowerCase().startsWith("text/html")) {
                    emailContentBuilder.append("HTML content: ").append(extractPlainTextFromHtml(bodyPart.getContent().toString())).append("\n");
                } else if (bodyPart.getContentType().toLowerCase().startsWith("multipart/alternative")) {
                    Multipart alternativePart = (Multipart) bodyPart.getContent();
                    handleAlternativeContent(alternativePart, emailContentBuilder);
                }
            }
        }
        return emailContentBuilder.toString();
    }

    private void handleAlternativeContent(Multipart alternativePart, StringBuilder emailContentBuilder) throws MessagingException, IOException {
        for (int i = 0; i < alternativePart.getCount(); i++) {
            BodyPart alternativeBodyPart = alternativePart.getBodyPart(i);
            if (alternativeBodyPart.getContentType().toLowerCase().startsWith("text/plain")) {
                emailContentBuilder.append("Text content: ").append(alternativeBodyPart.getContent()).append("\n");
            } else if (alternativeBodyPart.getContentType().toLowerCase().startsWith("text/html")) {
                emailContentBuilder.append("HTML content: ").append(extractPlainTextFromHtml(alternativeBodyPart.getContent().toString())).append("\n");
            }
        }
    }

    private void handleBodyPart(BodyPart bodyPart, StringBuilder emailContentBuilder) throws MessagingException, IOException {
        if (bodyPart instanceof MimeBodyPart) {
            MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;
            String contentType = mimeBodyPart.getContentType().toLowerCase();

            System.out.println("KONTENT_______: " + contentType );

            if (contentType.startsWith("text/plain")) {
                Object content = mimeBodyPart.getContent();
                if (content instanceof String) {
                    emailContentBuilder.append("    ").append(content).append("\n");
                }
            } else if (contentType.startsWith("text/html")) {
                String htmlContent = mimeBodyPart.getContent().toString();
                String plainTextContent = extractPlainTextFromHtml(htmlContent);
                emailContentBuilder.append("    ").append(plainTextContent).append("\n");
            } else if (contentType.startsWith("multipart/alternative")) {
                handleAlternativeContent((Multipart) mimeBodyPart.getContent(), emailContentBuilder);
            } else {
                String fileName = mimeBodyPart.getFileName();
                if (fileName != null && !fileName.trim().isEmpty()) {
                    emailContentBuilder.append("Attachment Name: ").append(fileName).append("\n");
                }
            }
        } else {
            System.out.println("Unsupported BodyPart type: " + bodyPart.getClass());
        }
    }


    private String extractPlainTextFromHtml(String htmlContent) {
        Document document = Jsoup.parse(htmlContent);
        return document.text();
    }


}
