public class MainClass {
    public static void main(String[] args) throws InterruptedException {
        Tray tray = new Tray();
        //生成10个拿取产品的ConsumerThread
        ConsumerThread consumerThread = new ConsumerThread(tray);
        consumerThread.start();
        //创建一个生产10个产品的ProducerThread
        ProducerThread producerThread = new ProducerThread(tray);
        producerThread.start();
        Thread.sleep(100);
        System.out.println("The END!!!");
    }
}
