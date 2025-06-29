import java.util.ArrayList;

public class RequestTable {
    private static RequestTable requestTable = null;
    private ArrayList<Person> people;
    private boolean isEnd;
    private ArrayList<Integer> waitingElevators;

    public static RequestTable getInstance() {
        if (requestTable == null) {
            requestTable = new RequestTable();
        }
        return requestTable;
    }

    private RequestTable() {
        people = new ArrayList<>();
        isEnd = false;
        waitingElevators = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            waitingElevators.add(0);
        }
    }

    public synchronized void addPerson(Person person) {
        people.add(person);
        notifyAll();
    }

    public synchronized void addWaitingElevator(Integer elevatorId) {
        waitingElevators.set(elevatorId - 1, 1);
        notifyAll();
    }

    public synchronized void subWaitingElevator(Integer elevatorId) {
        waitingElevators.set(elevatorId - 1, 0);
        notifyAll();
    }

    public synchronized Person getPerson() {
        //若当前请求队列内为空，且输入线程尚未结束，
        //则从此处获得请求的调度器线程应该歇一会，等一下输入线程的操作
        while (people.isEmpty() && !isEnd) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (people.isEmpty()) {
            //防止总候乘表到末尾刚好请求队列为空的情况下返回有误
            return null;
        }
        //this.notifyAll();不需要都notifyAll，会增加轮询的可能，只在修改值后进行notify
        //要一个总候乘表返回一个，因为只要总候乘表里有请求，
        //调度器线程就会来要，所以不用担心请求不同步给出的问题
        notifyAll();
        return people.remove(0);
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        return isEnd;
    }

    public synchronized boolean isEmpty() {
        return people.isEmpty();
    }

    public synchronized boolean isAllWaiting() {
        Integer num = 0;
        for (int i = 0; i < 6; i++) {
            num += waitingElevators.get(i);
        }
        return num == 6;
    }
}
