import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Definer.build(sc, 0, sc.nextLine());
        Definer.build(sc, 1, sc.nextLine());
        String input = sc.nextLine();
        //构造此法分析器
        input = Process.process(input); //字符串的预处理
        Lexer lexer = new Lexer(input);
        //构造语法分析器
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        Poly poly = expr.toPoly();
        System.out.println(poly.buildString(0));
    }
}
