import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        //读入输入
        String input = sc.nextLine();
        //构造此法分析器
        Lexer lexer = new Lexer(input);
        //构造语法分析器
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        expr.print(expr.getFactor());
    }
}
