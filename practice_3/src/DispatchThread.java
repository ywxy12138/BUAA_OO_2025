import com.oocourse.exp.Order;

import java.util.HashMap;

public class DispatchThread extends Thread {
    private final OrderQueue orderQueue;
    private final HashMap<String, ProcessingQueue> queueMap;

    public DispatchThread(OrderQueue orderQueue,
                          HashMap<String, ProcessingQueue> queueMap) {
        this.orderQueue = orderQueue;
        this.queueMap = queueMap;
    }

    @Override
    public void run() {
        while (true) {
            if (orderQueue.isEmpty() && orderQueue.isEnd()) {
                for (ProcessingQueue queue : queueMap.values()) {
                    queue.setEnd();
                }
                System.out.println("DispatchThread ends");
                break;
            }
            Order order = orderQueue.poll();
            if (order == null) {
                continue;
            }
            dispatch(order);
        }
    }

    private void dispatch(Order order) {
        String chef;
        switch (order.getDish()) {
            case ("Appetizer"):
                chef = "A";
                break;
            case ("Main Course"):
                chef = "B";
                break;
            case ("Dessert"):
                chef = "C";
                break;
            default:
                throw new IllegalArgumentException("Invalid order type");
        }
        ProcessingQueue queue = queueMap.get(chef);
        queue.offer(order, chef);
    }
}
