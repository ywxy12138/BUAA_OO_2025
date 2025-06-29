public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
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

    public Expr parseExpr() {
        Token token = isToken();
        Expr expr = new Expr(1);
        expr.addTerm(parseTerm(), token.getType());
        //获取表达式的第一项
        token = lexer.getToke();

        while ((token.getType().equals(Token.Type.ADD) || token.getType().equals(Token.Type.SUB))
                && !lexer.isEnd()) {
            lexer.nextToken();
            expr.addTerm(parseTerm(), token.getType());
            token = lexer.getToke();
        }
        return expr;
    }

    public Term parseTerm() {
        Token token;
        token = isToken();
        Term term = new Term(token.getType());
        term.addFactor(parseFactor());
        Token mulToken = lexer.getToke();

        while (!lexer.isEnd() && mulToken.getType().equals(Token.Type.MUL)) {
            lexer.nextToken();
            term.addFactor(parseFactor());
            mulToken = lexer.getToke();
        }
        return term;
    }

    public Integer getPow() {
        Token token = lexer.getToke();
        if (token.getType().equals(Token.Type.POW)) {
            lexer.nextToken();
            token = lexer.getToke();
            if (token.getType().equals(Token.Type.ADD)) {
                lexer.nextToken();
            }
            token = lexer.getToke();
            Integer exp = Integer.parseInt(token.getValue());
            lexer.nextToken();
            return exp;
        }
        return 1;
    }

    public Expr parseExpFactor() {
        lexer.nextToken();
        Expr expr = parseExpr();
        lexer.nextToken();
        Integer exp = getPow();
        expr.setExp(exp);
        return expr;
    }

    public PowVar parsePowVar() {
        String type = lexer.getToke().getValue();
        lexer.nextToken();
        Integer exp = getPow();
        return new PowVar(type, exp);
    }

    public Const parseConst() {
        Token token = lexer.getToke();
        if (token.getType().equals(Token.Type.NUM)) {
            token = new Token(Token.Type.ADD, "+");
        }
        else {
            lexer.nextToken();
        }
        Token numToken = lexer.getToke();
        lexer.nextToken();
        return new Const(token, numToken);
    }

    public TriFun parseTriFun() {
        Token.Type type;
        type = lexer.getToke().getType();
        lexer.nextToken();
        lexer.nextToken();
        Factor factor = parseFactor();
        lexer.nextToken();
        Integer exp = getPow();
        return new TriFun(factor, exp, type);
    }

    public Factor parseRecFun() {
        lexer.nextToken(); //跳过'f'
        int lay;
        lay = Integer.parseInt(lexer.getToke().getValue()); //获得lay
        lexer.nextToken(); //跳过调用序号
        lexer.nextToken(); //跳过函数的左括号
        Expr repOne = parseExpr();
        Expr repTwo = new Expr(1);
        if (lexer.getToke().getType().equals(Token.Type.END)) {
            lexer.nextToken(); //跳过逗号
            repTwo = parseExpr();
        }
        lexer.nextToken();//跳过函数的右括号
        return Definer.callFunc(lay, repOne, repTwo);
    }

    public Factor parseFactor() {
        Token.Type type = lexer.getToke().getType();
        if (type.equals(Token.Type.LPAREN)) {
            return parseExpFactor();
        }
        else if (type.equals(Token.Type.VAR)) {
            return parsePowVar();
        }
        else if (type.equals(Token.Type.SIN) || type.equals(Token.Type.COS)) {
            return parseTriFun();
        }
        else if (type.equals(Token.Type.FUN)) {
            return parseRecFun();
        }
        else {
            return parseConst();
        }
    }

}
