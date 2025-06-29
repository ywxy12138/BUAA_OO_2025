import java.math.BigInteger;
import java.util.HashMap;

public class Var implements Factor {
    private final HashMap<Integer, BigInteger> vars;

    public Var(Token token) {
        vars = new HashMap<>();
        Integer pow = Integer.parseInt(token.getValue());
        vars.put(pow, BigInteger.ONE);
    }

    @Override
    public HashMap<Integer, BigInteger> getFactor() {
        return vars;
    }
}
