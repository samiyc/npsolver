package dev.samiyc.npsolver.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum des opérateurs NP Solver
 */
public enum OperatorEnum {

    // === Déclaration des constantes (obligatoirement en tête) ===
    ADD('+', Type.MATH, Type.MATH, 2),
    MINUS('-', Type.MATH, Type.MATH, 2),
    MULT('x', Type.MATH, Type.MATH, 2),
    DIV('/', Type.MATH, Type.MATH, 2),
    HYPOT('h', Type.MATH, Type.MATH, 2),
    MIN('m', Type.MATH, Type.MATH, 2),

    SQRT('#', Type.MATH, Type.MATH, 1),
    ABS('a', Type.MATH, Type.MATH, 1),

    ALT(':', Type.MATH, Type.MATH, 2),
    MORE_THAN('>', Type.MATH, Type.BOOLEAN, 2),
    BOOL_INT('?', Type.BOTH, Type.MATH, 2),

    // INVERTED('§', Type.BOOLEAN, Type.BOOLEAN, 1),
    AND('&', Type.BOOLEAN, Type.BOOLEAN, 2),
    OR('§', Type.BOOLEAN, Type.BOOLEAN, 2),
    XOR('!', Type.BOOLEAN, Type.BOOLEAN, 2),

    INPUT('¤', Type.NA, Type.MATH, 1),
    NOOP('~', Type.NA, Type.NA, 0);

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

    public boolean isInputTypeMath() {
        return inputType == Type.MATH;
    }

    public boolean isInputTypeBoolean() {
        return inputType == Type.BOOLEAN;
    }

    public boolean isOutputTypeMath() {
        return outputType == Type.MATH;
    }

    public boolean isOutputTypeBoolean() {
        return outputType == Type.BOOLEAN;
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

    /**
     * Retourne un opérateur choisi aléatoirement parmi ceux du type donné.
     *
     * @param type Type.MATH ou Type.BOOLEAN
     * @return OperatorEnum choisi aléatoirement, ou null si aucun ne correspond
     */
    public static OperatorEnum getRandomOpForInputType(Type type) {
        if (type == null)
            throw new RuntimeException("NOOP !!!");
        java.util.List<OperatorEnum> filtered = new java.util.ArrayList<>();
        for (OperatorEnum op : values()) {
            if (op.inputType == type) {
                filtered.add(op);
            }
        }
        if (filtered.isEmpty())
            throw new RuntimeException("NOOP !!!");
        return filtered.get(RNG.nextInt(filtered.size()));
    }

    public static List<OperatorEnum> opsOfType(Type type) {
        List<OperatorEnum> ops = new ArrayList<>();
        for (OperatorEnum op : OperatorEnum.values())
            if (op.inputType == type)
                ops.add(op);
        return ops;
    }

}
