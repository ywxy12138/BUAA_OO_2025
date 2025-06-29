import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String n = sc.nextLine();
        if (n.equals("1")) {
            Definer.build(sc); //接收自定义函数的定义
        }
        String input = sc.nextLine();
        //构造此法分析器
        input = Definer.getOutMoreBlank(input);
        input = Definer.getOutMoreSign(input);
        Lexer lexer = new Lexer(input);
        //构造语法分析器
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        Poly poly = expr.toPoly();
        //poly.simplifyPoly();
        System.out.println(poly.buildString(0));
    }
}
