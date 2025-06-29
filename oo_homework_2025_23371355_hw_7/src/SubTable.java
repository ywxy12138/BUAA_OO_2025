import com.oocourse.elevator3.ScheRequest;
import java.util.ArrayList;

public class SubTable {
    private ArrayList<Person> people;
    private boolean isEnd;
    private Integer elevatorID;
    private Elevator elevator;
    private String side;
    private long speed;
    private boolean isUp;
    private TransferArea transferArea;
    private long lastTime;
    private Status lastStatus;
    private boolean isSchedule;
    private boolean isUpdate;
    private ScheRequest scheRequest;
    private long lastScheduleTime;
    private boolean isScheduleBegin;

    public SubTable(Integer elevatorID) {
        people = new ArrayList<>();
        isEnd = false;
        this.elevatorID = elevatorID;
        elevator = new Elevator(0, new ArrayList<>());
        side = "up";
        speed = 400;
        isUp = true;
        transferArea = new TransferArea();
        lastTime = System.currentTimeMillis();
        lastStatus = Status.START;
        isSchedule = false;
        isUpdate = false;
        lastScheduleTime = System.currentTimeMillis();
        isScheduleBegin = false;
    }

    public synchronized int getSize() {
        return people.size() + elevator.getSize();
    }

    public synchronized int getCurrentFloor() {
        return elevator.getCurrentFloor();
    }

    public synchronized TransferArea getArea() {
        return transferArea;
    }

    public synchronized String getSide() {
        return side;
    }

    public int getEleId() {
        return elevatorID;
    }

    public Status run() {
        if (isSchedule()) {
            schedule();
        }
        if (isUpdate()) {
            try {
                update();
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }
        Status nextStatus;
        synchronized (this) {
            nextStatus = Strategy.getNextStatus(people, elevator, isUp, transferArea, side);
        }
        if (nextStatus.equals(Status.MOVE)) {
            move();
        }
        else if (nextStatus.equals(Status.ROB)) {
            rob();
            setStatus(Status.ROB);
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
        else if (nextStatus.equals(Status.RELEASE)) {
            setStatus(Status.RELEASE);
        }
        else if (nextStatus.equals(Status.WAIT)) {
            setStatus(Status.WAIT);
            if (isEnd() && !isSchedule() && !isUpdate()) {
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
        return nextStatus;
    }

    public synchronized void addPerson(Person person) {
        people.add(person);
        if (people.size() == 1 && !isSchedule && elevator.isEmpty()
            && person.isReceived()) {
            lastTime = System.currentTimeMillis();
            //不在临时调度时接受到第一位乘客的请求，就可以开始计时了
            lastStatus = Status.START;
        }
        notifyAll();
    }

    public synchronized void removePerson(Person person) {
        people.remove(person);
        notifyAll();
    }

    public synchronized ArrayList<Person> getPeople() {
        return people;
    }

    public synchronized Boolean isUp() {
        return isUp;
    }

    public void rob() {
        while (!transferArea.getSide().equals("none")) {
            synchronized (transferArea) {
                try {
                    transferArea.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        transferArea.setSide(side);
        move();
        Status status = run();
        while (elevator.getCurrentFloor() == transferArea.getTraFlo()
                && !status.equals(Status.RELEASE) && !status.equals(Status.WAIT)) {
            status = run();
        }
        if (status.equals(Status.RELEASE)) {
            moveAwayTranFloor();
        }
        transferArea.setSide("none");
    }

    public void moveAwayTranFloor() {
        synchronized (this) {
            isUp = (side.equals("up"));
            notifyAll();
        }
        move();
    }

    private synchronized void reverse() {
        isUp = !isUp;
        setStatus(Status.REVERSE);
        notifyAll();
    }

    public void move() {
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
            Output.printArrive(ChangeFloor.getFloor(elevator.getCurrentFloor()), elevatorID);
            setLastTime();
            setStatus(Status.MOVE);
            notifyAll();
        }
    }

    private void openAndClose() throws Exception {
        long openTime;
        long waitTime;
        synchronized (this) {
            Output.printOpen(ChangeFloor.getFloor(elevator.getCurrentFloor()), elevatorID);
            openTime = System.currentTimeMillis();
            if (isSchedule) {
                double none = elevator.out(0, isUp,
                    ChangeFloor.getFloor(scheRequest.getToFloor()), isScheduleBegin || isUpdate,
                    false, isSchedule, transferArea.getTraFlo(), side);
                if (!isScheduleBegin && !isUpdate) {
                    elevator.in(this, isUp, false, null,
                        ChangeFloor.getFloor(scheRequest.getToFloor()), true);
                } //只有不是开始调度后的开门且处于调度状态才可以调度开门,也就是为了把不顺带的人放下而开的门
            } else {
                double none = elevator.out(0, isUp,  0, isUpdate,
                    false, isSchedule(), transferArea.getTraFlo(), side);
                if (!isUpdate) {
                    elevator.in(this, isUp, false, null, 0, false);
                }
            }
            waitTime = (isScheduleBegin) ? 1000 : 400;
            //只有SCHEBEGIN后的开门需要wait 1s
            setStatus(Status.OPEN);
            notifyAll();
        }
        long closeTime = System.currentTimeMillis();
        if (closeTime - openTime < waitTime) {
            Thread.sleep(waitTime - closeTime + openTime);
        }
        Output.printClose(ChangeFloor.getFloor(elevator.getCurrentFloor()), elevatorID);
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
        int scale;
        synchronized (this) {
            scale = (lastTime >= lastScheduleTime && lastStatus.equals(Status.MOVE)) ? 1 : 2;
            //临时调度在已经开始移动的时候来的，说明已经移动了一层，则之后只能再移动一层
            if (isUp != scheduleIsUp) {
                isUp = !isUp;
            }
            notifyAll();
        }
        if (elevator.getCurrentFloor() != targetFloor) {
            if (elevator.canOpenForScheduleOut(isUp, targetFloor)) {
                try {
                    openAndClose();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            while (elevator.getCurrentFloor() != targetFloor) {
                if (isScheduleBegin) {
                    move();
                } else {
                    if ((scheduleSpeed <= speed) || scale == 0 || (isEmpty())) {
                        Output.printScheduleBegin(elevatorID);
                        synchronized (this) {
                            setSpeed(scheduleSpeed);
                            setStatus(Status.SCHEBEGIN);
                            setLastTime();
                            Strategy.cancelReceive(people);
                            transferSubToMain();
                            notifyAll();
                        } //将开始临时调度后的操作变为原子操作
                        isScheduleBegin = true;
                    } else {
                        scale--;
                    }
                    move();
                }
            }
        }
        scheduleBeginThenEnd();
    }

    private void scheduleBeginThenEnd() {
        if (!isScheduleBegin) {
            Output.printScheduleBegin(elevatorID);
            synchronized (this) {
                setStatus(Status.SCHEBEGIN);
                setLastTime();
                Strategy.cancelReceive(people);
                transferSubToMain();
                notifyAll();
            }
            isScheduleBegin = true;
        }
        try {
            openAndClose();
            Output.printScheduleEnd(elevatorID);
            setLastTime();
            synchronized (this) {
                isScheduleBegin = false; //临时调度结束，将该变量置false，为下次临时调度做准备
                setStatus(Status.SCHEEND);
                setSpeed(400);
                finishSchedule();
                int len = people.size();
                for (int i = 0; i < len; i++) {
                    Person person = people.get(i);
                    isCanTake(person, transferArea.getTraFlo());
                    if (i == 0) {
                        setLastTime();
                    }
                }
                notifyAll();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void acceptUpdate(String side) {
        isUpdate = true;
        this.side = side;
        notifyAll();
    }

    public void update() throws Exception {
        if (!elevator.isEmpty()) {
            openAndClose();
        }
        transferArea.setBegin(side);
        while (!transferArea.getRealBegin()) {
            synchronized (transferArea) {
                transferArea.wait();
            }
        }
        synchronized (this) {
            setStatus(Status.UPDATEBegin);
            Strategy.cancelReceive(people);
            transferSubToMain();
            notifyAll();
        }
        while (!transferArea.getEnd()) {
            synchronized (transferArea) {
                transferArea.wait();
            }
        }
        int transferFloor = transferArea.getTraFlo();
        synchronized (this) {
            setStatus(Status.UPDATEEnd);
            setLastTime();
            elevator.setCurrentFloor(side.equals("up") ? transferFloor + 1 : transferFloor - 1);
            setSpeed(200);
            isUpdate = false;
            int size = people.size();
            for (int i = 0; i < size; i++) {
                Person person = people.get(i);
                isCanTake(person, transferFloor);
                if (i == 0) {
                    setLastTime();
                }
            }
            notifyAll();
        }
    }

    private void isCanTake(Person person, int transferFloor) {
        if (side.equals("up") && (person.isHigher(transferFloor)
            || (!person.isLower(transferFloor) && person.toHigher(transferFloor)))) {
            if (!person.isReceived()) {
                Output.printReceive(person.getPersonId(), elevatorID);
                person.setIsReceived(true);
            }
        }
        else if (side.equals("down") && (person.isLower(transferFloor)
            || (!person.isHigher(transferFloor) && person.toLower(transferFloor)))) {
            if (!person.isReceived()) {
                Output.printReceive(person.getPersonId(), elevatorID);
                person.setIsReceived(true);
            }
        }
        else {
            person.setIsReceived(false);
            RequestTable.getInstance().addPerson(person);
            people.remove(person);
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

    public synchronized void setStatus(Status status) {
        if (status.equals(Status.WAIT) && people.isEmpty() && elevator.isEmpty()) {
            RequestTable.getInstance().addWaitingElevator(elevatorID);
            lastStatus = status;
        }
        else {
            RequestTable.getInstance().subWaitingElevator(elevatorID);
            if ((!status.equals(Status.START)) || lastStatus.equals(Status.WAIT)) {
                lastStatus = status;
            }
        }
        //修改电梯状态时都需要对总请求表中的状态进行修改
        notifyAll();
    }

    public synchronized void setTransferArea(TransferArea transferArea) {
        this.transferArea = transferArea;
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

    public synchronized boolean isUpdate() {
        return isUpdate;
    }

    public synchronized boolean isFull() {
        return elevator.isFull();
    }
}