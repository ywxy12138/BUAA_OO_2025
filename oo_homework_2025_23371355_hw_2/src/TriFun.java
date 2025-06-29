import java.math.BigInteger;
import java.util.HashMap;

public class TriFun implements Factor {
    private Factor factor;
    private Integer exp;
    private Token.Type type;

    public TriFun(Factor factor, Integer exp, Token.Type type) {
        this.factor = factor;
        this.exp = exp;
        this.type = type;
    }

    public Factor replaceFun(Factor repOne, Factor repTwo, String var1, String var2) {
        Factor newFactor = factor.deepCopy().replaceFun(repOne, repTwo, var1, var2);
        return new TriFun(newFactor, exp, type);
    }

    public Factor deepCopy() {
        return new TriFun(factor, exp, type);
    }

    public Poly toPoly() {
        Poly poly = new Poly();
        Unit unit;
        HashMap<Poly, Integer> none = new HashMap<>();
        if (type == Token.Type.SIN) {
            HashMap<Poly, Integer> sinMap = new HashMap<>();
            sinMap.put(factor.deepCopy().toPoly(), exp);
            if (exp == 0) {
                unit = new Unit(BigInteger.ONE, 0, none, none);
            } else {
                unit = new Unit(BigInteger.ONE, 0, sinMap, none);
            }
        } else {
            HashMap<Poly, Integer> cosMap = new HashMap<>();
            cosMap.put(factor.deepCopy().toPoly(), exp);
            if (exp == 0) {
                unit = new Unit(BigInteger.ONE, 0, none, none);
            } else {
                unit = new Unit(BigInteger.ONE, 0, none, cosMap);
            }
        }
        unit.simplifyUnit();
        poly.addUnit(unit);
        return poly;
    }
}
