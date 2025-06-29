import java.math.BigInteger;
import java.util.HashMap;

public class PowVar implements Factor {
    private Integer exp;
    private String type;

    public PowVar(String type, Integer exp) {
        this.exp = exp;
        this.type = type;
    }

    public Factor deepCopy() {
        return new PowVar(type, exp);
    }

    public Factor replaceFun(Factor repOne, Factor repTwo, String var1, String var2) {
        Expr newFactor1 = new Expr(exp);
        Term newTerm1 = new Term(Token.Type.ADD);
        newTerm1.addFactor(repOne.deepCopy());
        Expr newFactor2 = new Expr(exp);
        Term newTerm2 = new Term(Token.Type.ADD);
        newTerm2.addFactor(repTwo == null ? null : repTwo.deepCopy());
        newFactor1.addTerm(newTerm1, Token.Type.ADD);
        newFactor2.addTerm(newTerm2, Token.Type.ADD);
        if (type.equals(var1)) {
            return newFactor1;
        }
        if (type.equals(var2)) {
            return newFactor2;
        }
        return deepCopy();
    }

    public Poly toPoly() {
        Poly poly = new Poly();
        HashMap<Poly, Integer> none = new HashMap<>();
        Unit unit = new Unit(BigInteger.ONE, exp, none, none);
        poly.addUnit(unit);
        return poly;
    }
}
