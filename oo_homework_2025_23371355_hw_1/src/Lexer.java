import java.util.ArrayList;

public class Lexer {
    private final ArrayList<Token> tokens = new ArrayList<>();
    private int index = 0;

    public Lexer(String input) {
        int pos = 0;
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '(') {
                tokens.add(new Token(Token.Type.LPAREN, "("));
            }
            else if (c == ')') {
                tokens.add(new Token(Token.Type.RPAREN, ")"));
            }
            else if (c == '+') {
                tokens.add(new Token(Token.Type.ADD, "+"));
            }
            else if (c == '-') {
                tokens.add(new Token(Token.Type.SUB, "-"));
            }
            else if (c == '*') {
                tokens.add(new Token(Token.Type.MUL, "*"));
            }
            else if (c == '^') {
                tokens.add(new Token(Token.Type.POW, "^"));
            }
            else if (c == ' ' || c == '\t') {
                tokens.add(new Token(Token.Type.BLANK, " "));
            }
            else if (c == 'x') {
                tokens.add(new Token(Token.Type.VAR, "x"));
            }
            else {
                char cur = input.charAt(pos);
                StringBuilder sb = new StringBuilder();
                int flag = 0;
                while (cur == '0') {
                    pos++;
                    if (pos == input.length() || !Character.isDigit(input.charAt(pos))) {
                        flag = 1;
                        break;
                    }
                    cur = input.charAt(pos);
                }
                if (flag == 0) {
                    while (cur >= '0' && cur <= '9') {
                        sb.append(cur);
                        pos++;
                        if (pos >= input.length()) {
                            break;
                        }
                        cur = input.charAt(pos);
                    }
                }
                else {
                    sb.append("0");
                }
                tokens.add(new Token(Token.Type.NUM, sb.toString()));
            }
            if (!Character.isDigit(c)) {
                pos++;
            }
        }
    }

    public Token getToke() {
        if (index >= tokens.size()) {
            return new Token(Token.Type.BLANK, " ");
        }
        return tokens.get(index);
    }

    public void nextToken() {
        index++;
    }

    public boolean isEnd() {
        return index >= tokens.size();
    }
}
