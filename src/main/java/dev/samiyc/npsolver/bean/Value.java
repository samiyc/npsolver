package dev.samiyc.npsolver.bean;

public class Value {
    public Integer number = 0;
    public Boolean bool;
    public ValueType type = ValueType.EMPTY;

    public Value() {
    }

    public Value(Integer val) {
        type = ValueType.INT;
        number = val;
    }

    public Value(Boolean val) {
        type = ValueType.BOOL;
        bool = val;
    }

    public Value add(Value other) {
        if (bothInt(other)) return new Value(number + other.number);
        if (bothBool(other)) return new Value(bool || other.bool);
        if (other.isEmpty()) return this;
        if (isEmpty()) return other;
        return new Value();
    }

    public Value mult(Value other) {
        if (bothInt(other)) return new Value(number * other.number);
        if (bothBool(other)) return new Value(bool && other.bool);
        return new Value();
    }

    public Value sup(Value other) {
        if (bothInt(other)) return new Value(number > other.number);
        return new Value();
    }

    public Value alternative(Value other) {
        if (bothInt(other)) return other;
        if (bothBool(other)) return other;
        return this;
    }

    public Value minus(Value other) {
        if (bothInt(other)) return new Value(number - other.number);
        if (bothBool(other)) return new Value(!bool.equals(other.bool));
        if (other.isEmpty()) return this;
        if (isEmpty()) {
            if (other.isInt()) return new Value(-other.number);
            if (other.isBool()) return new Value(!other.bool);
        }
        return new Value();
    }

    public Value eq(Value other) {
        if (bothInt(other)) return new Value(number.equals(other.number));
        if (bothBool(other)) return new Value(bool.equals(other.bool));
        return new Value();
    }

    @Override
    public String toString() {
        if (isInt()) return number.toString();
        if (isBool()) return bool.toString();
        return "N";
    }

    public boolean bothInt(Value other) {
        return isInt() && other.isInt();
    }

    public boolean bothBool(Value other) {
        return isBool() && other.isBool();
    }

    public boolean isInt() {
        return type == ValueType.INT;
    }

    public boolean isBool() {
        return type == ValueType.BOOL;
    }

    public boolean isEmpty() {
        return type == ValueType.EMPTY;
    }
}
