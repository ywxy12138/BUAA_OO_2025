import java.util.ArrayList;
import java.util.HashMap;

public class Strategy {

    private static boolean canTake(Person person, SubTable elevator, TransferArea transferArea) {
        int transferFloor = transferArea.getTraFlo();
        if (elevator.getSide().equals("up")) {
            return (person.isHigher(transferFloor)
                    || (!person.isLower(transferFloor) && person.toHigher(transferFloor)));
        }
        else {
            return (person.isLower(transferFloor)
                    || (!person.isHigher(transferFloor) && person.toLower(transferFloor)));
        }
    }

    private static double cal(Person person, SubTable elevator,
        boolean isDouble, TransferArea transferArea) {
        if (!canTake(person, elevator, transferArea)) {
            return Double.POSITIVE_INFINITY;
        }
        int temp = Math.abs(person.getDistance(elevator.getCurrentFloor()));
        if (temp == 0) {
            if (elevator.isUp() == person.isUp() && !elevator.isFull()) {
                return 0;
            }
            return -1; //同一层但反向或者人满了都不要分配给它
        }
        else {
            return temp * (!isDouble ? 2 : 1) * ((elevator.isUp() == person.isUp()) ? 1 : -1)
                    * elevator.getSize();
        }
        //乘以电梯人数、距离、以及是否反向和是否是单轿厢，(因为双轿厢更快)
    }

    public static double minCom(double minSize, SubTable elevator) {
        return minSize < elevator.getSize() ? minSize : (double)elevator.getSize();
    }

    public static int fluPos(double minSize, SubTable elevator, int minPos) {
        return minSize == (double) elevator.getSize() ? elevator.getEleId() : minPos;
    }

    public static int getBestId(HashMap<Integer, HashMap<String, SubTable>> e, Person p) {
        int pos = 0;
        int minPos = 1;
        synchronized (e.get(1).get("up")) {
            synchronized (e.get(1).get("down")) {
                synchronized (e.get(2).get("up")) {
                    synchronized (e.get(2).get("down")) {
                        synchronized (e.get(3).get("up")) {
                            synchronized (e.get(3).get("down")) {
                                synchronized (e.get(4).get("up")) {
                                    synchronized (e.get(4).get("down")) {
                                        synchronized (e.get(5).get("up")) {
                                            synchronized (e.get(5).get("down")) {
                                                synchronized (e.get(6).get("up")) {
                                                    synchronized (e.get(6).get("down")) {
                                                        double mark = Double.POSITIVE_INFINITY;
                                                        double minSize = Double.POSITIVE_INFINITY;
                                                        for (int i = 1; i <= 6; i++) {
                                                            SubTable eu = e.get(i).get("up");
                                                            SubTable ed = e.get(i).get("down");
                                                            TransferArea t = eu.getArea();
                                                            double temp = (t.getTraFlo() == -5) ?
                                                                cal(p, eu, false, t) : Math.min(
                                                                (Math.max(cal(p, eu, true, t), 0)),
                                                                (Math.max(cal(p, ed, true, t), 0)));
                                                            if (temp <= mark && temp >= 0) {
                                                                mark = temp;
                                                                double calU = cal(p, eu, true, t);
                                                                double calD = cal(p, ed, true, t);
                                                                pos = (Math.min(calU, calD) >= 0) ?
                                                                        ((calU > calD ? ed.getEleId(
                                                                        ) : eu.getEleId())) :
                                                                        (calU >= 0) ? eu.getEleId()
                                                                                : ed.getEleId();
                                                            }
                                                            if (canTake(p, eu, t)) {
                                                                minSize = minCom(minSize, eu);
                                                                minPos = fluPos(minSize, eu,
                                                                        minPos);
                                                            }
                                                            if (canTake(p, ed, t)) {
                                                                minSize = minCom(minSize, ed);
                                                                minPos = fluPos(minSize, ed,
                                                                        minPos);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return (pos == 0) ? minPos : pos;
    }

    public static Status getNextStatus(ArrayList<Person> outPeople,
        Elevator elevator, boolean isUp, TransferArea transferArea, String side) {
        //如果可以开门就开门，不管是放人还是进人
        int transferFloor = transferArea.getTraFlo();
        if (elevator.canOpenForOut(transferFloor, side)
            || elevator.canOpenForIn(outPeople, isUp)) {
            return Status.OPEN;
        }
        //如果此刻电梯里有人
        if (!elevator.isEmpty()) {
            if (elevator.getCurrentFloor() + (isUp ? 1 : -1)
                == transferArea.getTraFlo()) {
                return Status.ROB;
            }
            return Status.MOVE;
        }
        else {
            //如果当前电梯外没有人在等待
            if (outPeople.isEmpty()) {
                //因为在电梯线程中判断了是否完全终止的情况，
                //因此此处，只需要等待调度器的调度即可
                if (transferFloor == elevator.getCurrentFloor()) {
                    return Status.RELEASE;
                }
                return Status.WAIT;
            } else {
                boolean isReverse = isReverse(outPeople,
                    elevator.getCurrentFloor(), isUp);
                if (isReverse) {
                    return Status.REVERSE;
                }
                else {
                    if (elevator.getCurrentFloor() + (isUp ? 1 : -1)
                        == transferArea.getTraFlo()) {
                        return Status.ROB;
                    }
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
