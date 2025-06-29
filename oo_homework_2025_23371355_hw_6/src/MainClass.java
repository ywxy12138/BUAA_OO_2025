import com.oocourse.elevator2.TimableOutput;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        RequestTable totalTable = RequestTable.getInstance();
        AllElevators allElevators = AllElevators.getInstance();
        for (int i = 1; i < 7; i++) {
            ProcessingTable requestTable = new ProcessingTable(i);
            ElevatorThread elevatorThread = new ElevatorThread(requestTable);
            AllElevators.getInstance().addElevator(requestTable, i);
            elevatorThread.start(); //创建并开启一个电梯线程
        }
        DispatchThread dispatchThread = new DispatchThread(totalTable);
        dispatchThread.start(); //创建并开启一个调度器线程
        InputThread inputThread = new InputThread(totalTable);
        inputThread.start(); //创建并开启一个输入线程
    }
}
