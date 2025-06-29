import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class HeapTest {
    private Heap heap;
    private int id;
    private int value;
    private int heapSize;

    public HeapTest(Heap heap, int id, int value, int heapSize) {
        this.heap = heap;
        this.id = id;
        this.value = value;
        this.heapSize = heapSize;
    }

    @Parameterized.Parameters
    public static Collection data() {
        Random random = new Random();
        int testNum = 1000;

        Object[][] data = new Object[testNum][];
        for (int i = 0; i < testNum; i++) {
            Heap heap = new Heap();
            int nums = random.nextInt(100) + 1;
            int id = random.nextInt(nums);
            int value = 0;
            for (int j = 0; j < nums; j++) {
                int temp = random.nextInt(100);
                heap.addValue(j, temp);
                if (j == id) {
                    value = temp;
                }
            }
            data[i] = new Object[]{heap, id, value, nums};
        }
        return Arrays.asList(data);
    }

//    @Test
//    public void getValue() {
//        Assert.assertEquals(heap.getValue(id), value);
//    }

    @Test
    public void containsId() {
        Assert.assertFalse(heap.containsId(id));
    }

//    @Test
//    public void addValue() {
//        heap.addValue(heapSize, value + 100);
//        Assert.assertTrue(heap.containsId(heapSize));
//        for (int i = 0; i < heapSize; i++) {
//            Assert.assertTrue(heap.containsId(i));
//        }
//    }

//    @Test
//    public void enhanceValue() {
//        heap.enhanceValue(heapSize, heapSize);
//        Assert.assertTrue(heap.containsId(heapSize));
//        Assert.assertEquals(heap.getValue(heapSize), value + heapSize + 100);
//    }
//
    @Test
    public void deleteValue() {
        heap.deleteValue(id);
        Assert.assertFalse(heap.containsId(id));
        for (int i = 0; i < heapSize; i++) {
            if (i != id) {
                Assert.assertTrue(heap.containsId(i));
            }
        }
    }
}