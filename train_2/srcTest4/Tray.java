public class Tray {
    private int productNum = 0;

    public synchronized void addProduct(int number) throws InterruptedException {
        while (productNum != 0) {
            this.wait();
        }
        productNum = number;
        System.out.println("Producer put:" + productNum);
        this.notifyAll();
    }

    public synchronized void getProductNum() throws InterruptedException {
        while (productNum == 0) {
            this.wait();
        }
        System.out.println("Consumer get:" + productNum);
        productNum = 0;
        this.notifyAll();
    }
}
