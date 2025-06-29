public class ElevatorThread extends Thread {
    private ProcessingTable processingTable; //电梯外的当前所处楼层不在目标楼层的人,也就是目前还在分候乘表的人

    public ElevatorThread(ProcessingTable processingTable) {
        this.processingTable = processingTable;
    }

    public void run() {
        while (true) {
            Status status = processingTable.run();
            if (status.equals(Status.OVER)) {
                break;
            }
        }
    }
}
