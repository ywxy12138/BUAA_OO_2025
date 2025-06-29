import java.util.ArrayList;
import java.util.HashMap;

public class Heap {
    //存储友情值的大根堆数组形式
    private ArrayList<Value> heap;
    //以某人的id为键，其友情值在大根堆中的索引为值建立的索引表
    private HashMap<Integer, Integer> index;

    public Heap() {
        heap = new ArrayList<>();
        index = new HashMap<>();
    }

    public int getBestId() {
        if (heap.isEmpty()) {
            return 0;
        }
        return heap.get(0).getId();
    }

    public int getBestValue() {
        //一开始一个子节点都没有，返回0
        if (heap.isEmpty()) {
            return 0;
        }
        return heap.get(0).getValue();
    }

    public int getValue(int id) {
        //传入的是人的id，要先通过索引表转化为在大根堆中的索引
        //进而得到相应的值
        return heap.get(index.get(id)).getValue();
    }

    public boolean containsId(int id) {
        return index.containsKey(id);
    }

    private void swap(int index1, int index2) {
        //先交换在索引表中的值
        this.index.replace(heap.get(index1).getId(), index2);
        this.index.replace(heap.get(index2).getId(), index1);
        //再交换大根堆中的位置
        Value temp = heap.get(index1);
        heap.set(index1, heap.get(index2));
        heap.set(index2, temp);
    }

    public void addValue(int id, int value) {
        //注意传入的参数的相对位置
        heap.add(new Value(value, id));
        index.put(id, heap.size() - 1);
        adjustUp(heap.size() - 1);
    }

    public void enhanceValue(int id, int value) {
        //给为id的人的友情值增加
        int pos = index.get(id);
        Value temp = heap.get(pos);
        temp.addValue(value);
        heap.set(pos, temp);
        if (value > 0) {
            adjustUp(pos);
        }
        else {
            adjustDown(pos, false);
        }
    }

    public void deleteValue(int id) {
        int pos = index.get(id);
        //下调该点到二叉树最底处，把其删除，其后结点重新加入大根堆中
        adjustDown(pos, true);
        pos = index.get(id);
        //从调换后的结点的后一个结点开始从前往后向前覆盖，并重新调整大顶堆
        for (int i = pos; i < heap.size() - 1; i++) {
            heap.set(i, heap.get(i + 1));
            index.replace(heap.get(i).getId(), i);
            adjustUp(i);
        }
        //删除id在索引表中的索引
        index.remove(id);
        //删除最后一个元素，实现删除功能
        heap.remove(heap.size() - 1);
    }

    private void adjustUp(int index) {
        int pos = index;
        int faIndex = (pos - 1) / 2;
        while (pos != 0 && heap.get(pos).bestThan(heap.get(faIndex))) {
            swap(pos, faIndex);
            pos = faIndex;
            //注意更新父节点索引
            faIndex = (pos - 1) / 2;
        }
    }

    private void adjustDown(int index, boolean isDelete) {
        int pos = index;
        int leftChi = 2 * pos + 1;
        int rightChi = 2 * pos + 2;
        while (leftChi <= heap.size() - 1) {
            Value left = heap.get(leftChi);
            int isSwap = 0;
            if ((rightChi > heap.size() - 1) || left.bestThan(heap.get(rightChi))) {
                if (isDelete || left.bestThan(heap.get(pos))) {
                    swap(pos, leftChi);
                    pos = leftChi;
                    isSwap = 1;
                }
                if (!isDelete && isSwap == 0) {
                    break;
                }
            }
            else {
                if (isDelete || heap.get(rightChi).bestThan(heap.get(pos))) {
                    swap(pos, rightChi);
                    pos = rightChi;
                    isSwap = 1;
                }
                if (!isDelete && isSwap == 0) {
                    break;
                }
            }
            //更新左右孩子的索引
            leftChi = 2 * pos + 1;
            rightChi = 2 * pos + 2;
        }
    }
}
