public class Person {
    private int currentFloor;
    private int toFloor;
    private int personId;
    private int priority;
    private int elevatorId;
    private Long startTime;

    public Person(int fromFloor, int toFloor, int personId, int priority, int elevatorId,
        Long startTime) {
        this.currentFloor = fromFloor;
        this.toFloor = toFloor;
        this.personId = personId;
        this.priority = priority;
        this.elevatorId = elevatorId;
        this.startTime = startTime;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public int getPriority() {
        return priority;
    }

    public int getPersonId() {
        return personId;
    }

    public long getStartTime() {
        return startTime;
    }

    public Person deepCopy() {
        return new Person(currentFloor, toFloor, personId, priority, elevatorId, startTime);
    }

    public boolean isUp() {
        return toFloor > currentFloor;
    }

    public boolean isHigher(int elevatorFloor) {
        return currentFloor > elevatorFloor;
    }

    public boolean isLower(int elevatorFloor) {
        return currentFloor < elevatorFloor;
    }

    public boolean canOpenForOut() {
        return toFloor == currentFloor;
    }

    public boolean canOpenForIn(int nowFloor, boolean isUp) {
        if (nowFloor == currentFloor && isUp == isUp()) {
            return true;
        }
        return false;
    }

    public void move(boolean isUp) {
        currentFloor += isUp ? 1 : -1;
    }
}
