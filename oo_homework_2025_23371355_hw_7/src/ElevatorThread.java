public class ElevatorThread extends Thread {
    private SubTable subTable; //电梯外的当前所处楼层不在目标楼层的人,也就是目前还在分候乘表的人

    public ElevatorThread(SubTable subTable) {
        this.subTable = subTable;
    }

    public void run() {
        while (true) {
            Status status = subTable.run();
            if (status.equals(Status.OVER)) {
                break;
            }
        }
    }
}
