import java.util.ArrayList;

public class Term {
    private ArrayList<Factor> factors;
    private Token.Type sign;

    public Term(Token.Type sign) {
        this.sign = sign;
        this.factors = new ArrayList<>();
    }

    public void addFactor(Factor factor) {
        factors.add(factor);
    }

    public Term replaceFun(Factor repOne, Factor repTwo, String var1, String var2) {
        Term newTerm = new Term(sign);
        int len = factors.size();
        for (int i = 0; i < len; i++) {
            Factor factor = factors.get(i).deepCopy();
            newTerm.addFactor(factor.replaceFun(repOne, repTwo, var1, var2));
        }
        return newTerm;
    }

    public Term deepCopy() {
        Term term = new Term(sign);
        for (Factor factor : factors) {
            term.addFactor(factor.deepCopy());
        }
        return term;
    }

    public Poly toPoly() {
        Poly poly = new Poly();
        for (Factor factor : factors) {
            Poly temp = factor.toPoly();
            poly.mulPoly(temp);
        }
        poly.setSign(this.sign);
        return poly;
    }

    public void setSign(Token.Type sign) {
        if (sign != this.sign) {
            this.sign = Token.Type.SUB;
            return;
        }
        this.sign = Token.Type.ADD;
    }
}
