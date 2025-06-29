import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;

public class Expr implements Factor {
    private final HashMap<Integer, BigInteger> terms;

    public Expr() {
        terms = new HashMap<>();
    }

    public void addTerm(HashMap<Integer, BigInteger> factors, Token op) {
        Iterator<HashMap.Entry<Integer,BigInteger>> iter = factors.entrySet().iterator();
        while (iter.hasNext()) {
            HashMap.Entry<Integer,BigInteger> entry = iter.next();
            BigInteger val = entry.getValue();
            if (terms.containsKey(entry.getKey())) {
                if (op.getType().equals(Token.Type.ADD)) {
                    val = terms.get(entry.getKey()).add(entry.getValue());
                } else {
                    val = terms.get(entry.getKey()).subtract(entry.getValue());
                }
            }
            else {
                if (op.getType().equals(Token.Type.SUB)) {
                    val = val.multiply(BigInteger.valueOf(-1));
                }
            }
            terms.put(entry.getKey(), val);
        }
    }

    public Integer printLg(HashMap.Entry<Integer, BigInteger> entry) {
        if (entry.getKey() == 0) {
            System.out.printf("%s", entry.getValue());
        }
        else {
            if (!entry.getValue().equals(BigInteger.ONE)) {
                System.out.printf("%s*", entry.getValue());
            }
            System.out.printf("x");
            if (entry.getKey() != 1) {
                System.out.printf("^%s", entry.getKey());
            }
        }
        return entry.getKey();
    }

    public void print(HashMap<Integer, BigInteger> terms) {
        Iterator<HashMap.Entry<Integer, BigInteger>> iter = terms.entrySet().iterator();
        Integer first = -1;
        int isPrint = 0;
        while (iter.hasNext()) {
            HashMap.Entry<Integer,BigInteger> entry = iter.next();
            if (entry.getValue().compareTo(BigInteger.ZERO) > 0) {
                first = printLg(entry);
                isPrint = 1;
                break;
            }
        }
        Iterator<HashMap.Entry<Integer, BigInteger>> iter2 = terms.entrySet().iterator();
        while (iter2.hasNext()) {
            HashMap.Entry<Integer,BigInteger> entry2 = iter2.next();
            if (entry2.getKey() != first) {
                if (entry2.getValue().compareTo(BigInteger.ZERO) > 0) {
                    System.out.printf("+");
                    Integer pow = printLg(entry2);
                    isPrint = 1;
                }
                else if (entry2.getValue().compareTo(BigInteger.ZERO) < 0) {
                    if (entry2.getKey() == 0) {
                        System.out.printf("%s", entry2.getValue());
                    }
                    else {
                        if (entry2.getValue().compareTo(BigInteger.valueOf(-1)) != 0) {
                            System.out.printf("%s*", entry2.getValue());
                        } else {
                            System.out.printf("-");
                        }
                        System.out.printf("x");
                        if (entry2.getKey() != 1) {
                            System.out.printf("^%s", entry2.getKey());
                        }
                    }
                    isPrint = 1;
                }
            }
        }
        if (isPrint == 0) {
            System.out.printf("0");
        }
    }

    @Override
    public HashMap<Integer, BigInteger> getFactor() {
        return terms;
    }
}
