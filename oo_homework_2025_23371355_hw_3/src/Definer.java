import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Definer {
    private static HashMap<String, ArrayList<String>> paraMap = new HashMap<>(); //不同函数的参数列表
    private static HashMap<String, Factor> norFunMap = new HashMap<>(); //自定义普通函数的函数式
    //自定义递推函数
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

    public static void build(Scanner sc, int mode, String n) {
        //自定义函数的构建模式，当其为0时进行普通函数的构建，为1时进行递推函数的构建
        scan = sc;
        if (mode == 1) {
            if (n.equals("1")) {
                for (int i = 0; i < 3; i++) {
                    String input = scan.nextLine();
                    input = Process.process(input);
                    process(input);
                    lexer.reset();
                }
                for (int i = 2; i < 6; i++) {
                    addFun(i);
                }
            }
        }
        else {
            int num = Integer.parseInt(n);
            for (int i = 0; i < num; i++) {
                String input = scan.nextLine();
                input = Process.process(input);
                process(input);
                lexer.reset();
            }
        }
    }

    public static Factor callFunc(String funName, int lay, Factor repOne, Factor repTwo) {
        ArrayList<String> paraList = paraMap.get(funName);
        String var1 = paraList.get(0);
        String var2 = (paraList.size() == 2) ? paraList.get(1) : "\0";
        Factor oriExpr;
        if (funName.equals("f")) {
            oriExpr = funMap.get(lay);
        }
        else {
            oriExpr = norFunMap.get(funName);
        }
        Factor expr = oriExpr.replaceFun(repOne, repTwo, var1, var2);
        return expr;
    }

    public static void addFun(Integer n) {
        Expr newExpr;
        newExpr = new Expr(BigInteger.ONE);
        Term newTerm1 = new Term(Token.Type.ADD);
        Factor simExpr = callFunc("f", n - 1,  funN11.deepCopy(),
            (funN12 == null) ? null : funN12.deepCopy());
        Factor simNum = num1;
        newTerm1.addFactor(simExpr);
        newTerm1.addFactor(simNum);
        newExpr.addTerm(newTerm1, Token.Type.ADD);
        simExpr = callFunc("f", n - 2, funN21.deepCopy(),
            (funN22 == null) ? null : funN22.deepCopy());
        simNum = num2;
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

    public static void processVar(String funName) {
        lexer.nextToken();
        //跳过函数的左括号
        ArrayList<String> paraList = new ArrayList<>();
        paraList.add(lexer.getToke().getValue());
        lexer.nextToken();
        //跳过第一个形参
        Token token = lexer.getToke();
        if (token.getType().equals(Token.Type.END)) {
            lexer.nextToken(); //跳过逗号
            paraList.add(lexer.getToke().getValue()); //获得第二个形参
            lexer.nextToken();
            //跳过第二个形参
        }
        if (!paraMap.containsKey(funName)) {
            paraMap.put(funName, paraList);
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
        int num = paraMap.get("f").size();
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
        String input = Process.process(str);
        lexer = new Lexer(input);
        while (!lexer.isEnd() && !lexer.getToke().getType().equals(Token.Type.FUN)) {
            lexer.nextToken();
        } //找到函数名
        String funName = lexer.getToke().getValue();
        lexer.nextToken();
        Token token = lexer.getToke();
        if (funName.equals("f")) {
            lexer.nextToken(); //跳过序号
            processVar(funName); //处理函数形参
            if (token.getType().equals(Token.Type.VAR) && token.getValue().equals("n")) {
                processN();
            } else {
                if (token.getValue().equals("0")) {
                    funMap.put(0, buildExpr());
                } else {
                    funMap.put(1, buildExpr());
                }
            }
        }
        else {
            processVar(funName); //处理函数形参
            norFunMap.put(funName, buildExpr());
        }
    }

    public static Expr buildExpr() {
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        return expr;
    }

}