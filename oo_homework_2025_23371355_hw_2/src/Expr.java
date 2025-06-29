import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Expr implements Factor {
    private ArrayList<Term> terms;
    private Integer exp;

    public Expr(Integer pow) {
        terms = new ArrayList<>();
        this.exp = pow;
    }

    public void addTerm(Term term, Token.Type op) {
        term.setSign(op);
        terms.add(term);
    }

    public Factor replaceFun(Factor repOne, Factor repTwo, String var1, String var2) {
        Expr newExpr = new Expr(exp);
        int len = terms.size();
        for (int i = 0; i < len; i++) {
            Term term = terms.get(i).deepCopy();
            Term newTerm = term.replaceFun(repOne, repTwo, var1, var2);
            newExpr.addTerm(newTerm, Token.Type.ADD);
        }
        return newExpr;
    }

    public void setExp(Integer exp) {
        this.exp *= exp;
    }

    public Factor deepCopy() {
        Expr expr = new Expr(exp);
        for (Term term : terms) {
            expr.addTerm(term.deepCopy(), Token.Type.ADD);
        }
        return expr;
    }

    public Poly toPoly() {
        Poly temp = new Poly();
        for (Term term : terms) {
            Poly poly = term.toPoly();
            temp.addPoly(poly);
        }
        if (exp == 0) {
            Unit unit = new Unit(BigInteger.ONE, 0, new HashMap<>(), new HashMap<>());
            Poly ret = new Poly();
            ret.addUnit(unit);
            return ret;
        } else {
            Poly ans = new Poly();
            for (int i = 0; i < exp; i++) {
                ans.mulPoly(temp);
            }
            return ans;
        }
    }
}