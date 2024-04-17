package dev.samiyc.npsolver.service;

import dev.samiyc.npsolver.bean.Value;

public class EvaluationStaticService {

    public static final int SIMILAR_EVAL_BOOST = 1;
    public static final int MAX_ESTIMATION = 80;

    /**
     * Lock the constructor to make sure it stay instanceLess
     */
    private EvaluationStaticService() {
    }

    public static int eval(int valOut, int valExp, int valOutDif, int valExpDif, int outDifDif, int expDifDif) {
        return eval(new Value(valOut), new Value(valExp),
                new Value(valOutDif), new Value(valExpDif),
                new Value(outDifDif), new Value(expDifDif));
    }

    public static int eval(boolean valOut, boolean valExp) {
        return eval(new Value(valOut), new Value(valExp), new Value(), new Value(), new Value(), new Value());
    }

    public static int eval(Value out, Value exp, Value outDif, Value expDif, Value outDifDif, Value expDifDif) {
        int eval = 0;
        if (out.bothBool(exp)) {
            if (out.bool.equals(exp.bool)) eval = 100;
        } else if (!out.isBool() && exp.isBool()) {
            if ((!out.isEmpty() && exp.bool) || (out.isEmpty() && !exp.bool)) eval = 100;
        } else if (out.bothInt(exp)) {
            if (out.number.equals(exp.number)) {
                eval = 100;
            } else {
                eval += compareValues(out.number, exp.number);
                if (!expDif.isEmpty()) eval += compareValues(outDif.number, expDif.number);
                if (!expDifDif.isEmpty()) eval += compareValues(outDifDif.number, expDifDif.number);
                if (eval > MAX_ESTIMATION) eval = MAX_ESTIMATION;
            }
        }
        return eval;
    }

    private static int compareValues(int out, int exp) {
        int eval = 0;
        eval += evaluate((out == exp)); //Sign
        eval += evaluate((out >= 0 && exp >= 0) || (out < 0 && exp < 0)); //Sign
        eval += evaluate(out % 2 == 0 && exp % 2 == 0);
        eval += evaluate(out % 3 == 0 && exp % 3 == 0);
        eval += evaluate(out % 5 == 0 && exp % 5 == 0);
        eval += evaluate(out % 7 == 0 && exp % 7 == 0);
        eval += evaluate(out / 10 == exp / 10);
        eval += evaluate(out / 100 == exp / 100);
        eval += evaluate(out / 1000 == exp / 1000);
        eval += evaluate(out != 0 && exp % out == 0);   //exp multiple de out
        eval += evaluate(exp != 0 && out % exp == 0);   //out multiple de out
        return eval;
    }

    public static int evaluate(boolean b) {
        return b ? SIMILAR_EVAL_BOOST : 0;
    }
}
