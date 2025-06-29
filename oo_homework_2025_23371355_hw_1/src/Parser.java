import java.math.BigInteger;
import java.util.HashMap;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public void omitBlank() {
        if (lexer.isEnd()) {
            return;
        }
        while (!lexer.isEnd() && lexer.getToke().getType().equals(Token.Type.BLANK)) {
            lexer.nextToken();
        }
        //排除可能的空白符
    }

    public Token isToken() {
        Token token;
        if (lexer.getToke().getType().equals(Token.Type.SUB)) {
            token = new Token(Token.Type.SUB, "-");
            lexer.nextToken();
        }
        else {
            if (lexer.getToke().getType().equals(Token.Type.ADD)) {
                lexer.nextToken();
            }
            token = new Token(Token.Type.ADD, "+");
        }
        //获取表达式第一项可能的符号
        return token;
    }

    public int subExpPow(HashMap<Integer, BigInteger> factors, Token powToken, Term term) {
        if (powToken.getType().equals(Token.Type.POW)) {
            lexer.nextToken();
            omitBlank();
            Token numToken = lexer.getToke();
            if (numToken.getType().equals(Token.Type.ADD)) {
                lexer.nextToken();
                numToken = lexer.getToke();
            }
            int num = Integer.parseInt(numToken.getValue());
            if (num != 0) {
                for (int i = 0; i < num - 1; i++) {
                    term.addFactor(factors);
                }
            }
            lexer.nextToken();
            return num;
        }
        return 1;
    }

    public Expr parseExpr() {
        omitBlank();
        Token token = isToken();
        Expr expr = new Expr();
        expr.addTerm(parseTerm().getFactors(), token);
        //获取表达式的第一项
        omitBlank();
        token = lexer.getToke();

        while ((token.getType().equals(Token.Type.ADD) || token.getType().equals(Token.Type.SUB))
                && !lexer.isEnd()) {
            lexer.nextToken();
            omitBlank();
            expr.addTerm(parseTerm().getFactors(), token);
            omitBlank();
            token = lexer.getToke();
        }
        return expr;
    }

    public Term parseTerm() {
        omitBlank();
        Token token;
        token = isToken();
        omitBlank();
        Term term = new Term();
        HashMap<Integer, BigInteger> factorsMap = parseFactor().getFactor();
        omitBlank();
        Token mulToken = lexer.getToke();
        if (subExpPow(factorsMap, mulToken, term) != 0) {
            term.addFactor(factorsMap);
        }
        else {
            factorsMap.clear();
            factorsMap.put(0, BigInteger.ONE);
            term.addFactor(factorsMap);
        }
        omitBlank();
        mulToken = lexer.getToke();
        while (!lexer.isEnd() && mulToken.getType().equals(Token.Type.MUL)) {
            lexer.nextToken();
            omitBlank();
            HashMap<Integer, BigInteger> factors = parseFactor().getFactor();
            omitBlank();
            Token powToken = lexer.getToke();
            if (subExpPow(factors, powToken, term) != 0) {
                term.addFactor(factors);
            }
            omitBlank();
            mulToken = lexer.getToke();
        }
        term.setFactors(token);
        return term;
    }

    public Factor parseFactor() {
        if (!lexer.isEnd() && lexer.getToke().getType().equals(Token.Type.LPAREN)) {
            lexer.nextToken();
            Factor exp = parseExpr();
            lexer.nextToken();
            return exp;
        }
        else if (!lexer.isEnd() && lexer.getToke().getType().equals(Token.Type.VAR)) {
            lexer.nextToken();
            omitBlank();
            Token pow = lexer.getToke();
            Var var;
            if (pow.getType().equals(Token.Type.POW)) {
                lexer.nextToken();
                omitBlank();
                Token nowToken = lexer.getToke();
                if (nowToken.getType().equals(Token.Type.ADD)) {
                    lexer.nextToken();
                }
                var = new Var(lexer.getToke());
                lexer.nextToken();
            }
            else {
                var = new Var(new Token(Token.Type.NUM, "1"));
            }
            return var;
        }
        else {
            Token tokenOp;
            if (lexer.getToke().getType().equals(Token.Type.SUB)) {
                tokenOp = new Token(Token.Type.SUB, "-");
                lexer.nextToken();
            }
            else if (lexer.getToke().getType().equals(Token.Type.ADD)) {
                tokenOp = new Token(Token.Type.ADD, "+");
                lexer.nextToken();
            }
            else {
                tokenOp = new Token(Token.Type.ADD, "+");
            }
            Factor num = new Const(tokenOp, lexer.getToke());
            lexer.nextToken();
            return num;
        }
    }

}
