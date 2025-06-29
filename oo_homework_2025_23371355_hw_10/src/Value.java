public class Value {
    //与某人的友情值
    private int value;
    //该人的id
    private int id;

    public Value(int value, int id) {
        this.value = value;
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public int getId() {
        return id;
    }

    public void addValue(int value) {
        this.value += value;
    }

    public boolean bestThan(Value anotherValue) {
        return ((this.value > anotherValue.value)
                || (this.value == anotherValue.value && this.id < anotherValue.id));
    }
}
