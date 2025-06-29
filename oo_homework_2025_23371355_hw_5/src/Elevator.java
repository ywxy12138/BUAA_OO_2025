import com.oocourse.elevator1.TimableOutput;

import java.util.concurrent.CopyOnWriteArrayList;

public class Elevator {
    private int currentFloor;
    private static final int capacity = 6;
    private static final int velocity = 400;
    private CopyOnWriteArrayList<Person> inPeople;//电梯内的人

    public Elevator() {
        currentFloor = 0;
        inPeople = new CopyOnWriteArrayList<>();
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public boolean isEmpty() {
        return inPeople.isEmpty();
    }

    public boolean canOpenForOut() {
        for (Person person : inPeople) {
            if (person.canOpenForOut()) {
                return true;
            }
        }
        return false;
    }

    public void out() {
        int len = inPeople.size();
        CopyOnWriteArrayList<Person> newInPeople = new CopyOnWriteArrayList<>();
        for (int i = 0; i < len; i++) {
            Person person = inPeople.get(i);
            if (person.canOpenForOut()) {
                TimableOutput.println(String.format("OUT-%d-%s-%d", person.getPersonId(),
                    ChangeFloor.getFloor(currentFloor), person.getElevatorId()));
            }
            else {
                newInPeople.add(person);
            }
        }
        inPeople = newInPeople;
    }

    public double out(double virtualTime) {
        int len = inPeople.size();
        CopyOnWriteArrayList<Person> newInPeople = new CopyOnWriteArrayList<>();
        double totalTime = 0;
        for (int i = 0; i < len; i++) {
            Person person = inPeople.get(i);
            if (person.canOpenForOut()) {
                totalTime += virtualTime * person.getPriority();
            }
            else {
                newInPeople.add(person);
            }
        }
        inPeople = newInPeople;
        return totalTime;
    }

    public boolean canOpenForIn(CopyOnWriteArrayList<Person> people, boolean isUp) {
        boolean isWantToIn = false; //电梯外的人是否愿意进来
        int maxOutPriority = 0; //电梯外的愿意进来的人中的最大优先级
        for (Person person : people) {
            if (person.canOpenForIn(currentFloor, isUp)) {
                isWantToIn = true;
                if (person.getPriority() > maxOutPriority) {
                    maxOutPriority = person.getPriority();
                }
            }
        }
        if (inPeople.size() < capacity) {
            return isWantToIn;
        }
        //电梯内人数已满
        int minInPriority = Strategy.getMinPriority(inPeople); //电梯内所有人的最小优先级
        return maxOutPriority > minInPriority; //电梯外有的人优先级比电梯内某人优先级更高
    }

    public void in(RequestTable requestTable, boolean isUp,
        boolean isVirtual, CopyOnWriteArrayList<Person> virtualPeople) {
        CopyOnWriteArrayList<Person> nowFloorAllPeople = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<Person> outPeople = (isVirtual) ?
            virtualPeople : requestTable.getPeople();
        for (Person person : outPeople) {
            if (person.canOpenForIn(currentFloor, isUp)) {
                nowFloorAllPeople.add(person);
            }
        }
        nowFloorAllPeople.addAll(this.inPeople);
        Strategy.sortPriority(nowFloorAllPeople);
        int allLen = nowFloorAllPeople.size();
        int len = Math.min(allLen, capacity);
        CopyOnWriteArrayList<Person> newInPeople = new CopyOnWriteArrayList<>();
        for (int i = 0; i < len; i++) {
            Person person = nowFloorAllPeople.get(i);
            newInPeople.add(person);
        }
        //先让人出电梯再让人进电梯
        len = inPeople.size();
        for (int i = 0; i < len; i++) {
            Person person = inPeople.get(i);
            if (!newInPeople.contains(person)) {
                if (!isVirtual) {
                    TimableOutput.println(String.format("OUT-%d-%s-%d", person.getPersonId(),
                        ChangeFloor.getFloor(currentFloor), person.getElevatorId()));
                    requestTable.addRequest(person);
                }
                else {
                    virtualPeople.add(person);
                }
            }
        }
        len = newInPeople.size();
        for (int i = 0; i < len; i++) {
            Person person = newInPeople.get(i);
            if (!inPeople.contains(person)) {
                if (!isVirtual) {
                    TimableOutput.println(String.format("IN-%d-%s-%d", person.getPersonId(),
                        ChangeFloor.getFloor(currentFloor), person.getElevatorId()));
                    requestTable.removeRequest(person);
                }
                else {
                    virtualPeople.remove(person);
                }
            }
        }
        inPeople = newInPeople; //交换电梯厢()，不然等于白进出(挺瘆人的)
    }

    public void move(boolean isUp) {
        currentFloor += isUp ? 1 : -1;
        for (Person p : inPeople) {
            p.move(isUp);
        }
    }

}
