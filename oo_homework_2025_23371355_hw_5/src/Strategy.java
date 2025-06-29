import java.util.concurrent.CopyOnWriteArrayList;

public class Strategy {

    public static boolean initIsUp(RequestTable requestTable) {
        //除了刚开始的时候，可以重选向，每次等待之后都可以重选向
        VirtualElevatorThread upElevator =
            new VirtualElevatorThread(0,true);
        VirtualElevatorThread downElevator =
            new VirtualElevatorThread(0,false);
        int maxPriority = 0;
        int minPriority = 100;
        int totalPriority = 0;
        CopyOnWriteArrayList<Person> people = new CopyOnWriteArrayList<>();
        synchronized (requestTable) {
            upElevator.setPeople(requestTable.deepCopy());
            downElevator.setPeople(requestTable.deepCopy());
            people = requestTable.deepCopy();
        }
        for (Person person : people) {
            //if (person.getPriority() > maxPriority) {
            //maxPriority = person.getPriority();
            //}
            //if (person.getPriority() < minPriority) {
            //minPriority = person.getPriority();
            //}
            totalPriority += person.getPriority();
        }
        upElevator.setTotalPriority(totalPriority);
        downElevator.setTotalPriority(totalPriority);
        upElevator.run();
        downElevator.run();
        //if (maxPriority / minPriority >= 5) {
        //最大最小优先级差5倍的话，就以完成时间和平均完成时间为重
        //return downElevator.getTime() > upElevator.getTime();
        //}
        //否则以耗电量为重
        //return downElevator.getElectricUsage() > upElevator.getElectricUsage();
        return downElevator.getTime() > upElevator.getTime();
    }

    public static Status getNextStatus(CopyOnWriteArrayList<Person> outPeople,
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

    private static boolean isReverse(CopyOnWriteArrayList<Person> outPeople, int currentFloor,
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

    public static void sortPriority(CopyOnWriteArrayList<Person> people) {
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

    public static int getMinPriority(CopyOnWriteArrayList<Person> people) {
        int ans = 100;
        for (Person person : people) {
            if (person.getPriority() < ans) {
                ans = person.getPriority();
            }
        }
        return ans;
    }
}
