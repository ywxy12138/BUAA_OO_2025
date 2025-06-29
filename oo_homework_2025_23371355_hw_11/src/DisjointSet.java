import java.util.HashMap;
import java.util.HashSet;

public class DisjointSet {
    //以每个人为结点形成无向图
    //以当前结点id为键，以其所在集合的根节点id为值的hashmap
    private HashMap<Integer, Integer> roots;
    private HashMap<Integer, Integer> ranks;

    public DisjointSet() {
        roots = new HashMap<>();
        ranks = new HashMap<>();
    }

    public void add(Person person) {
        if (!roots.containsKey(person.getId())) {
            roots.put(person.getId(), person.getId());
            ranks.put(person.getId(), 1);
        }
    }

    //按路径压缩，返回给定结点所在集合的根节点id
    public int find(int id) {
        int rootId = id;
        while (rootId != roots.get(rootId)) {
            rootId = roots.get(rootId);
        }
        //按路径压缩
        int now = id;
        while (now != rootId) {
            //获取当前节点的父节点
            int fa = roots.get(now);
            //修改当前节点的父节点为实际上的根节点
            roots.replace(now, rootId);
            //当前节点换成父节点，进入下一次循环
            now = fa;
        }
        return rootId;
    }

    //按秩合并，当返回true时，说明这两个节点的根节点相同，又因为这两个节点相连，所以将形成环
    //否则，将两个根节点合并，返回false
    public boolean union(int id1, int id2) {
        //获取两个id所在集合的根节点id
        int root1 = find(id1);
        int root2 = find(id2);
        //若相等说明不需要进行合并
        if (root1 == root2) {
            return true;
        }
        //否则，比较两个集合构成的树的高度，低树根节点挂在高树根节点上
        int rank1 = ranks.get(root1);
        int rank2 = ranks.get(root2);
        if (rank1 > rank2) {
            //修改低树根节点的根节点id为高树根节点id
            roots.put(root2, root1);
        }
        else {
            //只有在两根树高度相同时需要去修改其中一颗树的高度为两树高度加一
            if (rank1 == rank2) {
                ranks.put(root2, rank2 + 1);
            }
            roots.put(root1, root2);
        }
        return false;
    }

    public HashSet<Integer> delete(int id) {
        //获得该节点对应集合的根节点
        int rootId = find(id);
        HashSet<Integer> deleted = new HashSet<>();
        for (HashMap.Entry<Integer, Integer> entry : roots.entrySet()) {
            if (find(entry.getKey()) == rootId) {
                deleted.add(entry.getKey());
            }
        }
        //将所有该集合内的节点复原，重新加入并查集
        for (Integer key : deleted) {
            roots.replace(key, key);
            ranks.replace(key, 1);
        }
        //返回hashset，重新添加加入并查集
        return deleted;
    }
}
