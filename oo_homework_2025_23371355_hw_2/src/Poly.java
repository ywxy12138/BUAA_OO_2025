import java.util.ArrayList;

public class Poly {
    private ArrayList<Unit> units;

    public Poly() {
        this.units = new ArrayList<>();
    }

    public ArrayList<Unit> getPolyList() {
        return units;
    }

    public void addPoly(Poly poly) {
        int len = poly.units.size();
        for (int i = 0; i < len; i++) {
            Unit unit = poly.units.get(i);
            unit.simplifyUnit();
            addUnit(unit);
        }
    }

    public void addUnit(Unit unit) {
        ArrayList<Unit> temp = new ArrayList<>();
        int flag = 0;
        unit.simplifyUnit();
        for (Unit u : this.units) {
            u.simplifyUnit();
            int isEquals = u.deepCopy().myEquals(unit.deepCopy(), 1);
            if (isEquals == 1 || isEquals == -1) {
                Unit hold = u.deepCopy().mergeUnit(unit.deepCopy(), isEquals);
                hold.simplifyUnit();
                flag = 1;
                temp.add(hold);
            }
            else {
                temp.add(u);
            }
        }
        if (temp.isEmpty() || flag == 0) {
            temp.add(unit);
        }
        this.units = temp;
    }

    public void mulPoly(Poly poly) {
        ArrayList<Unit> polyList = poly.units;
        if (units.isEmpty()) {
            units = polyList;
        } else {
            ArrayList<Unit> temp = new ArrayList<>();
            for (Unit u1 : units) {
                for (Unit u2 : polyList) {
                    u1.simplifyUnit();
                    u2.simplifyUnit();
                    Unit u3 = u1.deepCopy().mul(u2.deepCopy(), 1);
                    u3.simplifyUnit();
                    int flag = 0;
                    int len = temp.size();
                    for (int i = 0; i < len; i++) {
                        int isEquals = u3.deepCopy().myEquals(temp.get(i).deepCopy(), 1);
                        if (isEquals == 1 || isEquals == -1) {
                            Unit hold = u3.deepCopy().mergeUnit(temp.get(i).deepCopy(), isEquals);
                            hold.simplifyUnit();
                            temp.set(i, hold);
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0) {
                        temp.add(u3);
                    }
                }
            }
            units = temp;
        }
    }

    public String buildString(int mode) {
        //mode代表此时该多项式所处的状态，假如是在三角函数中则为状态1，需要考虑是否要加括号，否则不用考虑加括号的问题
        String ans = "";
        Unit unit = null;
        if (mode == 1 && (units.size() >= 2
            || (units.size() == 1 && !units.get(0).isSinFact()))) {
            ans += "(";
        }
        for (Unit u : units) {
            if (u.coeComToZero() > 0) {
                unit = u;
                ans += u.buildString();
                break;
            }
        }
        for (Unit u : units) {
            if (u != unit) {
                if (u.coeComToZero() > 0) {
                    ans += "+";
                }
                ans += u.buildString();
            }
        }
        if (mode == 1 && (units.size() >= 2
            || (units.size() == 1 && !units.get(0).isSinFact()))) {
            ans += ")";
        }
        if (ans.isEmpty() || ans.equals("()")) {
            return "0";
        }
        else {
            return ans;
        }
    }

    public void setSign(Token.Type sign) {
        if (sign.equals(Token.Type.SUB)) {
            for (Unit u : this.units) {
                u.setSign(sign);
            }
        }
    }

    public Poly deepCopy() {
        Poly poly = new Poly();
        for (Unit u : this.units) {
            poly.units.add(u.deepCopy());
        }
        return poly;
    }

    public int myEquals(Poly poly, int mode) {
        Poly thisPoly = this.deepCopy();
        Poly comparePoly = poly.deepCopy();
        ArrayList<Unit> thisUnits = thisPoly.units;
        ArrayList<Unit> compareUnits = comparePoly.units;
        if (thisUnits.size() != compareUnits.size()) {
            return 0;
        }
        Poly ret = new Poly();
        ret.addPoly(thisPoly);
        ret.addPoly(comparePoly);
        int len = ret.units.size();
        int flag = 1;
        for (int i = 0; i < len; i++) {
            if (ret.units.get(i).coeComToZero() != 0) {
                flag = 0;
                break;
            }
        }
        if (flag == 1) {
            return -1;
        }
        int thisLen = thisUnits.size();
        int compareLen = compareUnits.size();
        for (int i = 0; i < thisLen; i++) {
            Unit thisUnit = thisUnits.get(i).deepCopy();
            flag = 0;
            for (int j = 0; j < compareLen; j++) {
                Unit compareUnit = compareUnits.get(j).deepCopy();
                if (thisUnit.myEquals(compareUnit, mode) == 1) {
                    flag = 1;
                    break;
                }
            }
            if (flag == 0) {
                return 0;
            }
        }
        return 1;
    }
}