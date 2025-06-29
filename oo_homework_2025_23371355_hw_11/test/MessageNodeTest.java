import com.oocourse.spec3.main.MessageInterface;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

public class MessageNodeTest {
    @Test
    public void testMessageNode() {
        Person person1 = new Person(0, "3", 2);
        Person person2 = new Person(1, "2", 3);
        Message message = new Message(0, 0, person1, person2);
        MessageNode messageNode = new MessageNode(message);
        Assert.assertEquals(message, messageNode.getMessage());
    }
}