public class Person {
    private int currentFloor;
    private int toFloor;
    private int personId;
    private int priority;
    private int elevatorId;
    private boolean isReceived;

    public Person(int fromFloor, int toFloor, int personId, int priority) {
        this.currentFloor = fromFloor;
        this.toFloor = toFloor;
        this.personId = personId;
        this.priority = priority;
        this.isReceived = false;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public boolean isReceived() {
        return isReceived;
    }

    public int getPriority() {
        return priority;
    }

    public int getPersonId() {
        return personId;
    }

    public boolean getIfArrived() {
        return toFloor == currentFloor;
    }

    public Person deepCopy() {
        return new Person(currentFloor, toFloor, personId, priority);
    }

    public boolean isUp() {
        return toFloor > currentFloor;
    }

    public boolean isHigher(int elevatorFloor) {
        return currentFloor > elevatorFloor;
    }

    public boolean toHigher(int elevatorFloor) {
        return toFloor > elevatorFloor;
    }

    public boolean toLower(int elevatorFloor) {
        return toFloor < elevatorFloor;
    }

    public int getDistance(int elevatorFloor) {
        return elevatorFloor - currentFloor;
    }

    public void setElevatorId(int elevatorId) {
        this.elevatorId = elevatorId;
    }

    public void setIsReceived(boolean isReceived) {
        this.isReceived = isReceived;
    }

    public boolean isLower(int elevatorFloor) {
        return currentFloor < elevatorFloor;
    }

    public boolean canOpenForOut(int transferFloor, String side) {
        if (toFloor == currentFloor) {
            return true;
        }
        if (transferFloor == currentFloor) {
            if (side.equals("up") && !isUp() && toLower(transferFloor)) {
                return true;
            }
            if (side.equals("down") && isUp() && toHigher(transferFloor)) {
                return true;
            }
        }
        return false;
    }

    public boolean canOpenForScheduleOut(boolean isUp, int targetFloor) {
        if (isUp() != isUp) {
            return true;
        }
        if (isUp) {
            if (targetFloor >= toFloor) {
                return false;
            } //可以去的离目的地更近就没必要出去
            return (2 * targetFloor < currentFloor + toFloor);
            //临时调度楼层更靠近当前楼层就可以申请出去
        }
        else {
            if (targetFloor <= toFloor) {
                return false;
            }
            return (2 * targetFloor > currentFloor + toFloor);
        }
    }

    public boolean canOpenForIn(int nowFloor, boolean isUp) {
        return (nowFloor == currentFloor && isUp == isUp());
    }

    public void move(boolean isUp) {
        currentFloor += isUp ? 1 : -1;
    }
}
