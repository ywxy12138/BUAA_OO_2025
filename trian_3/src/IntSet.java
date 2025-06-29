public interface IntSet {
    //@ public instance model int[] ia;
    //@ public invariant (\forall int i, j; 0 <= i && i < j && j < ia.length; ia[i] < ia[j]);


    //@ ensures \result == (\exists int i; 0 <= i && i < ia.length; ia[i] == x);
    public /*@ pure @*/ Boolean contains(int x);

    /*@ public normal_behavior
      @ assignable \nothing
      @ requires 0 <= x && x < ia.length;
      @ ensures \result == ia[x];
      @ also
      @ public exceptional_behavior
      @ signals (IndexOutOfBoundsException e) (x < 0 || x >= ia.length);
      @*/
    public int getNum(int x) throws IndexOutOfBoundsException;

    /*@ public normal_behavior
      @ assignable ia;
      @ requires !contains(x);
      @ ensures contains(x);
      @ ensures (\forall int i; 0 <= i && i < \old(ia.length); contains(\old(ia[i])));
      @ ensures (\forall int i; 0 <= i && i < ia.length && ia[i] != x; \old(contains(ia[i])));
      @*/
    public void insert(int x);

    /*@ public normal_behavior
      @ assignable ia;
      @ requires contains(x);
      @ ensures !contains(x);
      @ ensures (\forall int i; 0 <= i && i < ia.length; \old(contains(ia[i])));
      @ ensures (\forall int i; 0 <= i && i < \old(ia.length) && \old(ia[i]) != x; contains(\old(ia[i])));
      @*/
    public void delete(int x);

    //@ ensures \result == ia.length;
    public /*@ pure @*/ int size();
    
    /**
     * 该方法用于计算对称差
     * 数学上，两个集合的对称差是只属于其中一个集合，而不属于另一个集合的元素组成的集合
     *        集合论中的这个运算相当于布尔逻辑中的异或运算，如 A, B 两个集合的对称差记为 A⊕B，
     *        则 A⊕B = (A-B)∪(B-A) = (A∪B)-(A∩B)
     *        例如：集合{1,2,3}和{3,4}的对称差为{1,2,4}
     * @param a 与 this 进行对称差运算的操作数
     * @return 返回两个 {@link IntSet} 对象的对称差运算结果
     * @throws NullPointerException 如果对空的 IntSet 对象进行对称差的运算，将抛出 NullPointerException 的异常
     */
    //TODO
    /*@ public normal_behavior
      @ requires a != null;
      @ assignable \nothing;
      @ ensures (\forall int i; 0 <= i && i < \result.length;
      @ (contains(\result[i]) && !a.contains(\result[i])) ||
      @ (!contains(\result[i]) && a.contains(\result[i])));
      @ ensures (\forall int j; 0 <= j && j < ia.length;
      @ (\forall int k; 0 <= k && k < a.ia.length; ia[j] != a.ia[k]) ==>
      @ \result.contains(ia[j]));
      @ ensures (\forall int j; 0 <= j && j < a.ia.length;
      @ (\forall int k; 0 <= k && k < ia.length; a.ia[j] != ia[k]) ==>
      @ \result.contains(a.ia[j]));
      @ also
      @ public exceptional_behavior
      @ signals (NullPointerException) (a == null);
     */
    public /*@ pure @*/ IntSet symmetricDifference(IntSet a) throws NullPointerException;
    
    /**
     * 该方法用于计算 **三个集合中恰好属于两个集合的元素** 组成的集合
     * 数学上，三个集合的恰好属于两个集合的元素是只属于其中任意两个集合交集，且不属于三个集合交集的元素
     *        集合论中的这个运算可以表示为 (A∩B ∪ A∩C ∪ B∩C) - (A∩B∩C)
     *        其中 A = this, B = a, C = b
     *        例如：集合A={1,2}, B={2,3}, C={3,4} 的运算结果为 {2,3,4}
     *             集合A={1,2,3}, B={2,3,4}, C={3,4,5} 的运算结果为 {2,4}
     * @param a 参与运算的第一个 IntSet 操作数
     * @param b 参与运算的第二个 IntSet 操作数
     * @return 返回一个新的 {@link IntSet} 对象，包含恰好属于任意两个集合的元素
     * @throws NullPointerException 如果参数 a 或 b 为 null，将抛出 NullPointerException 异常
     */
    //TODO
    /*@ public normal_behavior
      @ requires a != null;
      @ requires b != null;
      @ assignable \nothing;
      @ ensures \result != null;
      @ ensures (\forall int i; \result.contains(i) <==>
      @ (!contains(i) && a.contains(i) && b.contains(i)) ||
      @ (contains(i) && !a.contains(i) && b.contains(i)) ||
      @ (contains(i) && a.contains(i) && !b.contains(i)));
      @ also
      @ public exceptional_behavior
      @ signals (NullPointerException) (a == null || b == null);
     */
    public /*@ pure @*/ IntSet elementsInExactlyTwoSets(IntSet a, IntSet b) throws NullPointerException;

    //@ ensures \result == (\forall int i, j ; 0 <= i && i < j && j < ia.length; ia[i] != ia[j] && ia[i] < ia[j]);
    public /*@ pure @*/ boolean repOK();
}
