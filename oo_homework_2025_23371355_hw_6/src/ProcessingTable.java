import com.oocourse.elevator2.ScheRequest;
import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;

public class ProcessingTable {
    private ArrayList<Person> people;
    private boolean isEnd;
    private Integer elevatorID;
    private Elevator elevator;
    private long speed;
    private boolean isUp;
    private long lastTime;
    private Status lastStatus;
    private boolean isSchedule;
    private ScheRequest scheRequest;
    private long lastScheduleTime;
    private int scale;

    public ProcessingTable(Integer elevatorID) {
        people = new ArrayList<>();
        isEnd = false;
        this.elevatorID = elevatorID;
        elevator = new Elevator(0, new ArrayList<>());
        speed = 400;
        isUp = true;
        lastTime = System.currentTimeMillis();
        lastStatus = Status.START;
        isSchedule = false;
        lastScheduleTime = System.currentTimeMillis();
        scale = -1;
    }

    public synchronized long getSpeed() {
        return speed;
    }

    public synchronized long getLastScheduleTime() {
        return lastScheduleTime;
    }

    public synchronized int getScale() {
        return scale;
    }

    public synchronized long getLastTime() {
        return lastTime;
    }

    public synchronized ScheRequest getScheRequest() {
        return scheRequest;
    }

    public synchronized Status getLastStatus() {
        return lastStatus;
    }

    public Status run() {
        if (isSchedule()) {
            schedule();
            return lastStatus;
        }
        if ((lastStatus.equals(Status.WAIT) || lastStatus.equals(Status.START))
            && !isEmpty()) {
            synchronized (this) {
                isUp = Strategy.initIsUp(this, elevator);
            }
        }
        Status nextStatus;
        synchronized (this) {
            nextStatus = Strategy.getNextStatus(people, elevator, isUp);
        }
        if (nextStatus.equals(Status.MOVE)) {
            move();
        }
        else if (nextStatus.equals(Status.OPEN)) {
            try {
                openAndClose();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else if (nextStatus.equals(Status.REVERSE)) {
            reverse();
        }
        else if (nextStatus.equals(Status.WAIT)) {
            RequestTable.getInstance().addWaitingElevator(elevatorID);
            if (isEnd()) {
                return Status.OVER;
            }
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        setStatus(nextStatus);
        return nextStatus;
    }

    public synchronized void addPerson(Person person) {
        people.add(person);
        if (people.size() == 1 && !isSchedule && elevator.isEmpty()
            && person.isReceived()) {
            lastTime = System.currentTimeMillis();
            //不在临时调度时接受到第一位乘客的请求，就可以开始计时了
        }
        notifyAll();
    }

    public synchronized void removePerson(Person person) {
        people.remove(person);
        notifyAll();
    }

    public synchronized ArrayList<Person> deepCopyPeople() {
        ArrayList<Person> newPeople = new ArrayList<>();
        for (Person p : people) {
            newPeople.add(p.deepCopy());
        }
        return newPeople;
    }

    public synchronized ArrayList<Person> getPeople() {
        return people;
    }

    public synchronized Elevator getElevator() {
        return elevator;
    }

    public synchronized Boolean isUp() {
        return isUp;
    }

    public synchronized int getTotalPriority() {
        int len = people.size();
        int total = 0;
        for (int i = 0; i < len; i++) {
            Person person = people.get(i);
            total += person.getPriority();
        }
        return total;
    }

    private synchronized void reverse() {
        isUp = !isUp;
        notifyAll();
    }

    private void move() {
        long arriveTime;
        arriveTime = System.currentTimeMillis();
        if (arriveTime - lastTime < speed) {
            try {
                Thread.sleep(speed - arriveTime + lastTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } //防止可能的再次开门
        synchronized (this) {
            elevator.move(isUp);
            TimableOutput.println(String.format("ARRIVE-%s-%d",
                ChangeFloor.getFloor(elevator.getCurrentFloor()), elevatorID));
            setLastTime();
        }
    }

    private void openAndClose() throws Exception {
        long openTime;
        long waitTime;
        boolean isScheduleBegin = lastStatus.equals(Status.SCHEBEGIN);
        synchronized (this) {
            TimableOutput.println(String.format("OPEN-%s-%d",
                ChangeFloor.getFloor(elevator.getCurrentFloor()), elevatorID));
            openTime = System.currentTimeMillis();
            if (isSchedule) {
                double none = elevator.out(0, isUp,
                    ChangeFloor.getFloor(scheRequest.getToFloor()), isScheduleBegin,
                    false, isSchedule);
                if (!isScheduleBegin) {
                    elevator.in(this, isUp, false, null,
                        ChangeFloor.getFloor(scheRequest.getToFloor()), true);
                } //只有不是开始调度后的开门且处于调度状态才可以调度开门,也就是为了把不顺带的人放下而开的门
            } else {
                double none = elevator.out(0, isUp,  0, false, false, isSchedule());
                elevator.in(this, isUp, false, null, 0, isSchedule());
            }
            waitTime = (isScheduleBegin) ? 1000 : 400;
            //只有SCHEBEGIN后的开门需要wait10s
        }
        long closeTime = System.currentTimeMillis();
        if (closeTime - openTime < waitTime) {
            Thread.sleep(waitTime - closeTime + openTime);
        }
        TimableOutput.println(String.format("CLOSE-%s-%d",
            ChangeFloor.getFloor(elevator.getCurrentFloor()), elevatorID));
        setLastTime();
    }

    public synchronized void acceptSchedule(ScheRequest scheRequest, long time) {
        isSchedule = true;
        this.scheRequest = scheRequest;
        lastScheduleTime = time;
        if (isEmpty() && elevator.isEmpty()) {
            lastTime = time;
        }
        notifyAll();
    }

    public void schedule() {
        int targetFloor = ChangeFloor.getFloor(scheRequest.getToFloor());
        long scheduleSpeed = (long) (scheRequest.getSpeed() * 1000);
        boolean scheduleIsUp = (targetFloor > elevator.getCurrentFloor());
        synchronized (this) {
            scale = (lastTime >= lastScheduleTime && lastStatus.equals(Status.MOVE)) ? 1 : 2;
            //临时调度在已经开始移动的时候来的，说明已经移动了一层，则之后只能再移动一层
            if (isUp != scheduleIsUp) {
                isUp = !isUp;
            }
        }
        if (elevator.getCurrentFloor() != targetFloor) {
            if (elevator.canOpenForScheduleOut(isUp, targetFloor) &&
                elevator.getCurrentFloor() != -4 && elevator.getCurrentFloor() != 6) {
                try {
                    openAndClose();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            boolean isBegin = false;
            while (elevator.getCurrentFloor() != targetFloor) {
                if (isBegin) {
                    move();
                } else {
                    if ((scheduleSpeed <= speed) || scale == 0 || (isEmpty())) {
                        TimableOutput.println(String.format("SCHE-BEGIN-%d", elevatorID));
                        synchronized (this) {
                            setSpeed(scheduleSpeed);
                            setStatus(Status.SCHEBEGIN);
                            setLastTime();
                            Strategy.cancelReceive(people);
                            transferSubToMain();
                        } //将开始临时调度后的操作变为原子操作
                        isBegin = true;
                    } else {
                        synchronized (this) {
                            scale--;
                        }
                    }
                    move();
                }
            }
        }
        scheduleBeginThenEnd();
    }

    private void scheduleBeginThenEnd() {
        if (!lastStatus.equals(Status.SCHEBEGIN)) {
            TimableOutput.println(String.format("SCHE-BEGIN-%d", elevatorID));
            synchronized (this) {
                setStatus(Status.SCHEBEGIN);
                setLastTime();
                Strategy.cancelReceive(people);
                transferSubToMain();
            }
        }
        try {
            openAndClose();
            TimableOutput.println(String.format("SCHE-END-%d", elevatorID));
            synchronized (this) {
                setStatus(Status.SCHEEND);
                setSpeed(400);
                finishSchedule();
                if (!people.isEmpty()) {
                    int len = people.size();
                    for (int i = 0; i < len; i++) {
                        Person person = people.get(i);
                        TimableOutput.println(String.format("RECEIVE-%d-%d",
                            person.getPersonId(), elevatorID));
                        if (i == 0) {
                            setLastTime();
                        }
                    }
                    Strategy.receive(people);
                }
                else {
                    setStatus(Status.WAIT);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void transferSubToMain() {
        RequestTable mainTable = RequestTable.getInstance();
        for (Person p : people) {
            mainTable.addPerson(p);
        }
        people.clear();
        notifyAll();
    }

    public synchronized void finishSchedule() {
        isSchedule = false;
        notifyAll();
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }

    private synchronized void setSpeed(long speed) {
        this.speed = speed;
        notifyAll();
    }

    private synchronized void setLastTime() {
        this.lastTime = System.currentTimeMillis();
        notifyAll();
    }

    private synchronized void setStatus(Status status) {
        this.lastStatus = status;
        if (status.equals(Status.WAIT)) {
            RequestTable.getInstance().addWaitingElevator(elevatorID);
        }
        else {
            RequestTable.getInstance().subWaitingElevator(elevatorID);
        }
        //修改电梯状态时都需要对总请求表中的状态进行修改
        notifyAll();
    }

    public synchronized boolean isEnd() {
        return isEnd;
    }

    public synchronized boolean isEmpty() {
        for (Person p : people) {
            if (p.isReceived()) {
                return false;
            }
        }
        return elevator.isEmpty();
    }

    public synchronized boolean isSchedule() {
        return isSchedule;
    }
}