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
            else if (c == '+' || c == '-') {
                if (c == '+') {
                    tokens.add(new Token(Token.Type.ADD, "+"));
                }
                else {
                    tokens.add(new Token(Token.Type.SUB, "-"));
                }
            }
            else if (c == '*') {
                tokens.add(new Token(Token.Type.MUL, "*"));
            }
            else if (c == '^') {
                tokens.add(new Token(Token.Type.POW, "^"));
            }
            else if (c == 'x' || c == 'y' || c == 'n') {
                tokens.add(new Token(Token.Type.VAR, String.valueOf(c)));
            }
            else if (Character.isDigit(c)) {
                pos = getNum(input, pos);
            }
            else if (c == 's') {
                tokens.add(new Token(Token.Type.SIN, "sin"));
                pos += 2;
            }
            else if (c == 'c') {
                tokens.add(new Token(Token.Type.COS, "cos"));
                pos += 2;
            }
            else if (c == 'f') {
                tokens.add(new Token(Token.Type.FUN, "fun"));
            }
            else if (c == ',' || c == '\n') {
                tokens.add(new Token(Token.Type.END, "-1"));
            }
            if (!Character.isDigit(c)) {
                pos++;
            }
        }
    }

    public int getNum(String input, int pos) {
        int newPos = pos;
        char cur = input.charAt(newPos);
        StringBuilder sb = new StringBuilder();
        int flag = 0;
        while (cur == '0') {
            newPos++;
            if (newPos == input.length() || !Character.isDigit(input.charAt(newPos))) {
                flag = 1;
                break;
            }
            cur = input.charAt(newPos);
        }
        if (flag == 0) {
            while (cur >= '0' && cur <= '9') {
                sb.append(cur);
                newPos++;
                if (newPos >= input.length()) {
                    break;
                }
                cur = input.charAt(newPos);
            }
        }
        else {
            sb.append("0");
        }
        tokens.add(new Token(Token.Type.NUM, sb.toString()));
        return newPos;
    }

    public void reset() {
        index = 0;
        tokens.clear();
    }

    public Token getToke() {
        if (isEnd()) {
            return new Token(Token.Type.END, " ");
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
