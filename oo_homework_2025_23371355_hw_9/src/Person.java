import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

import java.util.HashMap;

public class Person implements PersonInterface {
    private int id;
    private String name;
    private int age;
    private HashMap<Integer, Person> acquaintance;
    private Heap heap;
    private HashMap<Integer, Tag> tags;

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        acquaintance = new HashMap<>();
        heap = new Heap();
        tags = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public HashMap<Integer, Person> getAcquaintance() {
        return acquaintance;
    }

    public int getAcquaintanceSize() {
        return acquaintance.size();
    }

    public int getBestId() {
        return heap.getBestId();
    }

    private boolean containsAcquaintance(int id) {
        return acquaintance.containsKey(id);
    }

    private boolean containsValue(int id) {
        return heap.containsId(id);
    }

    public boolean containsTag(int id) {
        return tags.containsKey(id);
    }

    public Tag getTag(int id) {
        if (containsTag(id)) {
            return tags.get(id);
        }
        return null;
    }

    private void addAcquaintance(Person person) {
        if (!containsAcquaintance(person.getId())) {
            acquaintance.put(person.getId(), person);
        }
    }

    private void addValue(int id, int value) {
        if (!containsValue(id)) {
            heap.addValue(id, value);
        }
    }

    public void addTag(TagInterface tag) {
        Tag t = (Tag) tag;
        if (!containsTag(t.getId())) {
            tags.put(t.getId(), t);
        }
    }

    public void delTag(int id) {
        if (containsTag(id)) {
            tags.remove(id);
        }
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof PersonInterface) {
            Person person = (Person) obj;
            return person.getId() == id;
        }
        return false;
    }

    public void link(Person person, int value) {
        addAcquaintance(person);
        addValue(person.getId(), value);
    }

    public boolean isLinked(PersonInterface person) {
        Person p = (Person) person;
        int id = p.getId();
        return containsAcquaintance(id) || id == this.id;
    }

    public void deeperRelation(Person person, int value) {
        int id = person.getId();
        if (containsAcquaintance(id)) {
            heap.enhanceValue(id, value);
        }
    }

    public void cancelRelation(Person person) {
        int id = person.getId();
        if (containsAcquaintance(id)) {
            acquaintance.remove(id);
            heap.deleteValue(id);
            for (HashMap.Entry<Integer, Tag> entry : tags.entrySet()) {
                Tag tag = entry.getValue();
                if (tag.hasPerson(person)) {
                    tag.delPerson(person);
                    tags.replace(entry.getKey(), tag);
                }
            }
        }
    }

    public int queryValue(PersonInterface person) {
        Person p = (Person) person;
        int id = p.getId();
        if (containsAcquaintance(id)) {
            return heap.getValue(id);
        }
        return 0;
    }
}
