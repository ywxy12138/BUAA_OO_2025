import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

public class Message implements MessageInterface {
    private int id;
    private int socialValue;
    private int type;
    private Person person1;
    private Person person2;
    private Tag tag;

    public Message(int messageId, int messageSocialValue,
        PersonInterface messagePerson1, PersonInterface messagePerson2) {
        this.type = 0;
        this.tag = null;
        this.id = messageId;
        this.socialValue = messageSocialValue;
        this.person1 = (Person) messagePerson1;
        this.person2 = (Person) messagePerson2;
    }

    public Message(int messageId, int messageSocialValue,
        PersonInterface messagePerson1, TagInterface messageTag) {
        this.type = 1;
        this.person2 = null;
        this.id = messageId;
        this.socialValue = messageSocialValue;
        this.person1 = (Person) messagePerson1;
        this.tag = (Tag) messageTag;
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public int getSocialValue() {
        return socialValue;
    }

    public PersonInterface getPerson1() {
        return person1;
    }

    public PersonInterface getPerson2() {
        return person2;
    }

    public TagInterface getTag() {
        return tag;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof MessageInterface)) {
            return false;
        }
        return ((MessageInterface) obj).getId() == this.getId();
    }

    public boolean repOk() {
        return (person1 != null && !person1.equals(person2));
    }
}
