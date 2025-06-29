public class Node {
    private Node prev;
    private Node next;

    public Node() {
        this.next = null;
        this.prev = null;
    }

    public Node getPrev() {
        return prev;
    }

    public Node getNext() {
        return next;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public void setNext(Node next) {
        this.next = next;
    }
}
