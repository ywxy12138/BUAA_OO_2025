import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;

public class Term {
    private HashMap<Integer, BigInteger> factors;

    public Term() {
        this.factors = new HashMap<>();
    }

    public void addFactor(HashMap<Integer, BigInteger> values) {
        HashMap<Integer, BigInteger> newFactors = new HashMap<>();
        Iterator<HashMap.Entry<Integer, BigInteger>> entry1 = values.entrySet().iterator();
        while (entry1.hasNext()) {
            HashMap.Entry<Integer, BigInteger> entryOne = entry1.next();
            Integer key1 = entryOne.getKey();
            BigInteger value1 = entryOne.getValue();
            Iterator<HashMap.Entry<Integer, BigInteger>> entry2 = factors.entrySet().iterator();
            while (entry2.hasNext()) {
                HashMap.Entry<Integer, BigInteger> entryTwo = entry2.next();
                Integer key2 = entryTwo.getKey();
                BigInteger value2 = entryTwo.getValue();
                Integer pow = key2 + key1;
                if (newFactors.containsKey(pow)) {
                    newFactors.put(pow, newFactors.get(pow).add(value1.multiply(value2)));
                }
                else {
                    newFactors.put(pow, value1.multiply(value2));
                }
            }
        }
        if (!newFactors.isEmpty()) {
            factors = newFactors;
        }
        else {
            factors = values;
        }
    }

    public void setFactors(Token token) {
        if (token.getType().equals(Token.Type.SUB)) {
            HashMap<Integer, BigInteger> newFactors = new HashMap<>();
            Iterator<HashMap.Entry<Integer, BigInteger>> entry = factors.entrySet().iterator();
            while (entry.hasNext()) {
                HashMap.Entry<Integer, BigInteger> temp = entry.next();
                newFactors.put(temp.getKey(), temp.getValue().multiply(BigInteger.valueOf(-1)));
            }
            factors = newFactors;
        }
    }

    public HashMap<Integer, BigInteger> getFactors() {
        return factors;
    }
}
