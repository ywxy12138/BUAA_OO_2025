public class ConsumerThread extends Thread {
    private Tray tray;

    public ConsumerThread(Tray tray) {
        this.tray = tray;
    }

    public void run() {
        for (int i = 0; i < 10; i++) {
            try {
                tray.getProductNum();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
