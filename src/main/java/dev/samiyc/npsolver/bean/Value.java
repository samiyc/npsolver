package dev.samiyc.npsolver.bean;

import dev.samiyc.npsolver.Application;

public class Value {
    Integer number = 0;
    Boolean bool = false;
    ValueType type;

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
        if (isEmpty()) return other; if (other.isEmpty()) return this;
        if (bothInt(other))  return new Value(number + other.number);
        if (bothBool(other)) return new Value(bool || other.bool);
        return new Value();
    }
    public Value minus(Value other) {
        if (other.isEmpty()) return this;
        if (bothInt(other))  return new Value(number - other.number);
        if (bothBool(other)) return new Value(bool != other.bool);
        return new Value();
    }
    public Value mult(Value other) {
        if (isEmpty() || other.isEmpty()) return new Value();
        if (bothInt(other))  return new Value(number * other.number);
        if (bothBool(other)) return new Value(bool && other.bool);
        if (isInt() && other.isBool() && other.bool) return new Value(number);
        if (isBool() && other.isInt() && bool) return new Value(other.number);
        return new Value();
    }
    public Value inf(Value other) {
        if (bothInt(other))  return new Value(number < other.number);
        if (bothBool(other)) return new Value(!bool && other.bool); // False < True
        return new Value();
    }

    public Value alternative(Value other) {
        if (isBool() && !bool) return other;
        return new Value();
    }

    private boolean bothInt(Value other) {
        return isInt() && other.isInt();
    }
    private boolean bothBool(Value other) {
        return isBool() && other.isBool();
    }

    @Override
    public String toString() {
        if (isInt())  return number.toString();
        if (isBool()) return bool.toString();
        return "EMPTY";
    }

    public boolean isInt() { return type == ValueType.INT; }
    boolean isBool() { return type == ValueType.BOOL; }
    boolean isEmpty() { return type == ValueType.BOOL; }
}
