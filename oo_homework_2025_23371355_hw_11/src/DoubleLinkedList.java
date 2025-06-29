import com.oocourse.spec3.main.MessageInterface;

import java.util.ArrayList;
import java.util.List;

public class DoubleLinkedList {
    private Node head;
    private int size;

    public DoubleLinkedList() {
        head = null;
        size = 0;
    }

    public void insertHead(Node newNode) {
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

    public void deleteNode(Node node) {
        if (node.getPrev() == null) {
            head = head.getNext();
            if (head != null) {
                head.setPrev(null);
            }
        }
        else if (node.getNext() == null) {
            Node prev = node.getPrev();
            prev.setNext(null);
        }
        else {
            Node prev = node.getPrev();
            Node next = node.getNext();
            prev.setNext(next);
            next.setPrev(prev);
        }
        size--;
    }

    public List<Integer> transferToArticleList() {
        Node temp = head;
        List<Integer> list = new ArrayList<>();
        while (temp != null) {
            list.add(((ArticleNode) temp).getArticleId());
            temp = temp.getNext();
        }
        return list;
    }

    public List<MessageInterface> transferToMessageList() {
        Node temp = head;
        List<MessageInterface> list = new ArrayList<>();
        while (temp != null) {
            list.add(((MessageNode) temp).getMessage());
            temp = temp.getNext();
        }
        return list;
    }

    public List<Integer> queryArticleList() {
        List<Integer> list = new ArrayList<>();
        Node temp = head;
        int len = Math.min(size, 5);
        while (temp != null && list.size() < len) {
            list.add(((ArticleNode) temp).getArticleId());
            temp = temp.getNext();
        }
        return list;
    }

    public List<MessageInterface> queryMessageList() {
        List<MessageInterface> list = new ArrayList<>();
        Node temp = head;
        int len = Math.min(size, 5);
        while (temp != null && list.size() < len) {
            list.add(((MessageNode) temp).getMessage());
            temp = temp.getNext();
        }
        return list;
    }
}
