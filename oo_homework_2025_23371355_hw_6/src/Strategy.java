import java.util.ArrayList;
import java.util.HashMap;

public class Strategy {

    public static boolean initIsUp(ProcessingTable requestTable, Elevator elevator) {
        //除了刚开始的时候，可以重选向，每次等待之后都可以重选向
        VirtualElevator upElevator;
        VirtualElevator downElevator;
        synchronized (requestTable) {
            long nowTime = System.currentTimeMillis();
            upElevator = new VirtualElevator(requestTable.getTotalPriority(), true,
                    requestTable.deepCopyPeople(), elevator.deepCopy(), 400, nowTime, Status.WAIT,
                    false, null, nowTime, 0, nowTime);
            downElevator = new VirtualElevator(requestTable.getTotalPriority(), false,
                    requestTable.deepCopyPeople(), elevator.deepCopy(), 400, nowTime, Status.WAIT,
                    false, null, nowTime, 0, nowTime);
        }
        upElevator.run();
        downElevator.run();
        return downElevator.getTime() > upElevator.getTime();
    }

    public static int getBestId(HashMap<Integer, ProcessingTable> elevators,
        Person person) {
        int pos = 1;
        HashMap<Integer, VirtualElevator> virtualElevators = new HashMap<>();
        synchronized (elevators.get(1)) {
            synchronized (elevators.get(2)) {
                synchronized (elevators.get(3)) {
                    synchronized (elevators.get(4)) {
                        synchronized (elevators.get(5)) {
                            synchronized (elevators.get(6)) {
                                long nowTime = System.currentTimeMillis();
                                for (int i = 1; i < 7; i++) {
                                    ProcessingTable realElevator = elevators.get(i);
                                    VirtualElevator virtualElevator = new VirtualElevator(
                                        realElevator.getTotalPriority(), realElevator.isUp(),
                                        realElevator.deepCopyPeople(),
                                        realElevator.getElevator().deepCopy(),
                                        realElevator.getSpeed(), realElevator.getLastTime(),
                                        realElevator.getLastStatus(), realElevator.isSchedule(),
                                        realElevator.getScheRequest(),
                                        realElevator.getLastScheduleTime(),
                                        realElevator.getScale(), nowTime);
                                    virtualElevators.put(i, virtualElevator);
                                }
                            }
                        }
                    }
                }
            }
        }
        double minTime = 0;
        for (int i = 1; i < 7; i++) {
            VirtualElevator virtualElevator = virtualElevators.get(i);
            virtualElevator.addPerson(person.deepCopy());
            virtualElevator.run();
            double time = virtualElevator.getTime();
            if (i == 1) {
                minTime = time;
            } else {
                if (time < minTime) {
                    minTime = time;
                    pos = i;
                }
            }
        }
        return pos;
    }

    public static Status getNextStatus(ArrayList<Person> outPeople,
        Elevator elevator, boolean isUp) {
        //如果可以开门就开门，不管是放人还是进人
        if (elevator.canOpenForOut() || elevator.canOpenForIn(outPeople, isUp)) {
            return Status.OPEN;
        }
        //如果此刻电梯里有人
        if (!elevator.isEmpty()) {
            return Status.MOVE;
        }
        else {
            //如果当前电梯外没有人在等待
            if (outPeople.isEmpty()) {
                //因为在电梯线程中判断了是否完全终止的情况，
                //因此此处，只需要等待调度器的调度即可
                return Status.WAIT;
            } else {
                boolean isReverse = isReverse(outPeople,
                    elevator.getCurrentFloor(), isUp);
                if (isReverse) {
                    return Status.REVERSE;
                }
                else {
                    return Status.MOVE;
                }
            }
        }
    }

    public static boolean isReverse(ArrayList<Person> outPeople, int currentFloor,
        boolean isUp) {
        if (isUp) {
            for (Person person : outPeople) {
                if (person.isHigher(currentFloor)) {
                    return false;
                }
            }
        }
        else {
            for (Person person : outPeople) {
                if (person.isLower(currentFloor)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void sortPriority(ArrayList<Person> people) {
        int len = people.size();
        for (int i = 0; i < len - 1; i++) {
            int flag = 0;
            for (int j = i + 1; j < len; j++) {
                if (people.get(i).getPriority() < people.get(j).getPriority()) {
                    flag = 1;
                    Person temp = people.get(i);
                    people.set(i, people.get(j));
                    people.set(j, temp);
                }
            }
            if (flag == 0) {
                break;
            }
        }
    }

    public static int getMinPriority(ArrayList<Person> people) {
        int ans = 100;
        for (Person person : people) {
            if (person.getPriority() < ans) {
                ans = person.getPriority();
            }
        }
        return ans;
    }

    public static void cancelReceive(ArrayList<Person> people) {
        for (Person p : people) {
            p.setIsReceived(false);
        }
    }

    public static void receive(ArrayList<Person> people) {
        for (Person person : people) {
            person.setIsReceived(true);
        }
    }
}
