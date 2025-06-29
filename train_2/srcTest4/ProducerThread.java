public class ProducerThread extends Thread {
    private Tray tray;

    public ProducerThread(Tray tray) {
        this.tray = tray;
    }

    public void run() {
        for (int i = 1; i <= 10; i++) {
            try {
                tray.addProduct(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                sleep((int)Math.random() * 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
