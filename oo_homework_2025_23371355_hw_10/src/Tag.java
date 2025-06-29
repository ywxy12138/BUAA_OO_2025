import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;
import java.util.HashMap;

public class Tag implements TagInterface {
    private int id;
    private HashMap<Integer, Person> persons;
    private long ageSum;
    private long agePowSum;
    private int valueSum;

    public Tag(int id) {
        this.id = id;
        this.persons = new HashMap<>();
        this.ageSum = 0;
        this.agePowSum = 0;
        this.valueSum = 0;
    }

    public int getId() {
        return id;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof TagInterface) {
            Tag tag = (Tag) obj;
            return id == tag.getId();
        }
        return false;
    }

    public void addValueSum(int value) {
        valueSum += 2 * value;
    }

    public void addPerson(PersonInterface person) {
        Person p = (Person) person;
        HashMap<Integer, Person> friends = p.getAcquaintance();
        HashMap<Integer, Person> map = (friends.size() < persons.size()) ? friends : persons;
        for (Person tempPerson : map.values()) {
            int id = tempPerson.getId();
            if (persons.containsKey(id) && friends.containsKey(id)) {
                valueSum += 2 * tempPerson.queryValue(p);
            }
        }
        persons.put(p.getId(), p);
        ageSum += p.getAge();
        agePowSum += p.getAge() * p.getAge();
    }

    public boolean hasPerson(PersonInterface person) {
        if (person == null) {
            return false;
        }
        Person p = (Person) person;
        return persons.containsKey(p.getId()) && p.equals(persons.get(p.getId()));
    }

    public int getValueSum() {
        return valueSum;
    }

    public int getAgeMean() {
        if (persons.isEmpty()) {
            return 0;
        }
        return (int) (ageSum / persons.size());
    }

    public int getAgeVar() {
        int mean = getAgeMean();
        if (persons.isEmpty()) {
            return 0;
        }
        return (int) ((agePowSum - 2 * ageSum * mean + persons.size() * mean * mean)
                / persons.size());
    }

    public void delPerson(PersonInterface person) {
        Person p = (Person) person;
        persons.remove(p.getId());
        ageSum -= person.getAge();
        agePowSum -= person.getAge() * person.getAge();
        HashMap<Integer, Person> friends = p.getAcquaintance();
        HashMap<Integer, Person> map = (friends.size() < persons.size()) ? friends : persons;
        for (Person tempPerson : map.values()) {
            if (persons.containsKey(tempPerson.getId())
                && friends.containsKey(tempPerson.getId())) {
                valueSum -= 2 * tempPerson.queryValue(p);
            }
        }
    }

    public int getSize() {
        return persons.size();
    }
}
