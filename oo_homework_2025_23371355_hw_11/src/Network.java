import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec3.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec3.exceptions.ContributePermissionDeniedException;
import com.oocourse.spec3.exceptions.DeleteArticlePermissionDeniedException;
import com.oocourse.spec3.exceptions.DeleteOfficialAccountPermissionDeniedException;
import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualArticleIdException;
import com.oocourse.spec3.exceptions.EqualOfficialAccountIdException;
import com.oocourse.spec3.exceptions.EqualEmojiIdException;
import com.oocourse.spec3.exceptions.EqualMessageIdException;
import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.EqualTagIdException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import com.oocourse.spec3.exceptions.OfficialAccountIdNotFoundException;
import com.oocourse.spec3.exceptions.PathNotFoundException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.EmojiMessageInterface;
import com.oocourse.spec3.main.TagInterface;
import com.oocourse.spec3.main.ForwardMessageInterface;
import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.NetworkInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Iterator;
import java.util.LinkedList;

public class Network implements NetworkInterface {
    //以人的id为键，人为值
    private HashMap<Integer, Person> persons;
    //维护一个并查集，只有在有人加入或者，有人关系破裂的时候或有人建立了关系时会更新
    private DisjointSet disjointSet;
    //维护三元环的个数，只有在加边、删边的情况下会去更新
    private int tripleSum;
    //以account's id为键，对应的对象为键
    private HashMap<Integer, OfficialAccount> accounts;
    //存储的是每篇文章的独一无二的id
    private HashSet<Integer> articles;
    //以文章id为键，相应贡献文章的人的id为值构建的映射关系
    private HashMap<Integer, Integer> articleContributors;
    //以message's id为键，对应的message对象为值建立hashmap
    private HashMap<Integer, Message> messages;
    //以emojiId为键，相应的message's id组成的hashset(因为对应的message不止一个)为值
    //(为了避免在未将emoji转换成message时设定特定的值引发的dce出错)建立hashmap
    private HashMap<Integer, HashSet<Integer>> emojiIdList;
    //以emojiId为键，相应的热度值为值建立hashmap
    private HashMap<Integer, Integer> emojiHeatList;

    public Network() {
        persons = new HashMap<>();
        disjointSet = new DisjointSet();
        tripleSum = 0;
        accounts = new HashMap<>();
        articles = new HashSet<>();
        articleContributors = new HashMap<>();
        messages = new HashMap<>();
        emojiIdList = new HashMap<>();
        emojiHeatList = new HashMap<>();
    }

    public boolean containsPerson(int id) { return persons.containsKey(id); }

    public Person getPerson(int id) { return persons.get(id); }

    public MessageInterface[] getMessages() { return null; }

    public int[] getEmojiIdList() { return null; }

    public int[] getEmojiHeatList() { return null; }

    public void addPerson(PersonInterface person) throws EqualPersonIdException {
        if (!containsPerson(person.getId())) {
            persons.put(person.getId(), (Person) person);
            disjointSet.add((Person) person);
        }
        else {
            throw new EqualPersonIdException(person.getId());
        }
    }

    public void addRelation(int id1, int id2, int value) throws
        PersonIdNotFoundException, EqualRelationException {
        if (containsPerson(id1) && containsPerson(id2)
            && !getPerson(id1).isLinked(getPerson(id2))) {
            Person person1 = getPerson(id1);
            Person person2 = getPerson(id2);
            person1.link(person2, value);
            person2.link(person1, value);
            persons.replace(id1, person1);
            persons.replace(id2, person2);
            for (HashMap.Entry<Integer, Person> entry : persons.entrySet()) {
                Person person = entry.getValue();
                for (HashMap.Entry<Integer, Tag> tag : person.getTags().entrySet()) {
                    Tag tempTag = tag.getValue();
                    if (tempTag.hasPerson(person1) && tempTag.hasPerson(person2)) {
                        tempTag.addValueSum(value);
                    }
                }
            }
            disjointSet.union(id1, id2);
            tripleSum += mapInterNum(id1, id2);
        }
        else {
            if (!containsPerson(id1)) {
                throw new PersonIdNotFoundException(id1);
            }
            else if (!containsPerson(id2)) {
                throw new PersonIdNotFoundException(id2);
            }
            else {
                throw new EqualRelationException(id1, id2);
            }
        }
    }

    public void modifyRelation(int id1, int id2, int value) throws PersonIdNotFoundException,
        EqualPersonIdException, RelationNotFoundException {
        Person person1 = getPerson(id1);
        Person person2 = getPerson(id2);
        if (containsPerson(id1) && containsPerson(id2) &&
            id1 != id2 && person1.isLinked(person2)) {
            int modifiedValue = (person1.queryValue(person2) + value > 0) ?
                value : -person1.queryValue(person2);
            for (HashMap.Entry<Integer, Person> entry : persons.entrySet()) {
                Person person = entry.getValue();
                for (HashMap.Entry<Integer, Tag> tag : person.getTags().entrySet()) {
                    Tag tempTag = tag.getValue();
                    if (tempTag.hasPerson(person1) && tempTag.hasPerson(person2)) {
                        tempTag.addValueSum(modifiedValue);
                    }
                }
            }
            if (person1.queryValue(person2) + value > 0) {
                person1.deeperRelation(person2, value);
                person2.deeperRelation(person1, value);
                persons.replace(id1, person1);
                persons.replace(id2, person2);
            }
            else {
                person1.cancelRelation(person2);
                person2.cancelRelation(person1);
                persons.replace(id1, person1);
                persons.replace(id2, person2);
                HashSet<Integer> deleted = disjointSet.delete(id1);
                for (Integer key : deleted) {
                    Person person = persons.get(key);
                    HashMap<Integer, Person> friends = person.getAcquaintance();
                    for (Integer friendId : friends.keySet()) {
                        disjointSet.union(key, friendId);
                    }
                }
                tripleSum -= mapInterNum(id1, id2);
            }
        }
        else {
            if (!containsPerson(id1)) {
                throw new PersonIdNotFoundException(id1);
            } else if (!containsPerson(id2)) {
                throw new PersonIdNotFoundException(id2);
            } else if (id1 == id2) {
                throw new EqualPersonIdException(id1);
            }
            else {
                throw new RelationNotFoundException(id1, id2);
            }
        }
    }

    public int queryValue(int id1, int id2) throws
        PersonIdNotFoundException, RelationNotFoundException {
        Person person1 = getPerson(id1);
        Person person2 = getPerson(id2);
        if (containsPerson(id1) && containsPerson(id2) && person1.isLinked(person2)) {
            return person1.queryValue(person2);
        }
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        else if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        else {
            throw new RelationNotFoundException(id1, id2);
        }
    }

    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (containsPerson(id1) && containsPerson(id2)) {
            return disjointSet.find(id1) == disjointSet.find(id2);
        }
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        else {
            throw new PersonIdNotFoundException(id2);
        }
    }

    private int mapInterNum(int id1, int id2) {
        int ans = 0;
        Person person1 = getPerson(id1);
        Person person2 = getPerson(id2);
        HashMap<Integer, Person> friends1 = person1.getAcquaintance();
        HashMap<Integer, Person> friends2 = person2.getAcquaintance();
        int size1 = person1.getAcquaintanceSize();
        int size2 = person2.getAcquaintanceSize();
        HashMap<Integer, Person> small = (size1 < size2) ? friends1 : friends2;
        HashMap<Integer, Person> big = (size1 < size2) ? friends2 : friends1;
        for (Integer key : small.keySet()) {
            if (big.containsKey(key)) {
                ans++;
            }
        }
        return ans;
    }

    public int queryTripleSum() {
        return tripleSum;
    }

    public void addTag(int personId, TagInterface tag) throws
        PersonIdNotFoundException, EqualTagIdException {
        if (containsPerson(personId) && !getPerson(personId).containsTag(tag.getId())) {
            getPerson(personId).addTag(tag);
        }
        else {
            if (!containsPerson(personId)) {
                throw new PersonIdNotFoundException(personId);
            }
            else {
                throw new EqualTagIdException(tag.getId());
            }
        }
    }

    public void addPersonToTag(int personId1, int personId2, int tagId) throws
        PersonIdNotFoundException, RelationNotFoundException,
        TagIdNotFoundException, EqualPersonIdException {
        Person person1 = getPerson(personId1);
        Person person2 = getPerson(personId2);
        if (containsPerson(personId1) && containsPerson(personId2) && personId1 != personId2
            && person2.isLinked(person1) && person2.containsTag(tagId)
            && !person2.getTag(tagId).hasPerson(person1)
            && person2.getTag(tagId).getSize() <= 999) {
            person2.getTag(tagId).addPerson(person1);
            persons.replace(personId2, person2);
        }
        else {
            if (!containsPerson(personId1)) {
                throw new PersonIdNotFoundException(personId1);
            }
            else if (!containsPerson(personId2)) {
                throw new PersonIdNotFoundException(personId2);
            }
            else if (personId1 == personId2) {
                throw new EqualPersonIdException(personId1);
            }
            else if (!person2.isLinked(person1)) {
                throw new RelationNotFoundException(personId1, personId2);
            }
            else if (!person2.containsTag(tagId)) {
                throw new TagIdNotFoundException(tagId);
            }
            else if (person2.getTag(tagId).hasPerson(person1)) {
                throw new EqualPersonIdException(personId1);
            }
        }
    }

    public int queryTagValueSum(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId) && getPerson(personId).containsTag(tagId)) {
            return getPerson(personId).getTag(tagId).getValueSum();
        }
        else if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        else {
            throw new TagIdNotFoundException(tagId);
        }
    }

    public int queryTagAgeVar(int personId, int tagId) throws
        PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId) && getPerson(personId).containsTag(tagId)) {
            return getPerson(personId).getTag(tagId).getAgeVar();
        }
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        else {
            throw new TagIdNotFoundException(tagId);
        }
    }

    public void delPersonFromTag(int personId1, int personId2, int tagId) throws
        PersonIdNotFoundException, TagIdNotFoundException {
        Person person1 = getPerson(personId1);
        Person person2 = getPerson(personId2);
        if (containsPerson(personId1) && containsPerson(personId2)
            && person2.containsTag(tagId) && person2.getTag(tagId).hasPerson(person1)) {
            person2.getTag(tagId).delPerson(person1);
            persons.replace(personId2, person2);
        }
        else {
            if (!containsPerson(personId1)) {
                throw new PersonIdNotFoundException(personId1);
            }
            else if (!containsPerson(personId2)) {
                throw new PersonIdNotFoundException(personId2);
            }
            else if (!getPerson(personId2).containsTag(tagId)) {
                throw new TagIdNotFoundException(tagId);
            }
            else {
                throw new PersonIdNotFoundException(personId1);
            }
        }
    }

    public void delTag(int personId, int tagId) throws
        PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId) && getPerson(personId).containsTag(tagId)) {
            getPerson(personId).delTag(tagId);
            persons.replace(personId, getPerson(personId));
        }
        else {
            if (!containsPerson(personId)) {
                throw new PersonIdNotFoundException(personId);
            }
            else {
                throw new TagIdNotFoundException(tagId);
            }
        }
    }

    public boolean containsMessage(int id) {
        return messages.containsKey(id);
    }

    public void addMessage(MessageInterface message) throws
        EqualMessageIdException, EmojiIdNotFoundException,
        EqualPersonIdException, ArticleIdNotFoundException {
        if (containsMessage(message.getId())) {
            throw new EqualMessageIdException(message.getId());
        }
        else if (message instanceof EmojiMessageInterface
            && !containsEmojiId(((EmojiMessageInterface) message).getEmojiId())) {
            throw new EmojiIdNotFoundException(((EmojiMessageInterface) message).getEmojiId());
        }
        else if (message instanceof ForwardMessageInterface
            && !containsArticle(((ForwardMessageInterface) message).getArticleId())) {
            throw new ArticleIdNotFoundException(((ForwardMessage) message).getArticleId());
        }
        else if ((message instanceof ForwardMessageInterface) &&
            containsArticle(((ForwardMessageInterface) message).getArticleId()) &&
            !(message.getPerson1().getReceivedArticles().contains(
            ((ForwardMessageInterface) message).getArticleId()))) {
            throw new ArticleIdNotFoundException(((ForwardMessage) message).getArticleId());
        }
        else if ((!(message instanceof EmojiMessageInterface) ||
            containsEmojiId(((EmojiMessageInterface) message).getEmojiId())) &&
            (!(message instanceof ForwardMessageInterface) ||
            (containsArticle(((ForwardMessageInterface) message).getArticleId()) &&
            (message.getPerson1().getReceivedArticles().contains(
            ((ForwardMessageInterface) message).getArticleId())))) &&
            message.getType() == 0 && message.getPerson1().equals(message.getPerson2())) {
            throw new EqualPersonIdException(message.getPerson1().getId());
        }
        Message realMessage = (Message) message;
        if (realMessage instanceof EmojiMessage) {
            EmojiMessage emojiMessage = (EmojiMessage) realMessage;
            HashSet<Integer> set = emojiIdList.get(emojiMessage.getEmojiId());
            set.add(emojiMessage.getId());
            emojiIdList.put(emojiMessage.getEmojiId(), set);
        }
        messages.put(message.getId(), realMessage);
    }

    public MessageInterface getMessage(int id) { return messages.get(id); }

    public void sendMessage(int id) throws
        RelationNotFoundException, MessageIdNotFoundException, TagIdNotFoundException {
        if (!containsMessage(id)) {
            throw new MessageIdNotFoundException(id);
        }
        else if (getMessage(id).getType() == 0) {
            Message message = (Message) getMessage(id);
            Person person1 = (Person) message.getPerson1();
            Person person2 = (Person) message.getPerson2();
            if (!person1.isLinked(person2)) {
                throw new RelationNotFoundException(person1.getId(), person2.getId());
            }
            else if (person1 != person2) {
                int socialValue = message.getSocialValue();
                person1.addSocialValue(socialValue);
                person2.addSocialValue(socialValue);
                if (message instanceof RedEnvelopeMessage) {
                    int money = ((RedEnvelopeMessage) message).getMoney();
                    person1.addMoney(-money);
                    person2.addMoney(money);
                }
                else if (message instanceof ForwardMessage) {
                    person2.addArticle(((ForwardMessage) message).getArticleId());
                }
                else if (message instanceof EmojiMessage) {
                    int emojiId = ((EmojiMessage) message).getEmojiId();
                    emojiHeatList.replace(emojiId, emojiHeatList.get(emojiId) + 1);
                }
                person2.addMessage(message);
                messages.remove(id);
            }
        }
        else if (getMessage(id).getType() == 1) {
            Message message = (Message) getMessage(id);
            Person person1 = (Person) message.getPerson1();
            Tag tag = (Tag) message.getTag();
            if (!person1.containsTag(tag.getId())) {
                throw new TagIdNotFoundException(tag.getId());
            }
            else {
                int socialValue = message.getSocialValue();
                person1.addSocialValue(socialValue);
                tag.addSocialValue(socialValue);
                if (message instanceof RedEnvelopeMessage && tag.getSize() > 0) {
                    int averageMoney = ((RedEnvelopeMessage) message).getMoney() / tag.getSize();
                    person1.addMoney(-averageMoney * tag.getSize());
                    tag.addMoney(averageMoney);
                }
                else if (message instanceof ForwardMessage && tag.getSize() > 0) {
                    tag.addArticle(((ForwardMessage) message).getArticleId());
                }
                else if (message instanceof EmojiMessage) {
                    int emojiId = ((EmojiMessage) message).getEmojiId();
                    emojiHeatList.replace(emojiId, emojiHeatList.get(emojiId) + 1);
                }
                tag.addMessage(message);
                messages.remove(id);
            }
        }
    }

    public int querySocialValue(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        return getPerson(id).getSocialValue();
    }

    public List<MessageInterface> queryReceivedMessages(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        return getPerson(id).getReceivedMessages();
    }

    public boolean containsEmojiId(int id) {
        return emojiIdList.containsKey(id);
    }

    public void storeEmojiId(int id) throws EqualEmojiIdException {
        if (containsEmojiId(id)) {
            throw new EqualEmojiIdException(id);
        }
        emojiIdList.put(id, new HashSet<>());
        emojiHeatList.put(id, 0);
    }

    public int queryMoney(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        return getPerson(id).getMoney();
    }

    public int queryPopularity(int id) throws EmojiIdNotFoundException {
        if (!containsEmojiId(id)) {
            throw new EmojiIdNotFoundException(id);
        }
        return emojiHeatList.get(id);
    }

    public int deleteColdEmoji(int limit) {
        Iterator<Integer> iterator = emojiHeatList.keySet().iterator();
        while (iterator.hasNext()) {
            int emojiId = iterator.next();
            int heatValue = emojiHeatList.get(emojiId);
            if (heatValue < limit) {
                HashSet<Integer> set = emojiIdList.get(emojiId);
                if (!set.isEmpty()) {
                    for (Integer messageId : set) {
                        messages.remove(messageId);
                    }
                }
                emojiIdList.remove(emojiId);
                iterator.remove();
            }
        }
        return emojiIdList.size();
    }

    public int queryBestAcquaintance(int id) throws
        PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (containsPerson(id) && getPerson(id).getAcquaintanceSize() != 0) {
            return getPerson(id).getBestId();
        }
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        else {
            throw new AcquaintanceNotFoundException(id);
        }
    }

    public int queryCoupleSum() {
        int result = 0;
        for (HashMap.Entry<Integer, Person> entry : persons.entrySet()) {
            Person person1 = entry.getValue();
            if (person1.getAcquaintanceSize() > 0) {
                int person1sBestFriendId = person1.getBestId();
                Person person2 = getPerson(person1sBestFriendId);
                if (person2.getAcquaintanceSize() > 0) {
                    if (person2.getBestId() == person1.getId()
                        && person1.getId() < person2.getId()) {
                        result++;
                    }
                }
            }
        }
        return result;
    }

    public int queryShortestPath(int id1, int id2)
        throws PersonIdNotFoundException, PathNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        else if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        else if (!isCircle(id1, id2)) {
            throw new PathNotFoundException(id1, id2);
        }
        if (id1 == id2) {
            return 0;
        }
        else {
            Queue<Integer> queue = new LinkedList<Integer>();
            HashSet<Integer> visited = new HashSet<>();
            HashMap<Integer, Integer> distance = new HashMap<>();
            queue.add(id1);
            visited.add(id1);
            distance.put(id1, 0);
            int ans = 0;
            while (!queue.isEmpty()) {
                int current = queue.poll();
                Person person = getPerson(current);
                HashMap<Integer, Person> friends = person.getAcquaintance();
                for (HashMap.Entry<Integer, Person> entry : friends.entrySet()) {
                    if (entry.getKey() == id2) {
                        ans = distance.get(current) + 1;
                        break;
                    }
                    if (!visited.contains(entry.getKey())) {
                        queue.add(entry.getKey());
                        visited.add(entry.getKey());
                        distance.put(entry.getKey(), distance.get(current) + 1);
                    }
                }
                if (ans != 0) {
                    break;
                }
            }
            return ans;
        }
    }

    public boolean containsAccount(int id) {
        return accounts.containsKey(id);
    }

    public void createOfficialAccount(int personId, int accountId, String name) throws
        PersonIdNotFoundException, EqualOfficialAccountIdException {
        if (containsPerson(personId) && !containsAccount(accountId)) {
            OfficialAccount newOfficialAccount = new OfficialAccount(personId, accountId, name);
            newOfficialAccount.addFollower(getPerson(personId));
            accounts.put(accountId, newOfficialAccount);
        }
        else if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        else {
            throw new EqualOfficialAccountIdException(accountId);
        }
    }

    public void deleteOfficialAccount(int personId, int accountId) throws
        PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        DeleteOfficialAccountPermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        else if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        else if (accounts.get(accountId).getOwnerId() != personId) {
            throw new DeleteOfficialAccountPermissionDeniedException(personId, accountId);
        }
        accounts.remove(accountId);
    }

    public boolean containsArticle(int id) {
        return articles.contains(id);
    }

    public void contributeArticle(int personId, int accountId, int articleId) throws
        PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        EqualArticleIdException, ContributePermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        else if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        else if (containsArticle(articleId)) {
            throw new EqualArticleIdException(articleId);
        }
        else if (!accounts.get(accountId).containsFollower(getPerson(personId))) {
            throw new ContributePermissionDeniedException(personId, articleId);
        }
        articles.add(articleId);
        accounts.get(accountId).addArticle(getPerson(personId), articleId);
        articleContributors.put(articleId, personId);
        accounts.get(accountId).receiveArticle(articleId);
    }

    public void deleteArticle(int personId, int accountId, int articleId) throws
        PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        ArticleIdNotFoundException, DeleteArticlePermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        else if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        else if (!accounts.get(accountId).containsArticle(articleId)) {
            throw new ArticleIdNotFoundException(articleId);
        }
        else if (accounts.get(accountId).getOwnerId() != personId) {
            throw new DeleteArticlePermissionDeniedException(personId, articleId);
        }
        OfficialAccount officialAccount = accounts.get(accountId);
        officialAccount.removeArticle(articleId);
        officialAccount.deleteArticle(articleId);
        officialAccount.subContribution(articleContributors.get(articleId));
    }

    public void followOfficialAccount(int personId, int accountId)
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        EqualPersonIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        else if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        else if (accounts.get(accountId).containsFollower(getPerson(personId))) {
            throw new EqualPersonIdException(personId);
        }
        OfficialAccount officialAccount = accounts.get(accountId);
        officialAccount.addFollower(getPerson(personId));
    }

    public int queryBestContributor(int id) throws OfficialAccountIdNotFoundException {
        if (containsAccount(id)) {
            return accounts.get(id).getBestContributor();
        }
        throw new OfficialAccountIdNotFoundException(id);
    }

    public List<Integer> queryReceivedArticles(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        return getPerson(id).queryReceivedArticles();
    }
}
