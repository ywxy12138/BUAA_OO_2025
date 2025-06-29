public class MessageNode extends Node {
    private Message message;

    public MessageNode(Message message) {
        super();
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
