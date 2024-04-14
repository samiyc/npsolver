package dev.samiyc.npsolver.bean;

import java.util.Objects;

public class Value {
    public Integer number = 0;
    public Boolean bool = false;
    public ValueType type;

    public Value() {
        type = ValueType.EMPTY;
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
        if (isEmpty()) return other;
        if (other.isEmpty()) return this;
        if (bothInt(other)) return new Value(number + other.number);
        if (bothBool(other)) return new Value(bool || other.bool);
        if (isInt() && other.isBool()) return new Value(number + (other.bool ? 1 : 0));
        if (isBool() && other.isInt()) return new Value((bool ? 1 : 0) + other.number);
        return new Value();
    }

    public Value mult(Value other) {
        if (isEmpty() || other.isEmpty()) return new Value();
        if (bothInt(other)) return new Value(number * other.number);
        if (bothBool(other)) return new Value(bool && other.bool);
        if (isInt() && other.isBool() && other.bool) return new Value(number);
        if (isBool() && other.isInt() && bool) return new Value(other.number);
        return new Value();
    }

    public Value sup(Value other) {
        if (bothInt(other)) return number > other.number ? new Value(number) : new Value();
        if (bothBool(other)) return new Value(!bool && !other.bool);
        if (isInt() && other.isBool()) return new Value(other.bool == (number >= 0));
        if (isBool() && other.isInt()) return new Value(bool == (other.number >= 0));
        return new Value();
    }

    public Value alternative(Value other) {
        if (isInt() && other.isInt()) return other;
        if (isBool() && other.isBool()) return other;
        return this;
    }

    public Value minus(Value other) {
        if (other.isEmpty()) return this;
        if (isEmpty()) {
            if (other.isInt()) return new Value(-other.number);
            if (other.isBool()) return new Value(!other.bool);
        }
        if (bothInt(other)) return new Value(number - other.number);
        if (bothBool(other)) return new Value(bool != other.bool);
        if (isInt() && other.isBool()) return new Value(number - (other.bool ? 1 : 0));
        if (isBool() && other.isInt()) return new Value((bool ? 1 : 0) - other.number);
        return new Value();
    }

    public Value eq(Value other) {
        if (bothInt(other)) return new Value(Objects.equals(number, other.number));
        if (bothBool(other)) return new Value(bool == other.bool);
        return new Value(false);
    }

    private boolean bothInt(Value other) {
        return isInt() && other.isInt();
    }

    private boolean bothBool(Value other) {
        return isBool() && other.isBool();
    }

    @Override
    public String toString() {
        if (isInt()) return number.toString();
        if (isBool()) return bool.toString();
        return "N";
    }

    public boolean isInt() {
        return type == ValueType.INT;
    }

    public boolean isBool() {
        return type == ValueType.BOOL;
    }

    public boolean isEmpty() {
        return type == ValueType.BOOL;
    }
}
