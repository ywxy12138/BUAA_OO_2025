import java.util.concurrent.ConcurrentHashMap;

public class DispatchThread extends Thread {
    private final RequestTable requests;
    private final ConcurrentHashMap<Integer, RequestTable> requestMap;

    public DispatchThread(RequestTable requests,
        ConcurrentHashMap<Integer, RequestTable> requestMap) {
        this.requestMap = requestMap;
        this.requests = requests;
    }

    public void run() {
        while (true) {
            //总候乘表为空且没有输入了，说明输入结束，调度器调度结束
            //告知分候乘表输入结束
            if (requests.isEmpty() && requests.isEnd()) {
                for (Integer key : requestMap.keySet()) {
                    requestMap.get(key).setEnd();
                }
                break;
            }
            Person person = null;
            //从总候乘表拿一个请求
            person = requests.getRequests();
            if (person == null) {
                continue;
            }
            dispatch(person);
        }
    }

    private void dispatch(Person person) {
        int elevatorId = person.getElevatorId();
        if (elevatorId >= 1 && elevatorId <= 6) {
            requestMap.get(elevatorId).addRequest(person);
        }
        else {
            throw new IllegalArgumentException("Invalid elevator id");
        }
    }
}
