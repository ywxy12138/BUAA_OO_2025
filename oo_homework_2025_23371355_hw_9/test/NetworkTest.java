import com.oocourse.spec1.main.PersonInterface;
import org.junit.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.management.relation.Relation;
import java.util.*;

@RunWith(Parameterized.class)
public class NetworkTest {
    private Network network;
    private HashMap<Integer, Person> persons;
    private HashMap<Integer, HashMap<Integer, Person>> friends;

    public NetworkTest(Network network, HashMap<Integer, Person> persons, HashMap<Integer, HashMap<Integer, Person>> friends) {
        this.network = network;
        this.persons = persons;
        this.friends = friends;
    }

    @Parameterized.Parameters
    public static Collection data() throws Exception {
        Random random = new Random();
        int testNum = 100;

        //随机生成10组数据进行测试
        Object[][] data = new Object[testNum][];
        for (int j = 0; j < testNum; j++) {
            Network network = new Network();
            int peopleNum = random.nextInt(20) + 1;
//            System.out.printf("第%d组测试\n", j + 1);
//            System.out.println(peopleNum);
            HashMap<Integer, HashMap<Integer, Person>> friends = new HashMap<>();
            HashMap<Integer, Person> persons = new HashMap<>();
            for (int i = 1; i <= peopleNum; i++) {
                Person person = new Person(i, String.valueOf(i), random.nextInt(100) + 1);
                network.addPerson(person);
                persons.put(i, person);
                HashMap<Integer, Person> oneFriends = new HashMap<>();
                friends.put(i, oneFriends);
            }
            int lineNum = random.nextInt(peopleNum) * (random.nextInt(peopleNum) + 1) / 2;
            HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();
            for (int i = 0; i < lineNum; i++) {
                int id1 = random.nextInt(peopleNum) + 1;
                int id2 = random.nextInt(peopleNum) + 1;
                while (id2 == id1) {
                    id2 = random.nextInt(peopleNum) + 1;
                }
                while (true) {
                    if (!map.containsKey(id1)) {
                        break;
                    }
                    else if (!map.get(id1).contains(id2)) {
                        break;
                    }
                    id1 = random.nextInt(peopleNum) + 1;
                    id2 = random.nextInt(peopleNum) + 1;
                    while (id2 == id1) {
                        id2 = random.nextInt(peopleNum) + 1;
                    }
                }
//                System.out.println("-------------");
//                System.out.println("id1:" + id1);
//                System.out.println("id2:" + id2);
//                System.out.println("----------");
                network.addRelation(id1, id2, random.nextInt(1000) + 1);
                HashMap<Integer, Person> oneFriends = friends.get(id1);
                oneFriends.put(id2, (Person) network.getPerson(id2));
                friends.replace(id1, oneFriends);
                oneFriends = friends.get(id2);
                oneFriends.put(id1, (Person) network.getPerson(id1));
                friends.replace(id2, oneFriends);
                ArrayList<Integer> list1 = new ArrayList<>();
                if (map.containsKey(id1)) {
                    list1 = map.get(id1);
                    list1.add(id2);
                    map.replace(id1, list1);
                }
                else {
                    list1.add(id2);
                    map.put(id1, list1);
                }
                ArrayList<Integer> list2 = new ArrayList<>();
                if (map.containsKey(id2)) {
                    list2 = map.get(id2);
                    list2.add(id1);
                    map.replace(id2, list2);
                }
                else {
                    list2.add(id1);
                    map.put(id2, list2);
                }
            }
            data[j] = new Object[]{network, persons, friends};
        }
        return Arrays.asList(data);
    }


    @Test
    public void addRelation() {
    }

    @Test
    public void modifyRelation() {
    }

    @Test
    public void isCircle() throws Exception {
//         /* public normal_behavior
//       requires containsPerson(id1) &&
//                containsPerson(id2);
//                */
//        HashMap<Integer, Person> persons = network.getPersons();
//        int size = persons.size();
//        Random random = new Random();
//        int id1 = random.nextInt(size) + 1;
//        int id2 = random.nextInt(size) + 1;
//        while (id2 == id1) {
//            id2 = random.nextInt(size) + 1;
//        }
//        System.out.println("该组测试的id为" + id1 + "," + id2);
//      /*ensures \result == (\exists PersonInterface[] array; array.length >= 2;
//                           array[0].equals(getPerson(id1)) &&
//                           array[array.length - 1].equals(getPerson(id2)) &&
//                           (\forall int i; 0 <= i && i < array.length - 1;
//                            array[i].isLinked(array[i + 1])));
//      */
//       boolean calResult = network.isCircle(id1, id2);
//       boolean realResult = false;
//       HashMap<Integer, Boolean> visited = new HashMap<>();
//       visited.put(id1, true);
//       HashMap<Integer, Person> friends = network.getPerson(id1).getAcquaintance();
//       if (!friends.isEmpty()) {
//           while (!isAllVisited(friends, visited)) {
//               for (Map.Entry<Integer, Person> entry : friends.entrySet()) {
//                   boolean isContinued = true;
//                   if (!visited.containsKey(entry.getKey())) {
//                       visited.put(entry.getKey(), true);
//                       if (entry.getKey() == id2) {
//                           realResult = true;
//                           break;
//                       }
//                       friends = network.getPerson(entry.getKey()).getAcquaintance();
//                       isContinued = false;
//                   }
//                   if (!isContinued) {
//                       break;
//                   }
//               }
//               if (realResult) {
//                   break;
//               }
//           }
//       }
//       Assert.assertEquals(calResult, realResult);
    }

    private boolean isAllVisited(HashMap<Integer, Person> friends, HashMap<Integer, Boolean> visited) {
        boolean result = true;
        for (Map.Entry<Integer, Person> entry : friends.entrySet()) {
            if (!visited.containsKey(entry.getKey())) {
                result = false;
                break;
            }
        }
        return result;
    }

    @Test
    public void queryTripleSum() throws Exception {

      /* ensures \result ==
               (\sum int i; 0 <= i && i < persons.length;
                   (\sum int j; i < j && j < persons.length;
                       (\sum int k; j < k && k < persons.length
                           && getPerson(persons[i].getId()).isLinked(getPerson(persons[j].getId()))
                           && getPerson(persons[j].getId()).isLinked(getPerson(persons[k].getId()))
                           && getPerson(persons[k].getId()).isLinked(getPerson(persons[i].getId()));
                           1)));
      */
        int calTripleSum = network.queryTripleSum();
        int realTripleSum = 0;
        for (Person person1 : persons.values()) {
            HashMap<Integer, Person> acquaintances1 = friends.get(person1.getId());
            for (Person person2 : acquaintances1.values()) {
                HashMap<Integer, Person> acquaintances2 = friends.get(person2.getId());
                for (Person person3 : acquaintances2.values()) {
                    if (!person3.equals(person1)) {
                        HashMap<Integer, Person> acquaintances3 = friends.get(person3.getId());
                        if (acquaintances3.containsKey(person1.getId())) {
                            realTripleSum++;
                        }
                    }
                }
            }
        }
        realTripleSum /= 6;
        //System.out.println(calTripleSum);
        Assert.assertEquals(calTripleSum, realTripleSum);

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
        Assert.assertEquals(0, network1.queryTripleSum());
        Assert.assertEquals(0, network2.queryTripleSum());
        Assert.assertEquals(20, network3.queryTripleSum());
        network2.addRelation(1, 5, 100);
        Assert.assertEquals(1, network2.queryTripleSum());
        network2.addRelation(2, 5, 100);
        Assert.assertEquals(2, network2.queryTripleSum());
        network2.addRelation(2, 6, 100);
        Assert.assertEquals(4, network2.queryTripleSum());
        network2.addRelation(1, 4, 100);
        Assert.assertEquals(5, network2.queryTripleSum());
        network2.addRelation(1, 3, 100);
        Assert.assertEquals(7, network2.queryTripleSum());
        network2.modifyRelation(2, 6, -150);
        Assert.assertEquals(5, network2.queryTripleSum());
        network2.addRelation(2, 6, 100);
        Assert.assertEquals(7, network2.queryTripleSum());
        network2.addRelation(3, 6, 100);
        Assert.assertEquals(9, network2.queryTripleSum());
        network2.addRelation(3, 5, 100);
        Assert.assertEquals(13, network2.queryTripleSum());
    }

    @Test
    public void queryTagAgeVar() {
    }

    @Test
    public void queryBestAcquaintance() {
    }
}