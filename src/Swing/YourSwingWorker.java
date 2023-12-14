package Swing;

import Model.EmailInfo;

import javax.swing.*;

public abstract class YourSwingWorker extends SwingWorker<Void, EmailInfo> {

//    public void publishData(Vector<String> data) {
//        publish(data);
//    }

    public void publishEmailInfo(EmailInfo emailInfo) {
        publish(emailInfo);
    }

}

