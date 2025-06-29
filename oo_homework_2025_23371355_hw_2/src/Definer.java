import java.util.HashMap;
import java.util.Scanner;

public class Definer {
    private static int num;
    private static String var1;
    private static String var2;
    private static Const num1; //递归调用时n-1函数的常量因子
    private static Const num2; //n-2的常量因子
    private static HashMap<Integer, Factor> funMap = new HashMap<>();
    private static Expr funN11;
    private static Expr funN12;
    private static Expr funN21;
    private static Expr funN22;
    private static Expr funN;
    private static Lexer lexer;
    private static Scanner scan;

    public static void build(Scanner sc) {
        scan = sc;
        for (int i = 0; i < 3; i++) {
            String input = scan.nextLine();
            process(input);
            lexer.reset();
        }
        for (int i = 2; i < 6; i++) {
            addFun(i);
        }
    }

    public static Factor callFunc(int lay, Expr repOne, Expr repTwo) {
        Factor oriExpr = funMap.get(lay);
        Factor expr = oriExpr.replaceFun(repOne, repTwo, var1, var2);
        return expr;
    }

    public static String getOutMoreBlank(String str) {
        int len = str.length();
        String newStr = "";
        for (int i = 0; i < len; i++) {
            if (str.charAt(i) == ' ' || str.charAt(i) == '\t') {
                continue;
            }
            newStr += str.charAt(i);
        }
        return newStr;
    }

    public static String getOutMoreSign(String str) {
        int len = str.length();
        String newStr = "";
        for (int i = 0; i < len; i++) {
            if (str.charAt(i) == '+' || str.charAt(i) == '-') {
                int sign = 1;
                while (i < len && str.charAt(i) == '+' || str.charAt(i) == '-') {
                    sign *= ((str.charAt(i) == '+') ? 1 : -1);
                    i++;
                }
                newStr += ((sign > 0) ? "+" : "-");
            }
            newStr += str.charAt(i);
        }
        return newStr;
    }

    public static Factor replaceFun(Factor expr, Factor repOne, Factor repTwo) {
        return expr.replaceFun(repOne, repTwo, var1, var2);
    }

    public static void addFun(Integer n) {
        Expr newExpr;
        newExpr = new Expr(1);
        Term newTerm1 = new Term(Token.Type.ADD);
        Factor fun1 =  funMap.get(n - 1).deepCopy();
        Factor fun2;
        fun2 = funMap.get(n - 2).deepCopy();
        Factor simExpr = replaceFun(fun1, funN11.deepCopy(),
            (funN12 == null) ? null : funN12.deepCopy());
        Factor simNum = num1.deepCopy();
        newTerm1.addFactor(simExpr);
        newTerm1.addFactor(simNum);
        newExpr.addTerm(newTerm1, Token.Type.ADD);
        simExpr = replaceFun(fun2, funN21.deepCopy(),
            (funN22 == null) ? null : funN22.deepCopy());
        simNum = num2.deepCopy();
        Term newTerm2 = new Term(Token.Type.ADD);
        newTerm2.addFactor(simExpr);
        newTerm2.addFactor(simNum);
        newExpr.addTerm(newTerm2, Token.Type.ADD);
        if (funN != null) {
            Term newTerm3 = new Term(Token.Type.ADD);
            newTerm3.addFactor(funN.deepCopy());
            newExpr.addTerm(newTerm3, Token.Type.ADD);
        }
        funMap.put(n, newExpr);
    }

    public static void processVar() {
        lexer.nextToken();
        //跳过函数的左括号
        var1 = lexer.getToke().getValue();
        lexer.nextToken();
        //跳过第一个形参
        Token token = lexer.getToke();
        if (token.getType().equals(Token.Type.END)) {
            num = 2;
            lexer.nextToken(); //跳过逗号
            var2 = lexer.getToke().getValue(); //获得第二个形参
            lexer.nextToken();
            //跳过第二个形参
        }
        else {
            num = 1;
            var2 = "\0";
        }
        lexer.nextToken();
        //跳过函数的右括号
    }

    public static Expr processFun() {
        lexer.nextToken(); //跳过乘号
        for (int i = 0; i < 5; i++) {
            lexer.nextToken(); //跳过f、n、-、1、( 或 f、n、-、2、(
        }
        return buildExpr();
    }

    public static void processN() {
        Parser parser = new Parser(lexer);
        num1 = parser.parseConst();
        funN11 = processFun();
        if (num == 2) {
            lexer.nextToken(); //跳过逗号
            funN12 = buildExpr();
            lexer.nextToken(); //跳过函数的右括号
            num2 = parser.parseConst(); //由于提前对多余的符号进行了去除，因此这里连接两个调用函数的只能是一个符号，因此这里直接读取一个常量因子即可
            funN21 = processFun();
            lexer.nextToken();
            funN22 = buildExpr();
            lexer.nextToken();
        }
        else {
            lexer.nextToken(); //跳过右括号
            num2 = parser.parseConst();
            funN21 = processFun();
            lexer.nextToken();
        }
        if (!lexer.getToke().getType().equals(Token.Type.END)) {
            funN = buildExpr();
        }
        else {
            funN = null;
        }
    }

    public static void process(String str) {
        String string = getOutMoreBlank(str);
        String input = getOutMoreSign(string);
        lexer = new Lexer(input);
        while (!lexer.isEnd() && !lexer.getToke().getType().equals(Token.Type.FUN)) {
            lexer.nextToken();
        }
        lexer.nextToken();
        Token token = lexer.getToke();
        if (token.getType().equals(Token.Type.VAR) && token.getValue().equals("n")) {
            lexer.nextToken();
            processVar();
            processN();
        }
        else {
            if (token.getValue().equals("0")) {
                lexer.nextToken();
                processVar();
                funMap.put(0, buildExpr());
            }
            else {
                lexer.nextToken();
                processVar();
                funMap.put(1, buildExpr());
            }
        }
    }

    public static Expr buildExpr() {
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        return expr;
    }

}