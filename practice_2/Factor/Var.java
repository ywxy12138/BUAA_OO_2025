package Factor;

public class Var implements Factor {
    
    private String name;
    
    public Var(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public Factor derive() {
        Term term = new Term();
        Number newVar = new Number("1");
        term.addFactor(newVar);
        return term;
    }
    
    @Override
    public Factor clone() {
        return new Var(name);
    }
}
