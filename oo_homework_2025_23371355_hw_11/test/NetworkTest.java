import com.oocourse.spec3.main.MessageInterface;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

@RunWith(Parameterized.class)
public class NetworkTest {
    private Network network;
    private HashMap<Integer, Person> persons;
    private HashMap<Integer, HashMap<Integer, Person>> friends;
    private HashMap<Integer, Message> messagesCopy;

    private HashMap<Integer, HashSet<Integer>> emojiIdListCopy;
    private HashMap<Integer, Integer> emojiHeatListCopy;


    public NetworkTest(Network network, HashMap<Integer, Person> persons,
                       HashMap<Integer, HashMap<Integer, Person>> friends,
                       HashMap<Integer, Message> messagesCopy, HashMap<Integer, HashSet<Integer>> emojiIdListCopy,
                       HashMap<Integer, Integer> emojiHeatListCopy) {
        this.network = network;
        this.persons = persons;
        this.friends = friends;
        this.messagesCopy = messagesCopy;
        this.emojiIdListCopy = emojiIdListCopy;
        this.emojiHeatListCopy = emojiHeatListCopy;
    }

    @Parameterized.Parameters
    public static Collection data() throws Exception {
        Random random = new Random();
        int testNum = 100;

        Object[][] data = new Object[testNum][];
        for (int j = 0; j < testNum; j++) {
            Network network = new Network();
            int peopleNum = random.nextInt(50) + 1;
            HashMap<Integer, HashMap<Integer, Person>> friends = new HashMap<>();
            HashMap<Integer, Person> persons = new HashMap<>();
            HashMap<Integer, Message> messages = new HashMap<>();
            HashMap<Integer, HashSet<Integer>> emojiIdList = new HashMap<>();
            HashMap<Integer, Integer> emojiHeatList = new HashMap<>();
            for (int i = 1; i <= peopleNum; i++) {
                Person person = new Person(i, String.valueOf(random.nextInt(1000)), random.nextInt(100) + 1);
                network.addPerson(person);
                persons.put(i, person);
                HashMap<Integer, Person> oneFriends = new HashMap<>();
                friends.put(i, oneFriends);
            }
            for (int i = 1; i < peopleNum; i++) {
                int tagIsFound = 0;
                Tag tag = new Tag(i);
                if (random.nextDouble() < 0.8) {
                    tagIsFound = 1;
                    network.addTag(i, tag);
                }
                network.createOfficialAccount(i, i, String.valueOf(i));
                for (int k = i + 1; k <= peopleNum; k++) {
                    int messageId = (i - 1) * peopleNum + k;
                    int type = (random.nextDouble() < 0.5) ? 1 : 0;
                    double percent = random.nextDouble();
                    Message message;
                    if (percent < 0.3) {
                        network.contributeArticle(i, i, messageId);
                        if (type == 1 && tagIsFound == 1) {
                            message =
                                    new ForwardMessage(messageId, messageId, network.getPerson(i), tag);
                        }
                        else {
                            message =
                                    new ForwardMessage(messageId, messageId, network.getPerson(i), network.getPerson(k));
                        }
                    }
                    else if (percent < 0.6) {
                        if (type == 1 && tagIsFound == 1) {
                            message =
                                    new RedEnvelopeMessage(messageId, messageId, network.getPerson(i), tag);
                        }
                        else {
                            message =
                                    new RedEnvelopeMessage(messageId, messageId, network.getPerson(i), network.getPerson(k));
                        }
                    }
                    else {
                        if (type == 1 && tagIsFound == 1) {
                            message =
                                    new EmojiMessage(messageId, messageId, network.getPerson(i), tag);
                        }
                        else {
                            message =
                                    new EmojiMessage(messageId, messageId, network.getPerson(i), network.getPerson(k));
                        }
                        network.storeEmojiId(messageId);
                        emojiIdList.put(messageId, new HashSet<>());
                        emojiHeatList.put(messageId, 0);
                    }
                    network.addMessage(message);
                    if (message instanceof EmojiMessage) {
                        HashSet<Integer> set = emojiIdList.get(messageId);
                        set.add(messageId);
                        emojiIdList.put(messageId, set);
                    }
                    messages.put(messageId, message);
                    if (random.nextDouble() < 0.9) {
                        int value = random.nextInt(200);
                        network.addRelation(i, k, value);
                        HashMap<Integer, Person> oneFriends = friends.get(i);
                        oneFriends.put(k, (Person) network.getPerson(k));
                        friends.replace(i, oneFriends);
                        oneFriends = friends.get(k);
                        oneFriends.put(i, (Person) network.getPerson(i));
                        friends.replace(k, oneFriends);
                    }
                    if (network.getPerson(i).isLinked(network.getPerson(k))) {
                        int isAddToTag = random.nextDouble() < 0.8 ? 1 : 0;
                        if (isAddToTag == 1 && tagIsFound == 1) {
                            network.addPersonToTag(k, i, tag.getId());
                        }
                        if (random.nextDouble() < 0.9) {
                            if ((type == 1 && tagIsFound == 1) || type == 0) {
                                network.sendMessage(messageId);
                                if (message instanceof EmojiMessage) {
                                    emojiHeatList.put(messageId, emojiHeatList.get(messageId) + 1);
                                }
                                messages.remove(messageId);
                            }
                        }
                    }
                }
            }
            data[j] = new Object[]{network, persons, friends, messages, emojiIdList, emojiHeatList};
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
//        PersonInterface[] originalPersons = networkCopy.getPersons();
//        int calSum = network.queryCoupleSum();
//        PersonInterface[] persons = network.getPersons();
//        int realSum = 0;
//        for (Person person : this.persons.values()) {
//            HashMap<Integer, Person> friends = this.friends.get(person.getId());
//            int bestFriendId = getBestId(person.getId());
//            for (Person tempPerson : friends.values()) {
//                if (bestFriendId == tempPerson.getId() && getBestId(tempPerson.getId()) == person.getId()
//                && person.getId() < tempPerson.getId()) {
//                    realSum++;
//                }
//            }
//        }
//        Assert.assertEquals(calSum, realSum);
//
//        for (PersonInterface person : persons) {
//            int personId = person.getId();
//            for (PersonInterface tempPerson : originalPersons) {
//                if (personId == tempPerson.getId()) {
//                    Assert.assertTrue(((Person) tempPerson).strictEquals(person));
//                }
//            }
//        }
//
//        for (PersonInterface person : originalPersons) {
//            int personId = person.getId();
//            for (PersonInterface tempPerson : persons) {
//                if (tempPerson.getId() == personId) {
//                    Assert.assertTrue(((Person) person).strictEquals(tempPerson));
//                }
//            }
//        }
//
//        Network network1 = new Network();
//        Network network2 = new Network();
//        Network network3 = new Network();
//
//        for (int i = 1; i < 7; i++) {
//            Person person1 = new Person(i, String.valueOf(i), i);
//            network1.addPerson(person1);
//            Person person2 = new Person(i, String.valueOf(i), i);
//            network2.addPerson(person2);
//            Person person3 = new Person(i, String.valueOf(i), i);
//            network3.addPerson(person3);
//        }
//
//        for (int i = 1; i < 6; i++) {
//            for (int j = i + 1; j < 7; j++) {
//                if (j == i + 1) {
//                    network2.addRelation(i, j, 100);
//                }
//                network3.addRelation(i, j, 100);
//            }
//        }
//        network2.addRelation(1, 6, 100);
//        Assert.assertEquals(0, network1.queryCoupleSum());
//        Assert.assertEquals(1, network2.queryCoupleSum());
//        Assert.assertEquals(1, network3.queryCoupleSum());
//        network2.addRelation(2, 4, 150);
//        Assert.assertEquals(1, network2.queryCoupleSum());
//        network2.modifyRelation(3, 4, 100);
//        Assert.assertEquals(1, network2.queryCoupleSum());
//        network2.addRelation(2, 5, 150);
//        Assert.assertEquals(1, network2.queryCoupleSum());
//        network2.modifyRelation(2, 3, 50);
//        network2.modifyRelation(3, 4, -200);
//        Assert.assertEquals(1, network2.queryCoupleSum());
//        Person person7 = new Person(7, String.valueOf(6), 6);
//        network3.addPerson(person7);
//        network3.addRelation(7, 6, 120);
//        Assert.assertEquals(2, network3.queryCoupleSum());
//        Person person8 = new Person(8, String.valueOf(6), 6);
//        network3.addPerson(person8);
//        network3.addRelation(8, 6, 120);
//        Assert.assertEquals(2, network3.queryCoupleSum());
//        network3.addRelation(2, 8, 120);
//        Assert.assertEquals(2, network3.queryCoupleSum());
    }

    @Test
    public void queryShortestPath() {
    }

    @Test
    public void deleteColdEmoji() {
        Random random = new Random();
        int limit = random.nextInt(3);
//        System.out.println("---------------");
//        System.out.println("messageList's size before deletion:" + network.getMessages().size());
//        System.out.println("emojiList's size before deletion:" + network.getEmojiIdList().size());
//        System.out.println("limit:" + limit);
        int result = network.deleteColdEmoji(limit);
//        System.out.println("result:" + result);
//        System.out.println("messageList's size after deletion:" + network.getMessages().size());
//        System.out.println("---------------");

        MessageInterface[] messages = network.getMessages();
        int[] emojiIdList = network.getEmojiIdList();
        int[] emojiHeatList = network.getEmojiHeatList();
        HashMap<Integer, Message> messagesMap = new HashMap<>();
        for (MessageInterface message : messages) {
            messagesMap.put(message.getId(), (Message) message);
        }
        HashSet<Integer> emojiIdSet = new HashSet<>();
        HashMap<Integer, Integer> emojiHeatMap = new HashMap<>();
        for (int i = 0; i < emojiIdList.length; i++) {
            emojiIdSet.add(emojiIdList[i]);
            emojiHeatMap.put(emojiIdList[i], emojiHeatList[i]);
        }

        Assert.assertEquals(result, emojiIdList.length);

        for (HashMap.Entry<Integer, Integer> entry : emojiHeatListCopy.entrySet()) {
            if (entry.getValue() >= limit) {
                Assert.assertTrue(emojiIdSet.contains(entry.getKey()));
            }
        }

        for (int id : emojiIdList) {
            Assert.assertTrue(emojiIdListCopy.containsKey(id)
                    && emojiHeatMap.get(id).equals(emojiHeatListCopy.get(id)));
        }

        int len = 0;
        for (HashMap.Entry<Integer, Integer> entry : emojiHeatListCopy.entrySet()) {
            if (entry.getValue() >= limit) {
                len++;
            }
        }
        Assert.assertEquals(len, emojiIdList.length);

        Assert.assertEquals(emojiIdList.length, emojiHeatList.length);

        for (HashMap.Entry<Integer, Message> entry : messagesCopy.entrySet()) {
            Message message = entry.getValue();
            if ((!(message instanceof EmojiMessage) || emojiIdSet.contains(message.getId()))) {
                Assert.assertTrue(messagesMap.containsKey(message.getId()));
                Message cmpMessage = messagesMap.get(message.getId());
                Assert.assertEquals(message, cmpMessage);
            }
        }

        int messagesLen = 0;
        for (HashMap.Entry<Integer, Message> entry : messagesCopy.entrySet()) {
            Message message = entry.getValue();
            if (!(message instanceof EmojiMessage) || emojiIdSet.contains(message.getId())) {
                messagesLen++;
            }
        }
        Assert.assertEquals(messagesLen, messages.length);
    }
}