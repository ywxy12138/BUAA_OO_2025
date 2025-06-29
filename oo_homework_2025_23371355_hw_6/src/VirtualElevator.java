import com.oocourse.elevator2.ScheRequest;

import java.util.ArrayList;

public class VirtualElevator {
    private double virtualTime;
    private Elevator elevator;
    private ArrayList<Person> people;
    private boolean isUp;
    private int totalPriority;
    private double averageTime; //记录乘客用时加权优先级的总时间
    private long speed;
    private long lastTime;
    private Status lastStatus;
    private boolean isSchedule;
    private ScheRequest scheRequest;
    private long lastScheduleTime;
    private int scale;

    public VirtualElevator(int totalPriority, boolean isUp, ArrayList<Person> people,
        Elevator elevator, long speed, long lastTime, Status status, boolean isSchedule,
        ScheRequest scheRequest, long lastScheduleTime, int scale, long startTime) {
        this.people = people;
        this.isUp = isUp;
        this.totalPriority = totalPriority;
        virtualTime = (double) startTime;
        averageTime = 0L;
        this.elevator = elevator;
        this.speed = speed;
        this.lastTime = lastTime;
        this.lastStatus = status;
        this.isSchedule = isSchedule;
        this.scheRequest = scheRequest;
        this.lastScheduleTime = lastScheduleTime;
        this.scale = scale;
    }

    public double getTime() {
        return (virtualTime + averageTime);
    }

    private void reverse() {
        isUp = !isUp;
    }

    public void addPerson(Person person) {
        people.add(person);
    }

    public void run() {
        while (!people.isEmpty() || !elevator.isEmpty()) {
            if (isSchedule) {
                schedule();
            }
            Status status = Strategy.getNextStatus(people, elevator, isUp);
            if (status.equals(Status.OPEN)) {
                openAndClose();
            }
            else if (status.equals(Status.MOVE)) {
                move();
            }
            else if (status.equals(Status.REVERSE)) {
                reverse();
            }
            lastStatus = status;
        }
        averageTime = (totalPriority == 0) ? averageTime : averageTime / totalPriority;
    }

    private void schedule() {
        int targetFloor = ChangeFloor.getFloor(scheRequest.getToFloor());
        long scheduleSpeed = (long) (scheRequest.getSpeed() * 1000);
        boolean scheduleIsUp = (targetFloor > elevator.getCurrentFloor());
        scale = (scale == -1) ?
             ((lastTime >= lastScheduleTime && lastStatus.equals(Status.MOVE)) ? 1 : 2) : scale;
        if (isUp != scheduleIsUp) {
            reverse();
        }
        boolean isBegin = lastStatus.equals(Status.SCHEBEGIN);
        if (elevator.getCurrentFloor() != targetFloor) {
            if (elevator.canOpenForScheduleOut(isUp, targetFloor)
                && virtualTime - (double) lastScheduleTime < 400 && !isBegin) {
                //当电梯里有人可以出去
                //且现在与临时调度来的时间差为0.4s
                //且此时并未开始临时调度
                openAndClose();
            }
            while (elevator.getCurrentFloor() != targetFloor) {
                if (isBegin) {
                    move();
                } else {
                    if ((scheduleSpeed <= speed) || scale == 0 || isEmpty()) {
                        speed = scheduleSpeed;
                        lastStatus = Status.SCHEBEGIN;
                        people.clear();
                        isBegin = true;
                        move();
                    } else {
                        move();
                        scale--;
                    }
                }
            }
        }
        if (!lastStatus.equals(Status.SCHEBEGIN)) {
            lastStatus = Status.SCHEBEGIN;
            people.clear();
        }
        openAndClose();
        lastStatus = Status.SCHEEND;
        Strategy.receive(people);
        isSchedule = false;
    }

    public synchronized boolean isEmpty() {
        for (Person p : people) {
            if (p.isReceived()) {
                return false;
            }
        }
        return elevator.isEmpty();
    }

    private void openAndClose() {
        boolean isScheduleBegin = lastStatus.equals(Status.SCHEBEGIN);
        long waitTime = (isScheduleBegin) ? 1000 : 400;
        //只有SCHEBEGIN后的开门需要wait10s
        if (isSchedule) {
            double none = elevator.out(virtualTime, isUp,
                ChangeFloor.getFloor(scheRequest.getToFloor()), isScheduleBegin,
                true, true);
            if (!isScheduleBegin) {
                elevator.in(null, isUp, true, people,
                    ChangeFloor.getFloor(scheRequest.getToFloor()), true);
            } //只有不是开始调度后的开门且处于调度状态才可以调度开门,也就是为了把不顺带的人放下而开的门
        }
        else {
            double none = elevator.out(virtualTime, isUp, 0, false, true, false);
            elevator.in(null, isUp, true, people, 0, false);
        }
        virtualTime += waitTime;
        lastTime = (long) virtualTime;
        lastStatus = Status.OPEN;
    }

    private void move() {
        elevator.move(isUp);
        virtualTime += (double) speed;
        lastTime = (long) virtualTime;
    }
}
