package dev.samiyc.npsolver.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum des opérateurs NP Solver
 */
public enum OperatorEnum {

    //UNARY
    ABS('a', Type.MATH, Type.MATH, 1),
    SQRT('#', Type.MATH, Type.MATH, 1),

    //MATH
    ADD('+', Type.MATH, Type.MATH, 2),
    MINUS('-', Type.MATH, Type.MATH, 2),
    MULT('x', Type.MATH, Type.MATH, 2),
    DIV('/', Type.MATH, Type.MATH, 2),
    HYPOT('h', Type.MATH, Type.MATH, 2),
    MIN('m', Type.MATH, Type.MATH, 2),
    MODULO('%', Type.MATH, Type.MATH, 2),

    //TERNARY
    MORE_THAN('>', Type.MATH, Type.BOOLEAN, 2),
    BOOL_INT('?', Type.BOTH, Type.MATH, 2),
    ALT(':', Type.MATH, Type.MATH, 2),

    //BOOLEAN
    AND('&', Type.BOOLEAN, Type.BOOLEAN, 2),
    OR('§', Type.BOOLEAN, Type.BOOLEAN, 2),
    XOR('!', Type.BOOLEAN, Type.BOOLEAN, 2),

    //SPECIAL
    CORRUPTED_GATE('¤', Type.NA, Type.NA, 2), //NOT YET!
    BIN_LSHIFT('⭺', Type.MATH, Type.MATH, 2),
    BIN_RSHIFT('⭼', Type.MATH, Type.MATH, 2),
    BIN_AND('⇼', Type.MATH, Type.MATH, 2),

    INPUT('z', Type.NA, Type.MATH, 1),
    NOOP('z', Type.NA, Type.NA, 0);

    private static final java.util.Random RNG = new java.util.Random();

    /** Sous enum pour le type */
    public enum Type {
        MATH, BOOLEAN, BOTH, NA
    }

    public final char symbol;
    public final Type inputType;
    public final Type outputType;
    public final int nbInput;

    OperatorEnum(char symbol, Type inputType, Type outputType, int nbInput) {
        this.symbol = symbol;
        this.inputType = inputType;
        this.outputType = outputType;
        this.nbInput = nbInput;
    }

    public char getSymbol() {
        return symbol;
    }

    public int getNbInput() {
        return nbInput;
    }

    public boolean isUnary() {
        return nbInput == 1;
    }

    public boolean isBinary() {
        return nbInput == 2;
    }

    public boolean isInputType(Type t) {
        return inputType == t;
    }

    public boolean isOutputType(Type t) {
        return outputType == t;
    }

    // --- Lookups ---

    /** Map symbole -> enum (inclut l'espace pour NOOP). */
    public static final String LIST_OPERATOR;
    static {
        StringBuilder temp = new StringBuilder();
        for (OperatorEnum op : values()) {
            if (op.getSymbol() != ' ')
                temp.append(op.getSymbol());
        }
        LIST_OPERATOR = temp.toString();
    }

    public static OperatorEnum randomOpOfInputType(Type type, List<OperatorEnum> excluded) {
        List<OperatorEnum> filtered = opsOfInputType(type);
        if (excluded != null) filtered.removeAll(excluded);
        return filtered.get(RNG.nextInt(filtered.size()));
    }

    public static List<OperatorEnum> opsOfInputType(Type type) {
        List<OperatorEnum> ops = new ArrayList<>();
        for (OperatorEnum op : OperatorEnum.values())
            if (op.inputType == type)
                ops.add(op);
        return ops;
    }

}
