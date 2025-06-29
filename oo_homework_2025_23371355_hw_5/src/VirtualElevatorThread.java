import java.util.concurrent.CopyOnWriteArrayList;

public class VirtualElevatorThread {
    private double virtualTime;
    private Elevator elevator;
    private CopyOnWriteArrayList<Person> people;
    private boolean isUp;
    private int totalPriority;
    private double averageTime; //记录乘客用时加权优先级的总时间
    private double electricUsage;

    public VirtualElevatorThread(int totalPriority, boolean isUp) {
        this.people = new CopyOnWriteArrayList<>();
        this.isUp = isUp;
        this.totalPriority = totalPriority;
        virtualTime = 0L;
        averageTime = 0L;
        electricUsage = 0;
        elevator = new Elevator();
    }

    public double getTime() {
        return (virtualTime + averageTime);
    }

    public double getElectricUsage() {
        return electricUsage;
    }

    public void setPeople(CopyOnWriteArrayList<Person> people) {
        this.people = people;
    }

    public void setTotalPriority(int totalPriority) {
        this.totalPriority = totalPriority;
    }

    public void run() {
        while (!people.isEmpty() || !elevator.isEmpty()) {
            Status status = Strategy.getNextStatus(people, elevator, isUp);
            if (status.equals(Status.OPEN)) {
                openAndClose();
            }
            else if (status.equals(Status.MOVE)) {
                move();
            }
            else if (status.equals(Status.REVERSE)) {
                isUp = !isUp;
            }
        }
        averageTime /= totalPriority;
    }

    private void openAndClose() {
        averageTime += elevator.out(virtualTime);//下乘客了，加一个时间
        elevator.in(new RequestTable(), isUp, true, people);
        virtualTime += 400; //将虚拟时间加上开关门的用时
        electricUsage += 0.2;
    }

    private void move() {
        elevator.move(isUp);
        virtualTime += 400; //虚拟电梯移动一层所需时间
        electricUsage += 0.4;
    }
}
