import com.oocourse.spec2.main.PersonInterface;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class NetworkTest {
    private Network network;
    private HashMap<Integer, Person> persons;
    private HashMap<Integer, HashMap<Integer, Person>> friends;
    private Network networkCopy;

    public NetworkTest(Network network, HashMap<Integer, Person> persons, HashMap<Integer, HashMap<Integer, Person>> friends, Network networkCopy) {
        this.network = network;
        this.persons = persons;
        this.friends = friends;
        this.networkCopy = networkCopy;
    }

    @Parameterized.Parameters
    public static Collection data() throws Exception {
        Random random = new Random();
        int testNum = 100;

        Object[][] data = new Object[testNum][];
        for (int j = 0; j < testNum; j++) {
            Network network = new Network();
            Network networkCopy = new Network();
            int peopleNum = random.nextInt(50) + 1;
            HashMap<Integer, HashMap<Integer, Person>> friends = new HashMap<>();
            HashMap<Integer, Person> persons = new HashMap<>();
            for (int i = 1; i <= peopleNum; i++) {
                Person person = new Person(i, String.valueOf(random.nextInt(1000)), random.nextInt(100) + 1);
                network.addPerson(person);
                persons.put(i, person);
                HashMap<Integer, Person> oneFriends = new HashMap<>();
                friends.put(i, oneFriends);
                Person personCopy = new Person(person.getId(), person.getName(), person.getAge());
                networkCopy.addPerson(personCopy);
            }
            for (int i = 1; i < peopleNum; i++) {
                for (int k = i + 1; k <= peopleNum; k++) {
                    if (random.nextDouble() < 0.8) {
                        int value = random.nextInt(200);
                        network.addRelation(i, k, value);
                        networkCopy.addRelation(i, k, value);
                        HashMap<Integer, Person> oneFriends = friends.get(i);
                        oneFriends.put(k, (Person) network.getPerson(k));
                        friends.replace(i, oneFriends);
                        oneFriends = friends.get(k);
                        oneFriends.put(i, (Person) network.getPerson(i));
                        friends.replace(k, oneFriends);
                    }
                }
            }
            data[j] = new Object[]{network, persons, friends, networkCopy};
        }
        return Arrays.asList(data);
    }
    @Test
    public void isCircle() {
    }

    @Test
    public void queryTripleSum() {
    }

    private int getBestId(int personId) {
        int value = Integer.MIN_VALUE;
        int bestId = Integer.MAX_VALUE;
        Person person = persons.get(personId);
        HashMap<Integer, Person> friends = this.friends.get(personId);
        for (Person tempPerson : friends.values()) {
            if (person.queryValue(tempPerson) > value) {
                value = person.queryValue(tempPerson);
                bestId = tempPerson.getId();
            }
            else if (person.queryValue(tempPerson) == value) {
                if (tempPerson.getId() < bestId) {
                    bestId = tempPerson.getId();
                }
            }
        }
        return bestId;
    }

    @Test
    public void queryCoupleSum() throws Exception {
        PersonInterface[] originalPersons = networkCopy.getPersons();
        int calSum = network.queryCoupleSum();
        PersonInterface[] persons = network.getPersons();
        int realSum = 0;
        for (Person person : this.persons.values()) {
            HashMap<Integer, Person> friends = this.friends.get(person.getId());
            int bestFriendId = getBestId(person.getId());
            for (Person tempPerson : friends.values()) {
                if (bestFriendId == tempPerson.getId() && getBestId(tempPerson.getId()) == person.getId()
                && person.getId() < tempPerson.getId()) {
                    realSum++;
                }
            }
        }
        Assert.assertEquals(calSum, realSum);

        for (PersonInterface person : persons) {
            int personId = person.getId();
            for (PersonInterface tempPerson : originalPersons) {
                if (personId == tempPerson.getId()) {
                    Assert.assertTrue(((Person) tempPerson).strictEquals(person));
                }
            }
        }

        for (PersonInterface person : originalPersons) {
            int personId = person.getId();
            for (PersonInterface tempPerson : persons) {
                if (tempPerson.getId() == personId) {
                    Assert.assertTrue(((Person) person).strictEquals(tempPerson));
                }
            }
        }

        Network network1 = new Network();
        Network network2 = new Network();
        Network network3 = new Network();

        for (int i = 1; i < 7; i++) {
            Person person1 = new Person(i, String.valueOf(i), i);
            network1.addPerson(person1);
            Person person2 = new Person(i, String.valueOf(i), i);
            network2.addPerson(person2);
            Person person3 = new Person(i, String.valueOf(i), i);
            network3.addPerson(person3);
        }

        for (int i = 1; i < 6; i++) {
            for (int j = i + 1; j < 7; j++) {
                if (j == i + 1) {
                    network2.addRelation(i, j, 100);
                }
                network3.addRelation(i, j, 100);
            }
        }
        network2.addRelation(1, 6, 100);
        Assert.assertEquals(0, network1.queryCoupleSum());
        Assert.assertEquals(1, network2.queryCoupleSum());
        Assert.assertEquals(1, network3.queryCoupleSum());
        network2.addRelation(2, 4, 150);
        Assert.assertEquals(1, network2.queryCoupleSum());
        network2.modifyRelation(3, 4, 100);
        Assert.assertEquals(1, network2.queryCoupleSum());
        network2.addRelation(2, 5, 150);
        Assert.assertEquals(1, network2.queryCoupleSum());
        network2.modifyRelation(2, 3, 50);
        network2.modifyRelation(3, 4, -200);
        Assert.assertEquals(1, network2.queryCoupleSum());
        Person person7 = new Person(7, String.valueOf(6), 6);
        network3.addPerson(person7);
        network3.addRelation(7, 6, 120);
        Assert.assertEquals(2, network3.queryCoupleSum());
        Person person8 = new Person(8, String.valueOf(6), 6);
        network3.addPerson(person8);
        network3.addRelation(8, 6, 120);
        Assert.assertEquals(2, network3.queryCoupleSum());
        network3.addRelation(2, 8, 120);
        Assert.assertEquals(2, network3.queryCoupleSum());
    }

    @Test
    public void queryShortestPath() {
    }
}