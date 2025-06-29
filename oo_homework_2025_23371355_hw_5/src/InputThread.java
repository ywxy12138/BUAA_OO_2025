import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.Request;

import java.io.IOException;

public class InputThread extends Thread {
    private final RequestTable requestTable;

    public InputThread(RequestTable requestTable) {
        this.requestTable = requestTable;
    }

    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            long time = System.currentTimeMillis();
            if (request == null) {
                //输入结束，退出输入进程
                //改变总候乘表状态
                //便于调度器线程和电梯线程判断输入结束
                requestTable.setEnd();
                break;
            }
            else {
                if (request instanceof PersonRequest) {
                    //如果是合法输入，则添加相应请求到总候乘表中
                    PersonRequest personRequest = (PersonRequest) request;
                    int fromFloor = ChangeFloor.getFloor(personRequest.getFromFloor());
                    int toFloor = ChangeFloor.getFloor(personRequest.getToFloor());
                    int personId = personRequest.getPersonId();
                    int priority = personRequest.getPriority();
                    int elevatorId = personRequest.getElevatorId();
                    Person person = new Person(fromFloor, toFloor,
                        personId, priority, elevatorId, time);
                    requestTable.addRequest(person);
                }
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
