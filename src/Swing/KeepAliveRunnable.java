package Swing;

import jakarta.mail.Folder;

public class KeepAliveRunnable implements Runnable {
    private final Folder folder;

    public KeepAliveRunnable(Folder folder) {
        this.folder = folder;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                folder.isOpen();
                Thread.sleep(300000); // Sleep for 5 minutes (adjust as needed)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
