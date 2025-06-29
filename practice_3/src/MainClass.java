import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        OrderQueue orderQueue = new OrderQueue();
        String[] chefTypes = {"A", "B", "C"};
        HashMap<String, ProcessingQueue> queueMap = new HashMap<>();
        for (String chefType : chefTypes) {
            ProcessingQueue queue = new ProcessingQueue();
            queueMap.put(chefType, queue);
            ChefThread chefThread = new ChefThread(chefType, queue);
            chefThread.start();
        }
        InputThread inputThread = new InputThread(orderQueue);
        inputThread.start();
        DispatchThread dispatchThread = new DispatchThread(orderQueue, queueMap);
        dispatchThread.start();
    }
}
