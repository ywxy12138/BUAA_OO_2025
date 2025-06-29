import java.math.BigInteger;
import java.util.HashMap;

public class Const implements Factor {
    private final HashMap<Integer, BigInteger> nums;

    public Const(Token tokenOp, Token tokenNum) {
        nums = new HashMap<>();
        BigInteger num = new BigInteger(tokenNum.getValue());
        if (tokenOp.getType().equals(Token.Type.ADD)) {
            num = num.multiply(BigInteger.ONE);
        }
        else {
            num = num.multiply(BigInteger.valueOf(-1));
        }
        nums.put(0, num);
    }

    public HashMap<Integer, BigInteger> getFactor() {
        return nums;
    }
}
