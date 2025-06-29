import com.oocourse.elevator3.TimableOutput;

public class Output {

    public static void printReceive(int personId, int elevatorId) {
        TimableOutput.println(String.format("RECEIVE-%d-%d", personId, elevatorId));
    }

    public static void printOutS(int personId, String floor, int elevatorId) {
        TimableOutput.println(String.format("OUT-S-%d-%s-%d", personId, floor, elevatorId));
    }

    public static void printOutF(int personId, String floor, int elevatorId) {
        TimableOutput.println(String.format("OUT-F-%d-%s-%d", personId, floor, elevatorId));
    }

    public static void printIn(int personId, String floor, int elevatorId) {
        TimableOutput.println(String.format("IN-%d-%s-%d", personId, floor, elevatorId));
    }

    public static void printArrive(String floor, int elevatorId) {
        TimableOutput.println(String.format("ARRIVE-%s-%d", floor, elevatorId));
    }

    public static void printOpen(String floor, int elevatorId) {
        TimableOutput.println(String.format("OPEN-%s-%d", floor, elevatorId));
    }

    public static void printClose(String floor, int elevatorId) {
        TimableOutput.println(String.format("CLOSE-%s-%d", floor, elevatorId));
    }

    public static void printScheduleBegin(int elevatorId) {
        TimableOutput.println(String.format("SCHE-BEGIN-%d", elevatorId));
    }

    public static void printScheduleEnd(int elevatorId) {
        TimableOutput.println(String.format("SCHE-END-%d", elevatorId));
    }

    public static void printUpdateBegin(int elevatorAId, int elevatorBId) {
        TimableOutput.println(String.format("UPDATE-BEGIN-%d-%d", elevatorAId, elevatorBId));
    }

    public static void printUpdateEnd(int elevatorAId, int elevatorBId) {
        TimableOutput.println(String.format("UPDATE-END-%d-%d", elevatorAId, elevatorBId));
    }
}
