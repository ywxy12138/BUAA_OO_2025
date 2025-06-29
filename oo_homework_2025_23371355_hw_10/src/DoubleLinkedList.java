import java.util.ArrayList;

public class DoubleLinkedList {
    private ArticleNode head;
    private int size;

    public DoubleLinkedList() {
        head = null;
        size = 0;
    }

    public void insertHead(ArticleNode newNode) {
        if (head == null) {
            head = newNode;
        }
        else {
            newNode.setNext(head);
            head.setPrev(newNode);
            head = newNode;
        }
        size++;
    }

    public void deleteNode(ArticleNode node) {
        if (node.getPrev() == null) {
            head = head.getNext();
            if (head != null) {
                head.setPrev(null);
            }
        }
        else if (node.getNext() == null) {
            ArticleNode prev = node.getPrev();
            prev.setNext(null);
        }
        else {
            ArticleNode prev = node.getPrev();
            ArticleNode next = node.getNext();
            prev.setNext(next);
            next.setPrev(prev);
        }
        size--;
    }

    public ArrayList<Integer> transferToList() {
        ArticleNode temp = head;
        ArrayList<Integer> list = new ArrayList<>();
        while (temp != null) {
            list.add(temp.getArticleId());
            temp = temp.getNext();
        }
        return list;
    }

    public ArrayList<Integer> queryList() {
        ArrayList<Integer> list = new ArrayList<>();
        ArticleNode temp = head;
        int len = Math.min(size, 5);
        while (temp != null && list.size() < len) {
            list.add(temp.getArticleId());
            temp = temp.getNext();
        }
        return list;
    }
}
