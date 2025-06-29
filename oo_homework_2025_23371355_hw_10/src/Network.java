import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.exceptions.RelationNotFoundException;
import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec2.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec2.exceptions.ContributePermissionDeniedException;
import com.oocourse.spec2.exceptions.DeleteArticlePermissionDeniedException;
import com.oocourse.spec2.exceptions.DeleteOfficialAccountPermissionDeniedException;
import com.oocourse.spec2.exceptions.EqualOfficialAccountIdException;
import com.oocourse.spec2.exceptions.EqualArticleIdException;
import com.oocourse.spec2.exceptions.PathNotFoundException;
import com.oocourse.spec2.exceptions.TagIdNotFoundException;
import com.oocourse.spec2.exceptions.OfficialAccountIdNotFoundException;
import com.oocourse.spec2.exceptions.EqualTagIdException;
import com.oocourse.spec2.main.NetworkInterface;
import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.ArrayList;

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

    public Network() {
        persons = new HashMap<>();
        disjointSet = new DisjointSet();
        tripleSum = 0;
        accounts = new HashMap<>();
        articles = new HashSet<>();
        articleContributors = new HashMap<>();
    }

    public boolean containsPerson(int id) {
        return persons.containsKey(id);
    }

    //有的话返回对应的人，否则返回null
    public Person getPerson(int id) {
        if (persons.containsKey(id)) {
            return persons.get(id);
        }
        return null;
    }

    public PersonInterface[] getPersons() {
        return null;
    }

    public void addPerson(PersonInterface person) throws EqualPersonIdException {
        Person p = (Person) person;
        int id = p.getId();
        if (!containsPerson(id)) {
            persons.put(id, p);
            //有人加入了关系网，避免一开启创建时无并查集可用，又调用isCircle函数可能导致的错误
            //只需要给并查集加入人即可，不用重新构建
            //但仅是加了一个人并不会影响tripleSum的计算，单人不成环
            disjointSet.add(p);
        }
        else {
            throw new EqualPersonIdException(id);
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
            //有人建立了关系，只需要合并相应的集合即可，不需要对并查集进行重建
            disjointSet.union(id1, id2);
            //添加关系，只需要去判断建立关系的两个节点的熟人集的交集，
            //只要有一个就会因为这两个点相连而形成一个新的三元环
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
                //加深关系并不去修改并查集，并查集并未发生改变
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
                //有人关系破裂，关系破裂需要重新构建并查集对应集合，其余不相干集合不需要进行重建
                //既然id1与id2有联系，说明它俩在一个集合中，因此根节点一致，只需传入一个即可
                //将该集合中节点还原成初始刚加入的状态
                HashSet<Integer> deleted = disjointSet.delete(id1);
                //重新加入边,重建刚才复原的集合，有可能会分为两个集合
                for (Integer key : deleted) {
                    Person person = persons.get(key);
                    HashMap<Integer, Person> friends = person.getAcquaintance();
                    for (Integer friendId : friends.keySet()) {
                        disjointSet.union(key, friendId);
                    }
                }
                //删除边只需要考虑该边的两个节点的交集个数
                //有一个就会因为这个而减少一个三元环
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
            //并查集在加入人、加边、减边的时候已经自行完成维护，因此每次调用isCircle函数，其都是有效的
            return disjointSet.find(id1) == disjointSet.find(id2);
        }
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        else {
            throw new PersonIdNotFoundException(id2);
        }
    }

    //求出id1和id2两个人的熟人集的交集个数，用来维护三元环
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
        //tripleSum在加减边时维护好了，无需重新计算
        return tripleSum;
    }

    public void addTag(int personId, TagInterface tag) throws
        PersonIdNotFoundException, EqualTagIdException {
        Tag t = (Tag) tag;
        Person person = getPerson(personId);
        if (containsPerson(personId) && !person.containsTag(t.getId())) {
            person.addTag(t);
        }
        else {
            if (!containsPerson(personId)) {
                throw new PersonIdNotFoundException(personId);
            }
            else {
                throw new EqualTagIdException(t.getId());
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
        Person person = getPerson(personId);
        if (containsPerson(personId) && person.containsTag(tagId)) {
            return person.getTag(tagId).getAgeVar();
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
        Person person = getPerson(personId);
        if (containsPerson(personId) && person.containsTag(tagId)) {
            person.delTag(tagId);
            persons.replace(personId, person);
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
        else {
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
        else {
            accounts.remove(accountId);
        }
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
        else {
            articles.add(articleId);
            accounts.get(accountId).addArticle(getPerson(personId), articleId);
            articleContributors.put(articleId, personId);
            accounts.get(accountId).receiveArticle(articleId);
        }
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
        else {
            OfficialAccount officialAccount = accounts.get(accountId);
            //从公众号中删除该篇文章
            officialAccount.removeArticle(articleId);
            //删除该公众号中所有有该篇文章的人的receivedArticles中的对应文章
            officialAccount.deleteArticle(articleId);
            //拿写这篇文章的人的id去删去其贡献值
            officialAccount.subContribution(articleContributors.get(articleId));
        }
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
        else {
            OfficialAccount officialAccount = accounts.get(accountId);
            officialAccount.addFollower(getPerson(personId));
        }
    }

    public int queryBestContributor(int id) throws OfficialAccountIdNotFoundException {
        if (containsAccount(id)) {
            return accounts.get(id).getBestContributor();
        }
        else {
            throw new OfficialAccountIdNotFoundException(id);
        }
    }

    public ArrayList<Integer> queryReceivedArticles(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        else {
            return getPerson(id).queryReceivedArticles();
        }
    }
}
