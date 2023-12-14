package Model;

public class EmailInfo {
    private String formattedDate;
    private String subject;
    private String sender;
    private String content;

    public EmailInfo(String formattedDate, String subject, String sender) {
        this.formattedDate = formattedDate;
        this.subject = subject;
        this.sender = sender;
    }
    public EmailInfo(String formattedDate, String subject, String sender, String content) {
        this.formattedDate = formattedDate;
        this.subject = subject;
        this.sender = sender;
        this.content = content;
    }


    public String getFormattedDate() {
        return formattedDate;
    }

    public String getSubject() {
        return subject;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

}
