import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Random;

import static org.junit.Assert.*;

public class DisjointSetTest {

    @Test
    public void add() throws Exception {
        Random rand = new Random();
        Network network = new Network();
        for (int i = 1; i <= 14; i++) {
            Person person = new Person(i, String.valueOf(i), rand.nextInt(100) + 1);
            network.addPerson(person);
        }
        network.addRelation(1, 3, 100);
        network.addRelation(1, 4, 100);
        network.addRelation(2, 4, 100);
        network.addRelation(4, 5, 100);
        network.addRelation(4, 6, 100);
        network.addRelation(6, 7, 100);
        network.addRelation(6, 8, 100);
        network.addRelation(9, 10, 100);
        network.addRelation(9, 11, 100);
        network.addRelation(13, 14, 100);
        Assert.assertTrue(network.isCircle(1, 3));
        Assert.assertTrue(network.isCircle(1, 4));
        Assert.assertTrue(network.isCircle(5, 4));
        Assert.assertTrue(network.isCircle(14, 13));
        Assert.assertTrue(network.isCircle(11, 9));
        Assert.assertTrue(network.isCircle(3, 8));
        Assert.assertTrue(network.isCircle(7, 3));
        Assert.assertFalse(network.isCircle(7, 9));
        Assert.assertFalse(network.isCircle(9, 13));
        Assert.assertFalse(network.isCircle(13, 12));
        Assert.assertFalse(network.isCircle(8, 12));
        Assert.assertFalse(network.isCircle(3, 11));
        Assert.assertFalse(network.isCircle(13, 1));

        network.modifyRelation(4, 6, -900);
        Assert.assertFalse(network.isCircle(4, 6));
        Assert.assertFalse(network.isCircle(1, 6));
        Assert.assertFalse(network.isCircle(2, 6));
        Assert.assertFalse(network.isCircle(3, 6));
        Assert.assertFalse(network.isCircle(5, 6));
        Assert.assertTrue(network.isCircle(7, 6));
        Assert.assertTrue(network.isCircle(8, 6));
        Assert.assertFalse(network.isCircle(9, 6));
        Assert.assertFalse(network.isCircle(13, 6));

        network.modifyRelation(7, 6, 150);
        Assert.assertTrue(network.isCircle(7, 6));

        network.modifyRelation(7, 6, -250);
        Assert.assertFalse(network.isCircle(7, 6));
        Assert.assertFalse(network.isCircle(7, 8));
        Assert.assertTrue(network.isCircle(8, 6));
    }
}