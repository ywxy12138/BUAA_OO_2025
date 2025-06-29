import java.util.ArrayList;

public class Poly {
    private ArrayList<Unit> units;

    public Poly() {
        this.units = new ArrayList<>();
    }

    public ArrayList<Unit> getPolyList() {
        return units;
    }

    public Poly derive() {
        Poly poly = new Poly();
        for (Unit unit : units) {
            poly.addPoly(unit.derive());
        }
        return poly;
    }

    public void addPoly(Poly poly) {
        int len = poly.units.size();
        for (int i = 0; i < len; i++) {
            Unit unit = poly.units.get(i);
            addUnit(unit);
        }
    }

    public void addUnit(Unit unit) {
        ArrayList<Unit> temp = new ArrayList<>();
        int flag = 0;
        unit.simplifyUnit();
        for (Unit u : this.units) {
            int isEquals = u.myEquals(unit, 1);
            if (isEquals == 1 || isEquals == -1) {
                Unit hold = u.deepCopy().mergeUnit(unit.deepCopy(), isEquals);
                //此处进行深拷贝来代替在mergeUnit()函数中的深拷贝
                hold.simplifyUnit();
                flag = 1;
                temp.add(hold);
            }
            else {
                temp.add(u.deepCopy());
            }
        }
        if (temp.isEmpty() || flag == 0) {
            temp.add(unit.deepCopy());
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
                    Unit u3 = u1.mul(u2);
                    u3.simplifyUnit();
                    int flag = 0;
                    int len = temp.size();
                    for (int i = 0; i < len; i++) {
                        int isEquals = u3.myEquals(temp.get(i), 1);
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

    public boolean isAllZero() {
        int len = units.size();
        for (Unit u : units) {
            if (u.coeComToZero() != 0) {
                return false;
            }
        }
        return true;
    }

    public int myEquals(Poly poly, int mode) {
        if (units.size() != poly.units.size()) {
            return 0;
        }
        Poly ret = new Poly();
        ret.addPoly(this);
        ret.addPoly(poly);
        if (ret.isAllZero()) {
            return -1;
        }
        //全互为相反数
        int flag;
        int thisLen = units.size();
        int compareLen = poly.units.size();
        for (int i = 0; i < thisLen; i++) {
            Unit thisUnit = units.get(i);
            flag = 0;
            for (int j = 0; j < compareLen; j++) {
                Unit compareUnit = poly.units.get(j);
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