import com.oocourse.spec2.main.OfficialAccountInterface;
import com.oocourse.spec2.main.PersonInterface;

import java.util.HashMap;
import java.util.HashSet;

public class OfficialAccount implements OfficialAccountInterface {
    private int ownerId;
    private int id;
    private String name;
    private HashMap<Integer, Person> followers;
    private HashSet<Integer> articles;
    private HashMap<Integer, Integer> contributions;

    public OfficialAccount(int ownerId, int id, String name) {
        this.ownerId = ownerId;
        this.id = id;
        this.name = name;
        followers = new HashMap<>();
        articles = new HashSet<>();
        contributions = new HashMap<>();
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void addFollower(PersonInterface person) {
        Person p = (Person) person;
        if (!containsFollower(p)) {
            followers.put(p.getId(), p);
            contributions.put(p.getId(), 0);
        }
    }

    public boolean containsFollower(PersonInterface person) {
        return followers.containsKey(person.getId());
    }

    public void addArticle(PersonInterface person, int id) {
        Person p = (Person) person;
        if (!containsArticle(id)) {
            articles.add(id);
            int personId = p.getId();
            int oldContribution = contributions.get(personId);
            contributions.replace(personId, oldContribution + 1);
        }
    }

    public void receiveArticle(int articleId) {
        for (Person person : followers.values()) {
            person.addArticle(articleId);
        }
    }

    public boolean containsArticle(int id) {
        return articles.contains(id);
    }

    public void removeArticle(int id) {
        if (containsArticle(id)) {
            articles.remove(id);
        }
    }

    public void deleteArticle(int articleId) {
        for (Person person : followers.values()) {
            person.delArticle(articleId);
        }
    }

    public void subContribution(int personId) {
        int oldContribution = contributions.get(personId);
        contributions.replace(personId, oldContribution - 1);
    }

    public int getBestContributor() {
        int bestId = Integer.MAX_VALUE;
        int maxContribution = 0;
        for (HashMap.Entry<Integer, Integer> contribution : contributions.entrySet()) {
            if (contribution.getValue() > maxContribution) {
                maxContribution = contribution.getValue();
                bestId = contribution.getKey();
            } else if (contribution.getValue() == maxContribution) {
                if (contribution.getKey() < bestId) {
                    bestId = contribution.getKey();
                }
            }
        }
        return bestId;
    }

    public boolean repOK() {
        if (followers == null) {
            return false;
        }
        for (HashMap.Entry<Integer, Person> entry1 : followers.entrySet()) {
            Person person1 = entry1.getValue();
            for (HashMap.Entry<Integer, Person> entry2 : followers.entrySet()) {
                Person person2 = entry2.getValue();
                if (!entry1.getKey().equals(entry2.getKey()) && person1.equals(person2)) {
                    return false;
                }
            }
        }
        return articles != null && contributions != null
                && followers.size() == contributions.size();
    }
}
