import com.oocourse.elevator1.TimableOutput;
import java.util.concurrent.CopyOnWriteArrayList;

public class ElevatorThread extends Thread {
    private Integer elevatorID;
    private RequestTable processingTable; //电梯外的当前所处楼层不在目标楼层的人,也就是目前还在分候乘表的人
    private Elevator elevator;
    private boolean isUp; //电梯的运行方向由电梯线程主导
    private long lastTime; //上一次关门或移动的时间，刚开始初始化为主线程开启的时间，可不是系统时间的0
    private Status lastStatus;

    public ElevatorThread(Integer elevatorID, RequestTable processingTable) {
        this.elevatorID = elevatorID;
        this.processingTable = processingTable;
        elevator = new Elevator();
        isUp = true;
        this.lastTime = System.currentTimeMillis();
        lastStatus = Status.START;
    }

    public void run() {
        while (true) {
            //分候乘表为空并且没有请求输入，
            //且电梯外没有等待的人了，电梯内也没人了，且电梯门关着时
            if (processingTable.isEmpty() && processingTable.isEnd() && elevator.isEmpty()) {
                break;
            }
            if ((lastStatus.equals(Status.WAIT) || lastStatus.equals(Status.START))
                && !processingTable.isEmpty()) {
                isUp = Strategy.initIsUp(processingTable);
            }
            //刚开始有人时，初始化第一次请求时间，
            //电梯从等待状态再次上人时，相等于和刚开始有人一样
            //此时通过虚拟电梯计算一下向上和向下哪个好
            Status nextStatus =
                Strategy.getNextStatus(processingTable.getPeople(), elevator, isUp);
            if (nextStatus.equals(Status.MOVE)) {
                try {
                    move();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (nextStatus.equals(Status.OPEN)) {
                try {
                    openAndClose();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (nextStatus.equals(Status.REVERSE)) {
                isUp = !isUp;
            }
            else if (nextStatus.equals(Status.WAIT)) {
                synchronized (processingTable) {
                    try {
                        processingTable.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            lastStatus = nextStatus;
        }
    }

    private void move() throws InterruptedException {
        elevator.move(isUp);
        long arriveTime = System.currentTimeMillis();
        if (arriveTime - lastTime < 400) {
            Thread.sleep(400 - arriveTime + lastTime);
        }
        //处于刚开始的状态时，lastTime为0，可以观察一下此刻请求的时间
        //如果比0.4s多，那么就够电梯移动一层了，可以在请求来的前0.4s开始移动，
        //"预知未来"，节省部分时间
        //否则那就从0s开始运动，手动sleep(400-arriveTime)s
        //注意这里的0s并不是系统时间的0s，因为系统时间是从1970年1月1日开始算的，所以并不是该线程启动的时间
        //因此这样一来在原来的算法基础上一开始是一定满足超0.4s的要求的，但是这是不合理的
        TimableOutput.println(String.format("ARRIVE-%s-%d",
            ChangeFloor.getFloor(elevator.getCurrentFloor()), elevatorID));
        lastTime = System.currentTimeMillis();
    }

    private void openAndClose() throws InterruptedException {
        TimableOutput.println(String.format("OPEN-%s-%d",
            ChangeFloor.getFloor(elevator.getCurrentFloor()), elevatorID));
        long openTime;
        openTime = System.currentTimeMillis();
        synchronized (processingTable) {
            try {
                processingTable.wait(400);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } //将当前请求列表交出，让该时间内可能进来的人进来
        elevator.out();
        elevator.in(processingTable, isUp, false, new CopyOnWriteArrayList<>());
        long closeTime = System.currentTimeMillis();
        if (closeTime - openTime < 400) {
            Thread.sleep(400 - closeTime + openTime);
        } //确保如果开关门之间的间隔时间小于400ms的话，那么总间隔时间为400ms
        TimableOutput.println(String.format("CLOSE-%s-%d",
            ChangeFloor.getFloor(elevator.getCurrentFloor()), elevatorID));
        lastTime = System.currentTimeMillis();
    }
}
