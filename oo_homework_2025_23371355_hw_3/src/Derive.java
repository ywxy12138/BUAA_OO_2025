public class Derive implements Factor {
    private Expr expr;

    public Derive(Expr expr) {
        this.expr = expr;
    }

    public Factor deepCopy() {
        return new Derive((Expr) expr.deepCopy());
    }

    public Factor replaceFun(Factor repOne, Factor repTwo, String var1, String var2) {
        return new Derive((Expr) expr.replaceFun(repOne, repTwo, var1, var2));
    }

    public Poly toPoly() {
        return expr.toPoly().derive();
    }
}
