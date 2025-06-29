public interface Factor {
    Factor deepCopy();

    Factor replaceFun(Factor repOne, Factor repTwo, String var1, String var2);

    Poly toPoly();
}
