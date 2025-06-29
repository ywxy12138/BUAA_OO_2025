import java.math.BigInteger;
import java.util.HashMap;

public class TriFun implements Factor {
    private Factor factor;
    private BigInteger exp;
    private Token.Type type;

    public TriFun(Factor factor, BigInteger exp, Token.Type type) {
        this.factor = factor;
        this.exp = exp;
        this.type = type;
    }

    public Factor replaceFun(Factor repOne, Factor repTwo, String var1, String var2) {
        Factor newFactor = factor.replaceFun(repOne, repTwo, var1, var2);
        return new TriFun(newFactor, exp, type);
    }

    public Factor deepCopy() {
        return new TriFun(factor.deepCopy(), exp, type);
    }

    public Poly toPoly() {
        Poly poly = new Poly();
        Unit unit;
        HashMap<Poly, BigInteger> none = new HashMap<>();
        Poly tripoly = factor.toPoly();
        if (type == Token.Type.SIN) {
            HashMap<Poly, BigInteger> sinMap = new HashMap<>();
            sinMap.put(tripoly, exp);
            if (exp.equals(BigInteger.ZERO)) {
                unit = new Unit(BigInteger.ONE, BigInteger.ZERO, none, none);
            } else {
                if (tripoly.isAllZero()) {
                    unit = new Unit(BigInteger.ZERO, BigInteger.ZERO, none, none);
                }
                else {
                    unit = new Unit(BigInteger.ONE, BigInteger.ZERO, sinMap, none);
                }
            }
        } else {
            HashMap<Poly, BigInteger> cosMap = new HashMap<>();
            cosMap.put(tripoly, exp);
            if (exp.equals(BigInteger.ZERO) || tripoly.isAllZero()) {
                unit = new Unit(BigInteger.ONE, BigInteger.ZERO, none, none);
            } else {
                unit = new Unit(BigInteger.ONE, BigInteger.ZERO, none, cosMap);
            }
        }
        unit.simplifyUnit();
        poly.addUnit(unit);
        return poly;
    }
}
