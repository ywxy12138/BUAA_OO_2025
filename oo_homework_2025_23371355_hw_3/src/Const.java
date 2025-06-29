import java.math.BigInteger;
import java.util.HashMap;

public class Const implements Factor {
    private BigInteger coe;

    public Const(Token tokenOp, Token tokenNum) {
        BigInteger num = new BigInteger(tokenNum.getValue());
        if (tokenOp.getType().equals(Token.Type.ADD)) {
            num = num.multiply(BigInteger.ONE);
        }
        else {
            num = num.multiply(BigInteger.valueOf(-1));
        }
        this.coe = num;
    }

    public Factor deepCopy() {
        Token op = new Token(Token.Type.ADD, "+");
        Token num = new Token(Token.Type.NUM, coe.toString());
        return new Const(op, num);
    }

    public Factor replaceFun(Factor repOne, Factor repTwo, String var1, String var2) {
        return deepCopy();
    }

    public Poly toPoly() {
        Poly poly = new Poly();
        HashMap<Poly, BigInteger> none = new HashMap<>();
        Unit unit = new Unit(this.coe,BigInteger.ZERO, none, none);
        poly.addUnit(unit);
        return poly;
    }
}
