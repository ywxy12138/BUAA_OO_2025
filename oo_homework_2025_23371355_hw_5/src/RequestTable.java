import java.util.concurrent.CopyOnWriteArrayList;

public class RequestTable {
    private CopyOnWriteArrayList<Person> people = new CopyOnWriteArrayList<>();
    private boolean isEnd = false;

    public synchronized void addRequest(Person person) {
        people.add(person);
        this.notifyAll();
    }

    public synchronized Person getRequests() {
        //若当前请求队列内为空，且输入线程尚未结束，
        //则从此处获得请求的调度器线程应该歇一会，等一下输入线程的操作
        while (people.isEmpty() && !isEnd) {
            try {
                this.wait();
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
        return people.remove(0);
    }

    public synchronized void removeRequest(Person person) {
        people.remove(person);
        this.notifyAll();
    }

    public synchronized CopyOnWriteArrayList<Person> getPeople() {
        this.notifyAll();
        return people;
    }

    public synchronized CopyOnWriteArrayList<Person> deepCopy() {
        CopyOnWriteArrayList<Person> copy = new CopyOnWriteArrayList<>();
        for (Person person : people) {
            copy.add(person.deepCopy());
        }
        return copy;
    }

    public synchronized void setEnd() {
        isEnd = true;
        this.notifyAll();
    }

    public synchronized boolean isEnd() {
        this.notifyAll();
        return isEnd;
    }

    public synchronized boolean isEmpty() {
        this.notifyAll();
        return people.isEmpty();
    }
}
