public class Token {
    public enum Type {
        ADD, SUB, MUL, POW, LPAREN, RPAREN, BLANK, NUM, VAR
    }

    private Type type;
    private String value;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
