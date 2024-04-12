package dev.samiyc.npsolver.service;

import dev.samiyc.npsolver.bean.Value;

public class EvaluationServiceUtil {

    public static final int SIMILAR_EVAL_BOOST_5 = 5;
    public static final int MAX_ESTIMATION_80 = 80;

    /**
     * Lock the constructor to make sure it stay instanceLess
     */
    private EvaluationServiceUtil() {
    }

    public static int eval(int valOut, int valExp, int valOutDif, int valExpDif) {
        return eval(new Value(valOut), new Value(valExp), new Value(valOutDif), new Value(valExpDif));
    }
    public static int eval(boolean valOut, boolean valExp) {
        return eval(new Value(valOut), new Value(valExp), new Value(), new Value());
    }

    public static int eval(Value valOut, Value valExp, Value valOutDif, Value valExpDif) {
        int eval = 0;
        if (bothBool(valOut, valExp)) {
            if (valOut.bool.equals(valExp.bool)) eval = 100;
        }
        else if (bothInt(valOut, valExp)) {
            if (valOut.number.equals(valExp.number)) {
                eval = 100;
            } else {
                eval += compareValues(valOutDif.number, valExpDif.number);
                eval += compareValues(valOut.number, valExp.number);
                if (eval > MAX_ESTIMATION_80) eval = MAX_ESTIMATION_80;
            }
        }
        return eval;
    }

    private static int compareValues(int out, int exp) {
        int eval = 0;
        eval += evaluate((out == exp)); //Sign
        eval += evaluate((out >= 0 && exp >= 0) || (out < 0 && exp < 0)); //Sign
        eval += evaluate(out % 2 == 0 && exp % 2 == 0); //Multiple de 2
        eval += evaluate(out % 3 == 0 && exp % 3 == 0); //Multiple de 3
        eval += evaluate(out / 10 == exp / 10);
        eval += evaluate(out / 100 == exp / 100);
        eval += evaluate(out / 1000 == exp / 1000);
        eval += evaluate(out != 0 && exp % out == 0);   //exp multiple de out
        eval += evaluate(exp != 0 && out % exp == 0);   //out multiple de out
        return eval;
    }

    public static int evaluate(boolean b) {
        return b ? SIMILAR_EVAL_BOOST_5 : 0;
    }
    private static boolean bothInt(Value valOut, Value valExp) {
        return valOut.isInt() && valExp.isInt();
    }
    private static boolean bothBool(Value valOut, Value valExp) {
        return valOut.isBool() && valExp.isBool();
    }
}
