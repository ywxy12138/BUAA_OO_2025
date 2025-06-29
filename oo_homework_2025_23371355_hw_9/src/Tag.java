import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;
import java.util.HashMap;

public class Tag implements TagInterface {
    private int id;
    private HashMap<Integer, Person> persons;
    private int ageSum;
    private int agePowSum;

    public Tag(int id) {
        this.id = id;
        this.persons = new HashMap<>();
        this.ageSum = 0;
        this.agePowSum = 0;
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

    public void addPerson(PersonInterface person) {
        Person p = (Person) person;
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

    public int getAgeMean() {
        if (persons.isEmpty()) {
            return 0;
        }
        return ageSum / persons.size();
    }

    public int getAgeVar() {
        int mean = getAgeMean();
        if (persons.isEmpty()) {
            return 0;
        }
        return (agePowSum - 2 * ageSum * mean + persons.size() * mean * mean)
                / persons.size();
    }

    public void delPerson(PersonInterface person) {
        Person p = (Person) person;
        persons.remove(p.getId());
        ageSum -= person.getAge();
        agePowSum -= person.getAge() * person.getAge();
    }

    public int getSize() {
        return persons.size();
    }
}
