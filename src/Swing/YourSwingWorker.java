package Swing;

import javax.swing.*;
import java.util.Vector;

public abstract class YourSwingWorker extends SwingWorker<Void, Vector<String>> {

    public void publishData(Vector<String> data) {
        publish(data);
    }


}

