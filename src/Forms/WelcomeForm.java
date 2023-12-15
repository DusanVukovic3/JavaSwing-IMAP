package Forms;

import Model.EmailInfo;
import Model.User;
import Swing.EmailManager;
import Swing.YourSwingWorker;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


public class WelcomeForm extends JFrame {
    private final EmailManager emailManager;
    private final JButton checkEmailButton;
    private final JButton stopEmailButton;
    private SwingWorker<Void, String> emailRetrievalWorker;
    private final DefaultTableModel tableModel;
    private JTable emailTable;
    private final JTextArea emailContentArea;
    private final JSplitPane splitPane;
    private List<EmailInfo> emailInfoList = new ArrayList<>();
    private List<EmailInfo> displayedEmails = new ArrayList<>(); // Prikazani mejlovi u tabeli

    private JTextField searchField;


    public WelcomeForm(JFrame parent, User user) {
        super("Welcome Form");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel welcomeLabel = new JLabel("Welcome, " + user.getEmail() + "!");
        add(welcomeLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        tableModel.addColumn("Date");
        tableModel.addColumn("Subject");
        tableModel.addColumn("Sender");

        emailTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(emailTable);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, null);
        splitPane.setDividerLocation(400);
        add(splitPane, BorderLayout.CENTER);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        emailTable.setRowSorter(sorter);
        emailTable.setDefaultEditor(Object.class, null);

        emailTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // Double-click
                    int selectedRow = emailTable.getSelectedRow();
                    if (selectedRow != -1) {
                        displayEmailContent(selectedRow);
                    }
                }
            }
        });

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(150, 25));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search by subject or sender: "));
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        searchField.getDocument().addDocumentListener(new DocumentListener() {      //na svaki unos slova, searchuj
            @Override
            public void insertUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                performSearch();
            }
        });

        JPanel buttonPanel = new JPanel();
        checkEmailButton = new JButton("Check New Emails");
        checkEmailButton.addActionListener(e -> checkNewEmails(user));
        buttonPanel.add(checkEmailButton);

        stopEmailButton = new JButton("Stop Retrieval");
        stopEmailButton.addActionListener(e -> stopEmailRetrieval());
        buttonPanel.add(stopEmailButton);

        add(buttonPanel, BorderLayout.SOUTH);

        emailContentArea = new JTextArea();
        emailContentArea.setEditable(false);

        pack();
        setMinimumSize(new Dimension(1400, 800));
        setLocationRelativeTo(parent);
        setVisible(true);

        emailManager = new EmailManager();
    }

    private void checkNewEmails(User user) {
        checkEmailButton.setEnabled(false);
        stopEmailButton.setEnabled(true);

        tableModel.setRowCount(0);
        emailContentArea.setText("");
        splitPane.setRightComponent(null);

        emailManager.resetStopRetrieval();
        YourSwingWorker emailRetrievalWorker = new YourSwingWorker() {
            @Override
            protected Void doInBackground() throws IOException {
                try {
                    System.out.println("Checking new emails...");
                    emailInfoList.clear();
                    emailManager.handleEmailRetrieval("imap.gmail.com", user.getEmail(), user.getPassword(), this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;

            }
            @Override
            protected void process(List<EmailInfo> chunks) {
                for (EmailInfo emailInfo : chunks) {
                    emailInfoList.add(emailInfo);
                    //emailInfoListOrder.add(emailInfo);      //napuni obe liste
                    tableModel.addRow(new Object[]{emailInfo.getFormattedDate(), emailInfo.getSubject(), emailInfo.getSender()});
                }
            }

            @Override
            protected void done() {
                checkEmailButton.setEnabled(true);
                stopEmailButton.setEnabled(false);
            }
        };
        emailRetrievalWorker.execute();
    }

    private void displayEmailContent(int selectedRow) {
        EmailInfo email = displayedEmails.get(selectedRow);
        String content = email.getContent();

        SwingUtilities.invokeLater(() -> {
            JEditorPane editorPane = new JEditorPane("text/html", content);
            editorPane.setEditable(false);
            editorPane.addHyperlinkListener(e -> {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(editorPane);
            scrollPane.setPreferredSize(new Dimension(650, 800));
            splitPane.setRightComponent(scrollPane);
        });
    }


    private void stopEmailRetrieval() {
        System.out.println("Stopping email retrieval...");
        emailManager.stopEmailRetrieval();

        if (emailRetrievalWorker != null && !emailRetrievalWorker.isDone()) {
            emailRetrievalWorker.cancel(true);
        }
    }

    private void performSearch() {
        String searchTerm = searchField.getText().toLowerCase();

        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) emailTable.getRowSorter();
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchTerm, 1, 2)); // 2. i 3. kolona, tj subject i sender

        // Update the displayedEmails list with the filtered emails
        displayedEmails = filterEmails(searchTerm);

        // Clear existing rows from the table
        tableModel.setRowCount(0);

        // Add rows for the filtered emails
        for (EmailInfo emailInfo : displayedEmails) {
            tableModel.addRow(new Object[]{emailInfo.getFormattedDate(), emailInfo.getSubject(), emailInfo.getSender()});
        }
    }

    private List<EmailInfo> filterEmails(String searchTerm) {
        List<EmailInfo> filteredEmails = new ArrayList<>();

        for (int i = 0; i < emailInfoList.size(); i++) {
            String subject = emailInfoList.get(i).getSubject().toLowerCase();
            String sender = emailInfoList.get(i).getSender().toLowerCase();

            if (subject.contains(searchTerm) || sender.contains(searchTerm)) {
                filteredEmails.add(emailInfoList.get(i));
            }
        }

        return filteredEmails;
    }







}
