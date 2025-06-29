import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Unit {
    private BigInteger coe;
    private Integer exp;
    private HashMap<Poly, Integer> sinHashMap;
    private HashMap<Poly, Integer> cosHashMap;

    public Unit(BigInteger c, Integer e,
        HashMap<Poly, Integer> sp, HashMap<Poly, Integer> cp) {
        this.coe = c;
        this.exp = e;
        this.sinHashMap = sp;
        this.cosHashMap = cp;
    }

    public int coeComToZero() {
        return coe.compareTo(BigInteger.ZERO);
    }

    public Unit deepCopy() {
        BigInteger newCoe = new BigInteger(coe.toString());
        Integer newExp = exp;
        HashMap<Poly, Integer> newSinMap = new HashMap<>();
        HashMap<Poly, Integer> newCosMap = new HashMap<>();
        if (!sinHashMap.isEmpty()) {
            for (Poly poly : sinHashMap.keySet()) {
                if (poly != null) {
                    newSinMap.put(poly.deepCopy(), sinHashMap.get(poly));
                }
            }
        }
        if (!cosHashMap.isEmpty()) {
            for (Poly poly : cosHashMap.keySet()) {
                if (poly != null) {
                    newCosMap.put(poly.deepCopy(), cosHashMap.get(poly));
                }
            }
        }
        return new Unit(newCoe, newExp, newSinMap, newCosMap);
    }

    public boolean isSinFact() {
        if (coe.equals(BigInteger.ZERO)) {
            return true;
        }
        if (exp == 0 && sinHashMap.isEmpty() && cosHashMap.isEmpty()) {
            return true;
        }
        if (coe.equals(BigInteger.ONE) && sinHashMap.isEmpty() && cosHashMap.isEmpty()) {
            return true;
        }
        if (coe.equals(BigInteger.ONE) && exp == 0 &&
            cosHashMap.isEmpty() && sinHashMap.size() == 1) {
            return true;
        }
        if (coe.equals(BigInteger.ONE) && exp == 0 &&
            sinHashMap.isEmpty() && cosHashMap.size() == 1) {
            return true;
        }
        return false;
    }

    public void simplifySinMap() {
        Iterator<HashMap.Entry<Poly, Integer>> sinIt = sinHashMap.entrySet().iterator();
        HashMap<Poly, Integer> temp = new HashMap<>();
        while (sinIt.hasNext()) {
            HashMap.Entry<Poly, Integer> entry = sinIt.next();
            Poly poly = entry.getKey();
            Integer exp = entry.getValue();
            ArrayList<Unit> units = poly.getPolyList();
            if (units.size() == 1 && units.get(0).coe.equals(BigInteger.ZERO)) {
                this.coe = BigInteger.ZERO;
                break;
            } //判断是否会出现sin(0)的情况
            int flag = 1;
            for (Unit unit : units) {
                if (unit.coe.compareTo(BigInteger.ZERO) > 0) {
                    flag = 0;
                }
            } //要是sin括号内因子都为负，可以提一个负号出去
            if (flag == 1) {
                poly.setSign(Token.Type.SUB);
                if (exp % 2 == 1) {
                    this.coe = this.coe.multiply(BigInteger.valueOf(-1));
                }
            }
            temp.put(poly, exp);
        }
        sinHashMap = temp;
    }

    public void simplifyCosMap() {
        Iterator<HashMap.Entry<Poly, Integer>> cosIt = cosHashMap.entrySet().iterator();
        HashMap<Poly, Integer> temp = new HashMap<>();
        while (cosIt.hasNext()) {
            HashMap.Entry<Poly, Integer> entry = cosIt.next();
            Poly poly = entry.getKey();
            ArrayList<Unit> units = poly.getPolyList();
            if (units.size() == 1) {
                Unit unit = units.get(0);
                if (unit.coe.equals(BigInteger.ZERO)) {
                    continue;
                }
            }
            int flag = 1;
            for (Unit unit : units) {
                if (unit.coe.compareTo(BigInteger.ZERO) > 0) {
                    flag = 0;
                }
            } //判断cos括号内是否全为负号
            if (flag == 1) {
                poly.setSign(Token.Type.SUB);
            }
            temp.put(poly, entry.getValue());
        }
        cosHashMap = temp;
    }

    public HashMap<Poly, Integer> simplifyMap(HashMap<Poly, Integer> map, Token.Type type) {
        ArrayList<HashMap.Entry<Poly, Integer>> list = new ArrayList<>();
        for (HashMap.Entry<Poly, Integer> entry : map.entrySet()) {
            list.add(entry);
        }
        int len = list.size();
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                HashMap.Entry<Poly, Integer> entryI = list.get(i);
                HashMap.Entry<Poly, Integer> entryJ = list.get(j);
                Integer valueI = entryI.getValue();
                Integer valueJ = entryJ.getValue();
                if (valueI != -1 && valueJ != -1) {
                    Poly factorI = entryI.getKey().deepCopy();
                    Poly factorJ = entryJ.getKey().deepCopy();
                    if (factorI.myEquals(factorJ, 0) == -1) {
                        if (type.equals(Token.Type.SIN)) {
                            if (valueJ % 2 == 1) {
                                entryI.setValue(valueI + valueJ);
                                entryJ.setValue(-1);
                                coe = coe.multiply(BigInteger.valueOf(-1));
                            }
                            else {
                                entryJ.setValue(valueI + valueJ);
                                entryI.setValue(-1);
                                BigInteger temp = (valueI % 2 == 1) ?
                                    BigInteger.valueOf(-1) : BigInteger.ONE;
                                coe = coe.multiply(temp);
                            }
                        }
                        else {
                            entryI.setValue(valueI + valueJ);
                            entryJ.setValue(-1);
                        }
                        list.set(i, entryI);
                        list.set(j, entryJ);
                    }
                }
            }
        }
        HashMap<Poly, Integer> newHashMap = new HashMap<>();
        for (int i = 0; i < len; i++) {
            if (list.get(i).getValue() != -1) {
                newHashMap.put(list.get(i).getKey(), list.get(i).getValue());
            }
        }
        return newHashMap;
    }

    public void simplifyUnit() {
        if (!sinHashMap.isEmpty() || !cosHashMap.isEmpty()) {
            simplifySinMap();
            simplifyCosMap();
            sinHashMap = simplifyMap(sinHashMap, Token.Type.SIN);
            cosHashMap = simplifyMap(cosHashMap, Token.Type.COS);
            Poly sinPoly = null;
            Poly realSinPoly = null;
            Poly cosPoly = null;
            Poly realCosPoly = null;
            if (!sinHashMap.isEmpty() && !cosHashMap.isEmpty()) {
                Iterator<HashMap.Entry<Poly, Integer>> sinIt = sinHashMap.entrySet().iterator();
                while (sinIt.hasNext()) {
                    HashMap.Entry<Poly, Integer> entry = sinIt.next();
                    if (entry.getValue() == 1) {
                        sinPoly = entry.getKey().deepCopy();
                        realSinPoly = entry.getKey(); //方便后续删除
                        break;
                    }
                }
                Iterator<HashMap.Entry<Poly, Integer>> cosIt = cosHashMap.entrySet().iterator();
                while (cosIt.hasNext()) {
                    HashMap.Entry<Poly, Integer> entry = cosIt.next();
                    if (entry.getValue() == 1) {
                        cosPoly = entry.getKey().deepCopy();
                        realCosPoly = entry.getKey();
                        break;
                    }
                }
                if (coe.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)
                    && sinPoly != null && cosPoly != null) {
                    int comResult = sinPoly.myEquals(cosPoly, 0);
                    if (comResult != 0) {
                        Const num = new Const(new Token(Token.Type.ADD, "+"),
                            new Token(Token.Type.NUM, BigInteger.valueOf(2).toString()));
                        sinPoly.mulPoly(num.toPoly());
                        sinHashMap.remove(realSinPoly);
                        cosHashMap.remove(realCosPoly);
                        sinHashMap.put(sinPoly, 1);
                        coe = (comResult == 1) ? coe.divide(new BigInteger("2"))
                                : coe.divide(new BigInteger("-2"));
                    }
                }
            }
        }
    }

    public Unit add(Unit unit) {
        Unit ans =
            new Unit(this.coe.add(unit.coe), this.exp, this.sinHashMap, this.cosHashMap);
        return ans;
    }

    public Unit mul(Unit unit, int mode) {
        HashMap<Poly, Integer> newSinMap = new HashMap<>();
        HashMap<Poly, Integer> newCosMap = new HashMap<>();
        HashMap<Poly, Integer> sinMap1 = this.sinHashMap;
        HashMap<Poly, Integer> cosMap1 = this.cosHashMap;
        HashMap<Poly, Integer> sinMap2 = unit.sinHashMap;
        HashMap<Poly, Integer> cosMap2 = unit.cosHashMap;
        newSinMap = mulMap(sinMap1, sinMap2, mode);
        newCosMap = mulMap(cosMap1, cosMap2, mode);
        BigInteger newCoe = this.coe.multiply(unit.coe);
        Integer newExp = this.exp + unit.exp;
        Unit ans = new Unit(newCoe, newExp, newSinMap, newCosMap);
        return ans;
    }

    public Unit mergeUnit(Unit unit, int comRes) {
        Unit hold = null;
        Unit unit1 = unit.deepCopy();
        Unit unit2 = deepCopy();
        if (comRes == -1) {
            if (unit1.coe.subtract(unit2.coe).compareTo(BigInteger.ZERO) > 0) {
                unit2.coe = unit2.coe.multiply(BigInteger.valueOf(-1));
                hold = unit1.add(unit2);
            }
            else {
                unit1.coe = unit1.coe.multiply(BigInteger.valueOf(-1));
                hold = unit2.add(unit1);
            }
        }
        else {
            hold = unit2.add(unit1);
        }
        return hold;
    }

    public String mapToString(String ans, HashMap<Poly, Integer> map, Token.Type type) {
        String newAns = "";
        newAns += ans;
        if (!map.isEmpty()) {
            Iterator<HashMap.Entry<Poly, Integer>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry<Poly, Integer> entry = it.next();
                int len = newAns.length();
                if (!newAns.isEmpty() && newAns.charAt(len - 1) == '1') {
                    if (len == 1 ||
                        (len >= 2 &&
                        (newAns.charAt(len - 2) < '0' || newAns.charAt(len - 2) > '9'))) {
                        newAns = newAns.substring(0, newAns.length() - 1);
                    } //只有前面的系数为1的时候才可以省略
                    else {
                        newAns += "*"; //如果是最后一个字符是1但实际上是多位的数字，那么还是不可省略，加上乘号表示相乘
                    }
                }
                else if (!newAns.isEmpty() && !newAns.equals("-")) {
                    newAns += "*";
                }
                newAns += (type.equals(Token.Type.SIN) ? "sin(" : "cos(");
                Poly poly = entry.getKey();
                Integer exp = entry.getValue();
                newAns += (poly.buildString(1) + ")");
                if (exp != 1) {
                    newAns += ("^" + exp);
                }
            }
        }
        return newAns;
    }

    public String buildString() {
        String ans = "";
        if (!coe.equals(BigInteger.ZERO)) {
            if (exp == 0 && sinHashMap.isEmpty() && cosHashMap.isEmpty()) {
                return coe.toString();
            }
            else {
                if (coe.equals(BigInteger.valueOf(-1))) {
                    ans += "-";
                }
                else {
                    if (!coe.equals(BigInteger.ONE)) {
                        ans += (coe);
                    }
                }
            }
            if (exp > 0) {
                if (exp == 1) {
                    if (!ans.isEmpty() && !ans.equals("-")) {
                        ans += "*x";
                    }
                    else {
                        ans += "x";
                    }
                }
                else {
                    if (!ans.isEmpty() && !ans.equals("-")) {
                        ans += ("*x^" + exp);
                    }
                    else {
                        ans += ("x^" + exp);
                    }
                }
            }
            ans = mapToString(ans, sinHashMap, Token.Type.SIN);
            ans = mapToString(ans, cosHashMap, Token.Type.COS);
        }
        return ans;
    }

    public HashMap<Poly, Integer> mulMap(
        HashMap<Poly, Integer> map1, HashMap<Poly, Integer> map2, int mode) {
        HashMap<Poly, Integer> newMap = new HashMap<>();
        if (map1.isEmpty()) {
            return map2;
        }
        else {
            if (map2.isEmpty()) {
                return map1;
            }
            else {
                Iterator<HashMap.Entry<Poly, Integer>> it1 = map1.entrySet().iterator();
                while (it1.hasNext()) {
                    HashMap.Entry<Poly, Integer> entry1 = it1.next();
                    Poly poly1 = entry1.getKey().deepCopy();
                    Poly realPoly1 = entry1.getKey();
                    Integer value1 = entry1.getValue();
                    Iterator<HashMap.Entry<Poly, Integer>> it2 = map2.entrySet().iterator();
                    int flag = 0;
                    while (it2.hasNext()) {
                        HashMap.Entry<Poly, Integer> entry2 = it2.next();
                        Poly poly2 = entry2.getKey().deepCopy();
                        Integer value2 = entry2.getValue();
                        if (poly1.myEquals(poly2, 0) == 1) {
                            newMap.put(realPoly1, value1 + value2);
                            flag = 1;
                        }
                    }
                    if (flag == 0) {
                        newMap.put(realPoly1, value1);
                    }
                }
                Iterator<HashMap.Entry<Poly, Integer>> it2 = map2.entrySet().iterator();
                while (it2.hasNext()) {
                    HashMap.Entry<Poly, Integer> entry2 = it2.next();
                    Iterator<HashMap.Entry<Poly, Integer>> iter1 = map1.entrySet().iterator();
                    int flag = 0;
                    Poly realPoly2 = entry2.getKey();
                    while (iter1.hasNext()) {
                        HashMap.Entry<Poly, Integer> entry1 = iter1.next();
                        Poly poly1 = entry1.getKey().deepCopy();
                        Poly poly2 = entry2.getKey().deepCopy();
                        if (poly1.myEquals(poly2, 0) == 1) {
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0) {
                        newMap.put(realPoly2, entry2.getValue());
                    }
                }
                return newMap;
            }
        }
    }

    public int judge(HashMap<Poly, Integer> map1,
        HashMap<Poly, Integer> map2, int mode) {
        Iterator<HashMap.Entry<Poly, Integer>> iterator1 = map1.entrySet().iterator();
        int equalSign = 1;
        while (iterator1.hasNext()) {
            HashMap.Entry<Poly, Integer> entry1 = iterator1.next();
            Poly poly1 = entry1.getKey().deepCopy();
            Integer exp1 = entry1.getValue();
            Iterator<HashMap.Entry<Poly, Integer>> iterator2 = map2.entrySet().iterator();
            int flag = 0;
            while (iterator2.hasNext()) {
                HashMap.Entry<Poly, Integer> entry2 = iterator2.next();
                Poly poly2 = entry2.getKey().deepCopy();
                Integer exp2 = entry2.getValue();
                int isEquals = poly1.myEquals(poly2, mode);
                if ((isEquals == 1 || isEquals == -1) && exp1.equals(exp2)) {
                    flag = 1;
                    equalSign *= isEquals;
                    break;
                }
            }
            if (flag == 0) {
                return 0;
            }
        }
        return equalSign;
    }

    public int myEquals(Unit unit, int mode) {
        HashMap<Poly, Integer> sinHashMap1 = this.sinHashMap;
        HashMap<Poly, Integer> cosHashMap1 = this.cosHashMap;
        HashMap<Poly, Integer> sinHashMap2 = unit.sinHashMap;
        HashMap<Poly, Integer> cosHashMap2 = unit.cosHashMap;
        if (sinHashMap1.size() != sinHashMap2.size()) {
            return 0;
        }
        if (cosHashMap1.size() != cosHashMap2.size()) {
            return 0;
        }
        int temp;
        if (exp.equals(unit.exp)) {
            temp =
                    judge(sinHashMap1, sinHashMap2, 0)
                            * judge(cosHashMap1, cosHashMap2, 0);
        }
        else {
            return 0;
        }
        if (mode == 0) {
            int isEquals = coe.equals(unit.coe) ? 1 :
                (coe.add(unit.coe).equals(BigInteger.ZERO) ? -1 : 0);
            return (isEquals * temp);
        }
        return temp;
    }

    public void setSign(Token.Type type) {
        if (type.equals(Token.Type.SUB)) {
            this.coe = this.coe.multiply(BigInteger.valueOf(-1));
        }
    }
}
