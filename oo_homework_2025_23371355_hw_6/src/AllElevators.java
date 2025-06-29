import com.oocourse.elevator2.ScheRequest;
import com.oocourse.elevator2.TimableOutput;

import java.util.HashMap;

public class AllElevators {
    private static AllElevators allElevators = null;
    private HashMap<Integer, ProcessingTable> elevators;

    public static AllElevators getInstance() {
        if (allElevators == null) {
            allElevators = new AllElevators();
        }
        return allElevators;
    }

    private AllElevators() {
        elevators = new HashMap<>();
    }

    public void addElevator(ProcessingTable elevator, Integer id) {
        elevators.put(id, elevator);
    }

    public void acceptSchedule(ScheRequest scheRequest, long time) {
        int elevatorId = scheRequest.getElevatorId();
        ProcessingTable elevator = elevators.get(elevatorId);
        elevator.acceptSchedule(scheRequest, time);
    }

    public void dispatch(Person person) {
        int id = Strategy.getBestId(elevators, person);
        ProcessingTable elevator = elevators.get(id);
        synchronized (elevator) {
            if (!elevator.isSchedule()) {
                TimableOutput.println(String.format("RECEIVE-%d-%d", person.getPersonId(), id));
                person.setIsReceived(true);
            } else {
                person.setIsReceived(false);
            }
            person.setElevatorId(id);
            elevator.addPerson(person);
        } //分配的时候要锁住，不然可能会出现奇怪的bug，比如分配和接受临时调度同时进行的时候
    }

    public void setEnd() {
        for (ProcessingTable elevator : elevators.values()) {
            elevator.setEnd();
        }
    }
}
