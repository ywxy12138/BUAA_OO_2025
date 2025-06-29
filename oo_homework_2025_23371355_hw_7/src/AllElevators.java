import com.oocourse.elevator3.ScheRequest;
import com.oocourse.elevator3.UpdateRequest;

import java.util.HashMap;

public class AllElevators {
    private static AllElevators allElevators = null;
    private HashMap<Integer, HashMap<String, SubTable>> elevators;
    private int count = 0;

    public static AllElevators getInstance() {
        if (allElevators == null) {
            allElevators = new AllElevators();
        }
        return allElevators;
    }

    private AllElevators() {
        elevators = new HashMap<>();
    }

    public void addElevator(SubTable elevator, Integer id) {
        HashMap<String, SubTable> twinElevators = new HashMap<>();
        twinElevators.put("up", elevator);
        twinElevators.put("down", elevator);
        elevators.put(id, twinElevators);
    }

    public void acceptSchedule(ScheRequest scheRequest, long time) {
        int elevatorId = scheRequest.getElevatorId();
        SubTable upElevator = elevators.get(elevatorId).get("up");
        SubTable downElevator = elevators.get(elevatorId).get("down");
        if (upElevator.getEleId() == elevatorId) {
            upElevator.acceptSchedule(scheRequest, time);
        }
        else if (downElevator.getEleId() == elevatorId) {
            downElevator.acceptSchedule(scheRequest, time);
        }
    }

    public void update(UpdateRequest updateRequest) {
        int updateAId = updateRequest.getElevatorAId();
        int updateBId = updateRequest.getElevatorBId();
        int transferFloor = ChangeFloor.getFloor(updateRequest.getTransferFloor());
        elevators.get(updateAId).replace("down", elevators.get(updateBId).get("down"));
        elevators.get(updateBId).replace("up", elevators.get(updateAId).get("up"));
        SubTable elevatorA = elevators.get(updateAId).get("up");
        SubTable elevatorB = elevators.get(updateAId).get("down");
        synchronized (elevatorA) {
            elevatorA.getArea().setTransferFloor(transferFloor);
            elevatorA.acceptUpdate("up");
            elevatorA.notifyAll();
        }
        synchronized (elevatorB) {
            elevatorB.setTransferArea(elevatorA.getArea()); //共享换乘层
            elevatorB.acceptUpdate("down");
            elevatorB.notifyAll();
        }
        TransferArea transferArea = elevatorA.getArea();
        while (!transferArea.getBegin()) {
            synchronized (transferArea) {
                try {
                    transferArea.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        Output.printUpdateBegin(elevatorA.getEleId(), elevatorB.getEleId());
        transferArea.setRealBegin();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Output.printUpdateEnd(elevatorA.getEleId(), elevatorB.getEleId());
        transferArea.setEnd("up");
        transferArea.setEnd("down");
    }

    public void dispatch(Person person) {
        int id = (count % 6) + 1;
        boolean isDispatched = false;
        while (!isDispatched) {
            SubTable upElevator = elevators.get(id).get("up");
            SubTable downElevator = elevators.get(id).get("down");
            int transferFloor = upElevator.getArea().getTraFlo();
            if (person.isHigher(transferFloor) || (!person.isLower(transferFloor)
                && person.toHigher(transferFloor))) {
                subDispatch(person, upElevator, upElevator.getEleId());
                isDispatched = true;
            } else if (person.isLower(transferFloor) || (!person.isHigher(transferFloor)
                && person.toLower(transferFloor))) {
                subDispatch(person, downElevator, downElevator.getEleId());
                isDispatched = true;
            }
            id = (count % 6) + 1;
            count++;
        }
    }

    public void subDispatch(Person person, SubTable elevator, int id) {
        synchronized (elevator) {
            elevator.setStatus(Status.START);
            if (!elevator.isSchedule() && !elevator.isUpdate()) {
                Output.printReceive(person.getPersonId(), id);
                person.setIsReceived(true);
            } else {
                person.setIsReceived(false);
            }
            person.setElevatorId(id);
            elevator.addPerson(person);
            elevator.notifyAll();
        }
        //分配的时候要锁住，不然可能会出现奇怪的bug，比如分配和接受临时调度同时进行的时候
    }

    public void setEnd() {
        for (HashMap<String, SubTable> elevator : elevators.values()) {
            elevator.get("up").setEnd();
            elevator.get("down").setEnd();
        }
    }
}
