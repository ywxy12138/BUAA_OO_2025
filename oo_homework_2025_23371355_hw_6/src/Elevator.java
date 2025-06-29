import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;

public class Elevator {
    private int currentFloor;
    private static final int capacity = 6;
    private ArrayList<Person> inPeople;//电梯内的人

    public Elevator(int currentFloor, ArrayList<Person> inPeople) {
        this.currentFloor = currentFloor;
        this.inPeople = inPeople;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public boolean isEmpty() {
        return inPeople.isEmpty();
    }

    public boolean canOpenForScheduleOut(boolean isUp, int targetFloor) {
        for (Person person : inPeople) {
            if (person.canOpenForScheduleOut(isUp, targetFloor)) {
                return true;
            }
        }
        return false;
    }

    public boolean canOpenForOut() {
        for (Person person : inPeople) {
            if (person.canOpenForOut()) {
                return true;
            }
        }
        return false;
    }

    public double out(double virtualTime, boolean isUp, int targetFloor,
        boolean isBegin, boolean isVirtual, boolean isSchedule) {
        int len = inPeople.size();
        ArrayList<Person> newInPeople = new ArrayList<>();
        double totalTime = 0;
        for (int i = 0; i < len; i++) {
            Person person = inPeople.get(i);
            if (!isSchedule) {
                if (person.canOpenForOut()) {
                    if (!isVirtual) {
                        TimableOutput.println(String.format("OUT-S-%d-%s-%d", person.getPersonId(),
                            ChangeFloor.getFloor(currentFloor), person.getElevatorId()));
                    }
                    else {
                        totalTime += virtualTime * person.getPriority();
                    }
                    person.setIsReceived(false);
                }
                else {
                    newInPeople.add(person);
                }
            }
            else {
                if (isBegin || person.canOpenForScheduleOut(isUp, targetFloor)) {
                    if (person.canOpenForOut()) {
                        if (!isVirtual) {
                            TimableOutput.println(String.format("OUT-S-%d-%s-%d",
                                person.getPersonId(), ChangeFloor.getFloor(currentFloor),
                                person.getElevatorId()));
                        } else {
                            totalTime += virtualTime * person.getPriority();
                        }
                    } else {
                        if (!isVirtual) {
                            TimableOutput.println(String.format("OUT-F-%d-%s-%d",
                                person.getPersonId(), ChangeFloor.getFloor(currentFloor),
                                person.getElevatorId()));
                            person.setIsReceived(false);
                            RequestTable.getInstance().addPerson(person);
                        }
                    }
                } else {
                    newInPeople.add(person);
                }
            }
        }
        inPeople = newInPeople;
        return totalTime;
    }

    public boolean canOpenForIn(ArrayList<Person> people, boolean isUp) {
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

    public void in(ProcessingTable requestTable, boolean isUp,
        boolean isVirtual, ArrayList<Person> virtualPeople,
        int targetFloor, boolean isSchedule) {
        ArrayList<Person> nowFloorAllPeople = new ArrayList<>();
        ArrayList<Person> outPeople = (isVirtual) ?
            virtualPeople : requestTable.getPeople();
        for (Person person : outPeople) {
            if (person.canOpenForIn(currentFloor, isUp)) {
                if ((!isSchedule || !person.canOpenForScheduleOut(isUp, targetFloor))) {
                    nowFloorAllPeople.add(person);
                }
            }
        }
        nowFloorAllPeople.addAll(this.inPeople);
        Strategy.sortPriority(nowFloorAllPeople);
        int allLen = nowFloorAllPeople.size();
        int len = Math.min(allLen, capacity);
        ArrayList<Person> newInPeople = new ArrayList<>();
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
                    TimableOutput.println(String.format("OUT-F-%d-%s-%d", person.getPersonId(),
                        ChangeFloor.getFloor(currentFloor), person.getElevatorId()));
                    person.setIsReceived(false);
                    RequestTable.getInstance().addPerson(person);
                }
            }
        }
        len = newInPeople.size();
        for (int i = 0; i < len; i++) {
            Person person = newInPeople.get(i);
            if (!inPeople.contains(person)) {
                if (!isVirtual) {
                    if (!person.isReceived()) {
                        TimableOutput.println(String.format("RECEIVE-%d-%d",
                            person.getPersonId(), person.getElevatorId()));
                        person.setIsReceived(true);
                    }
                    TimableOutput.println(String.format("IN-%d-%s-%d", person.getPersonId(),
                        ChangeFloor.getFloor(currentFloor), person.getElevatorId()));
                    requestTable.removePerson(person);
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

    public Elevator deepCopy() {
        ArrayList<Person> people = new ArrayList<>();
        for (Person p : inPeople) {
            people.add(p.deepCopy());
        }
        return new Elevator(currentFloor, people);
    }

}
