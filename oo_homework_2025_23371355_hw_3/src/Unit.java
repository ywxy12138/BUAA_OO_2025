import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Unit {
    private BigInteger coe;
    private BigInteger exp;
    private HashMap<Poly, BigInteger> sinHashMap;
    private HashMap<Poly, BigInteger> cosHashMap;
    private final BigInteger minus = BigInteger.valueOf(-1);
    private final BigInteger one = BigInteger.ONE;
    private final BigInteger zero = BigInteger.ZERO;

    public Unit(BigInteger c, BigInteger e,
        HashMap<Poly, BigInteger> sp, HashMap<Poly, BigInteger> cp) {
        this.coe = c;
        this.exp = e;
        this.sinHashMap = sp;
        this.cosHashMap = cp;
    }

    public int coeComToZero() {
        return this.coe.compareTo(zero);
    }

    public Poly derive() { //对最小单元求导
        Poly poly = new Poly();
        poly.addUnit(new Unit(zero, zero, new HashMap<>(), new HashMap<>())); //对系数项求导
        if (exp.compareTo(zero) > 0) {
            Unit unit = deepCopy();
            unit.coe = unit.coe.multiply(exp);
            unit.exp = unit.exp.subtract(one);
            poly.addUnit(unit);
        } //对x项求导
        for (HashMap.Entry<Poly, BigInteger> entry1 : sinHashMap.entrySet()) {
            Unit unit = deepCopy();
            Poly realPoly1 = entry1.getKey();
            HashMap<Poly, BigInteger> newSinMap = new HashMap<>();
            for (HashMap.Entry<Poly, BigInteger> entry2 : sinHashMap.entrySet()) {
                if (entry2.getKey() != realPoly1) {
                    newSinMap.put(entry2.getKey().deepCopy(), entry2.getValue());
                }
            }
            if (!entry1.getValue().equals(one)) {
                newSinMap.put(realPoly1.deepCopy(), entry1.getValue().subtract(one));
            }
            unit.sinHashMap = newSinMap;
            unit.cosHashMap.put(realPoly1.deepCopy(), one);
            unit.coe = unit.coe.multiply(entry1.getValue());
            Poly sinPoly = new Poly();
            sinPoly.addUnit(unit);
            sinPoly.mulPoly(realPoly1.deepCopy().derive());
            poly.addPoly(sinPoly);
        } //对sin函数项进行求导
        for (HashMap.Entry<Poly, BigInteger> entry1 : cosHashMap.entrySet()) {
            Unit unit = deepCopy();
            Poly realPoly2 = entry1.getKey();
            HashMap<Poly, BigInteger> newCoeMap = new HashMap<>();
            for (HashMap.Entry<Poly, BigInteger> entry2 : cosHashMap.entrySet()) {
                if (entry2.getKey() != realPoly2) {
                    newCoeMap.put(entry2.getKey().deepCopy(), entry2.getValue());
                }
            }
            if (!entry1.getValue().equals(one)) {
                newCoeMap.put(realPoly2.deepCopy(), entry1.getValue().subtract(one));
            }
            unit.cosHashMap = newCoeMap;
            unit.sinHashMap.put(realPoly2.deepCopy(), one);
            unit.coe = unit.coe.multiply(entry1.getValue());
            unit.coe = unit.coe.multiply(minus);
            Poly cosPoly = new Poly();
            cosPoly.addUnit(unit);
            cosPoly.mulPoly(realPoly2.deepCopy().derive());
            poly.addPoly(cosPoly);
        } //对cos函数项进行求导
        return poly;
    }

    public Unit deepCopy() {
        BigInteger newCoe = new BigInteger(coe.toString());
        BigInteger newExp = exp;
        HashMap<Poly, BigInteger> newSinMap = new HashMap<>();
        HashMap<Poly, BigInteger> newCosMap = new HashMap<>();
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

    public Unit mergeUnit(Unit unit, int comRes) {
        Unit hold = null;
        if (comRes == -1) {
            if (this.coe.subtract(unit.coe).compareTo(zero) > 0) {
                unit.coe = unit.coe.multiply(minus);
                hold = this.add(unit);
            }
            else {
                this.coe = this.coe.multiply(minus);
                hold = unit.add(this);
            }
        }
        else {
            hold = unit.add(this);
        }
        return hold;
    }

    public boolean isSinFact() {
        if (coe.equals(zero)) {
            return true;
        }
        if (exp.equals(zero) && sinHashMap.isEmpty() && cosHashMap.isEmpty()) {
            return true;
        }
        if (coe.equals(one) && sinHashMap.isEmpty() && cosHashMap.isEmpty()) {
            return true;
        }
        if (coe.equals(one) && exp.equals(zero) &&
            cosHashMap.isEmpty() && sinHashMap.size() == 1) {
            return true;
        }
        if (coe.equals(one) && exp.equals(zero) &&
            sinHashMap.isEmpty() && cosHashMap.size() == 1) {
            return true;
        }
        return false;
    }

    public void simplifySinMap() {
        Iterator<HashMap.Entry<Poly, BigInteger>> sinIt = sinHashMap.entrySet().iterator();
        HashMap<Poly, BigInteger> temp = new HashMap<>();
        while (sinIt.hasNext()) {
            HashMap.Entry<Poly, BigInteger> entry = sinIt.next();
            Poly poly = entry.getKey();
            BigInteger exp = entry.getValue();
            ArrayList<Unit> units = poly.getPolyList();
            if (units.size() == 1 && units.get(0).coeComToZero() == 0) {
                this.coe = zero;
                break;
            } //判断是否会出现sin(0)的情况
            int flag = 1;
            for (Unit unit : units) {
                if (unit.coeComToZero() > 0) {
                    flag = 0;
                }
            } //要是sin括号内因子都为负，可以提一个负号出去
            if (flag == 1) {
                poly.setSign(Token.Type.SUB);
                if (exp.mod(BigInteger.valueOf(2)).equals(one)) {
                    this.coe = this.coe.multiply(minus);
                }
            }
            temp.put(poly, exp);
        }
        sinHashMap = temp;
    }

    public void simplifyCosMap() {
        Iterator<HashMap.Entry<Poly, BigInteger>> cosIt = cosHashMap.entrySet().iterator();
        HashMap<Poly, BigInteger> temp = new HashMap<>();
        while (cosIt.hasNext()) {
            HashMap.Entry<Poly, BigInteger> entry = cosIt.next();
            Poly poly = entry.getKey();
            ArrayList<Unit> units = poly.getPolyList();
            if (units.size() == 1) {
                Unit unit = units.get(0);
                if (unit.coeComToZero() == 0) {
                    continue;
                }
            }
            int flag = 1;
            for (Unit unit : units) {
                if (unit.coeComToZero() > 0) {
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

    public HashMap<Poly, BigInteger> simplifyMap(HashMap<Poly, BigInteger> map, Token.Type type) {
        ArrayList<HashMap.Entry<Poly, BigInteger>> list = new ArrayList<>();
        for (HashMap.Entry<Poly, BigInteger> entry : map.entrySet()) {
            list.add(entry);
        }
        int len = list.size();
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                HashMap.Entry<Poly, BigInteger> entryI = list.get(i);
                HashMap.Entry<Poly, BigInteger> entryJ = list.get(j);
                BigInteger valueI = entryI.getValue();
                BigInteger valueJ = entryJ.getValue();
                if (!valueI.equals(minus) && !valueJ.equals(minus) && i != j) {
                    Poly factorI = entryI.getKey();
                    Poly factorJ = entryJ.getKey();
                    int isEquals = factorI.myEquals(factorJ, 0);
                    if (isEquals == -1) {
                        if (type.equals(Token.Type.SIN)) {
                            if (valueJ.mod(BigInteger.valueOf(2)).equals(one)) {
                                entryI.setValue(valueI.add(valueJ));
                                entryJ.setValue(minus);
                                this.coe = this.coe.multiply(minus);
                            }
                            else {
                                entryJ.setValue(valueI.add(valueJ));
                                entryI.setValue(minus);
                                BigInteger temp = (valueI.mod(BigInteger.valueOf(2)).equals(one)) ?
                                    minus : one;
                                this.coe = this.coe.multiply(temp);
                            }
                        }
                        else {
                            entryI.setValue(valueI.add(valueJ));
                            entryJ.setValue(minus);
                        }
                    }
                    else if (isEquals == 1) {
                        entryI.setValue(valueI.add(valueJ));
                        entryJ.setValue(minus);
                    }
                    list.set(i, entryI);
                    list.set(j, entryJ);
                }
            }
        }
        HashMap<Poly, BigInteger> newHashMap = new HashMap<>();
        for (int i = 0; i < len; i++) {
            if (!list.get(i).getValue().equals(minus)) {
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
            Poly realSinPoly = null;
            Poly realCosPoly = null;
            if (!sinHashMap.isEmpty() && !cosHashMap.isEmpty()) {
                Iterator<HashMap.Entry<Poly, BigInteger>> sinIt = sinHashMap.entrySet().iterator();
                while (sinIt.hasNext()) {
                    HashMap.Entry<Poly, BigInteger> entry = sinIt.next();
                    if (entry.getValue().equals(one)) {
                        realSinPoly = entry.getKey(); //方便后续删除
                        break;
                    }
                }
                Iterator<HashMap.Entry<Poly, BigInteger>> cosIt = cosHashMap.entrySet().iterator();
                while (cosIt.hasNext()) {
                    HashMap.Entry<Poly, BigInteger> entry = cosIt.next();
                    if (entry.getValue().equals(one)) {
                        realCosPoly = entry.getKey();
                        break;
                    }
                }
                if (coe.mod(BigInteger.valueOf(2)).equals(zero)
                    && realSinPoly != null && realCosPoly != null) {
                    int comResult = realSinPoly.myEquals(realCosPoly, 0);
                    if (comResult != 0) {
                        Const num = new Const(new Token(Token.Type.ADD, "+"),
                            new Token(Token.Type.NUM, BigInteger.valueOf(2).toString()));
                        sinHashMap.remove(realSinPoly);
                        cosHashMap.remove(realCosPoly);
                        realSinPoly.mulPoly(num.toPoly());
                        sinHashMap.put(realSinPoly, one);
                        coe = coe.divide(new BigInteger("2"));
                        //sin和cos内因子互为相反数也是以sin内因子为准，对于偶函数cos来说，这些都不是事
                    }
                }
            }
        }
    }

    public Unit add(Unit unit) {
        return new Unit(coe.add(unit.coe), exp, sinHashMap, cosHashMap);
    }

    public Unit mul(Unit unit) {
        HashMap<Poly, BigInteger> newSinMap = new HashMap<>();
        HashMap<Poly, BigInteger> newCosMap = new HashMap<>();
        HashMap<Poly, BigInteger> sinMap1 = this.sinHashMap;
        HashMap<Poly, BigInteger> cosMap1 = this.cosHashMap;
        HashMap<Poly, BigInteger> sinMap2 = unit.sinHashMap;
        HashMap<Poly, BigInteger> cosMap2 = unit.cosHashMap;
        newSinMap = mulMap(sinMap1, sinMap2);
        newCosMap = mulMap(cosMap1, cosMap2);
        BigInteger newCoe = this.coe.multiply(unit.coe);
        BigInteger newExp = this.exp.add(unit.exp);
        Unit ans = new Unit(newCoe, newExp, newSinMap, newCosMap);
        return ans;
    }

    public String mapToString(String ans, HashMap<Poly, BigInteger> map, Token.Type type) {
        String newAns = "";
        newAns += ans;
        if (!map.isEmpty()) {
            Iterator<HashMap.Entry<Poly, BigInteger>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry<Poly, BigInteger> entry = it.next();
                int len = newAns.length();
                if (!newAns.isEmpty() && newAns.charAt(len - 1) == '1') {
                    if (len == 1 ||
                        (len >= 2 && (!Character.isDigit(newAns.charAt(len - 2))))) {
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
                BigInteger exp = entry.getValue();
                newAns += (poly.buildString(1) + ")");
                if (!exp.equals(one)) {
                    newAns += ("^" + exp);
                }
            }
        }
        return newAns;
    }

    public String buildString() {
        String ans = "";
        if (!coe.equals(zero)) {
            if (exp.equals(zero) && sinHashMap.isEmpty() && cosHashMap.isEmpty()) {
                return coe.toString();
            }
            else {
                if (coe.equals(minus)) {
                    ans += "-";
                }
                else {
                    if (!coe.equals(one)) {
                        ans += (coe);
                    }
                }
            }
            if (exp.compareTo(zero) > 0) {
                if (exp.equals(one)) {
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

    public HashMap<Poly, BigInteger> mulMap(
        HashMap<Poly, BigInteger> map1, HashMap<Poly, BigInteger> map2) {
        HashMap<Poly, BigInteger> newMap = new HashMap<>();
        if (map1.isEmpty()) {
            return map2;
        }
        else {
            if (map2.isEmpty()) {
                return map1;
            }
            else {
                Iterator<HashMap.Entry<Poly, BigInteger>> it1 = map1.entrySet().iterator();
                while (it1.hasNext()) {
                    HashMap.Entry<Poly, BigInteger> entry1 = it1.next();
                    Poly realPoly1 = entry1.getKey();
                    BigInteger value1 = entry1.getValue();
                    Iterator<HashMap.Entry<Poly, BigInteger>> it2 = map2.entrySet().iterator();
                    int flag = 0;
                    while (it2.hasNext()) {
                        HashMap.Entry<Poly, BigInteger> entry2 = it2.next();
                        BigInteger value2 = entry2.getValue();
                        if (realPoly1.myEquals(entry2.getKey(), 0) == 1) {
                            newMap.put(realPoly1, value1.add(value2));
                            flag = 1;
                        }
                    }
                    if (flag == 0) {
                        newMap.put(realPoly1, value1);
                    }
                }
                Iterator<HashMap.Entry<Poly, BigInteger>> it2 = map2.entrySet().iterator();
                while (it2.hasNext()) {
                    HashMap.Entry<Poly, BigInteger> entry2 = it2.next();
                    Iterator<HashMap.Entry<Poly, BigInteger>> iter1 = map1.entrySet().iterator();
                    int flag = 0;
                    Poly realPoly2 = entry2.getKey();
                    while (iter1.hasNext()) {
                        HashMap.Entry<Poly, BigInteger> entry1 = iter1.next();
                        if (entry1.getKey().myEquals(realPoly2, 0) == 1) {
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

    public int judge(HashMap<Poly, BigInteger> map1,
        HashMap<Poly, BigInteger> map2, int mode) {
        Iterator<HashMap.Entry<Poly, BigInteger>> iterator1 = map1.entrySet().iterator();
        int equalSign = 1;
        while (iterator1.hasNext()) {
            HashMap.Entry<Poly, BigInteger> entry1 = iterator1.next();
            Poly poly1 = entry1.getKey();
            BigInteger exp1 = entry1.getValue();
            Iterator<HashMap.Entry<Poly, BigInteger>> iterator2 = map2.entrySet().iterator();
            int flag = 0;
            while (iterator2.hasNext()) {
                HashMap.Entry<Poly, BigInteger> entry2 = iterator2.next();
                Poly poly2 = entry2.getKey();
                //进行myEquals比较时不会改变比较的双方，因此此处不需要进行深拷贝
                BigInteger exp2 = entry2.getValue();
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
        HashMap<Poly, BigInteger> sinHashMap1 = this.sinHashMap;
        HashMap<Poly, BigInteger> cosHashMap1 = this.cosHashMap;
        HashMap<Poly, BigInteger> sinHashMap2 = unit.sinHashMap;
        HashMap<Poly, BigInteger> cosHashMap2 = unit.cosHashMap;
        if (sinHashMap1.size() != sinHashMap2.size()) {
            return 0;
        }
        if (cosHashMap1.size() != cosHashMap2.size()) {
            return 0;
        }
        int temp = 0;
        if (exp.equals(unit.exp)) {
            temp = judge(sinHashMap1, sinHashMap2, 0)
                            * judge(cosHashMap1, cosHashMap2, 0);
        }
        if (mode == 0) {
            int isEquals = coe.equals(unit.coe) ? 1 :
                (coe.add(unit.coe).equals(zero) ? -1 : 0);
            return (isEquals * temp);
        }
        return temp;
    }

    public void setSign(Token.Type type) {
        coe = (type.equals(Token.Type.SUB)) ? coe.multiply(minus) : coe;
    }
}
