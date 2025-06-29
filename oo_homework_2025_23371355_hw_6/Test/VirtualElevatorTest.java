import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class VirtualElevatorTest {

    @Test
    public void run() {
        Person person = new Person(4, -3, 665, 99);
        ArrayList<Person> people = new ArrayList<>();
        people.add(person);
        long now = System.currentTimeMillis();
        VirtualElevator virtualElevator = new VirtualElevator(99, true, people,
                new Elevator(0, new ArrayList<>()), 400, now, Status.WAIT, false,
                null, now, 0, now);
        virtualElevator.run();
        assert(people.size() == 1);
    }
}