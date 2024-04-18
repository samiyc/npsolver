package dev.samiyc.npsolver.service;

import dev.samiyc.npsolver.bean.Value;

import static dev.samiyc.npsolver.service.MainService.SIMILAR_EVAL_BOOST;

public class EvaluationStaticService {
    public static final int VALUE_FOUND = 100;

    /**
     * Lock the constructor to make sure it stay instanceLess
     */
    private EvaluationStaticService() {
    }

    public static int eval(int valOut, int valExp, int valOutDif, int valExpDif) {
        return eval(new Value(valOut), new Value(valExp),
                new Value(valOutDif), new Value(valExpDif));
    }

    public static int eval(boolean valOut, boolean valExp) {
        return eval(new Value(valOut), new Value(valExp), new Value(), new Value());
    }

    public static short eval(Value out, Value exp, Value outDif, Value expDif) {
        short eval = 0;
        if (out.bothInt(exp)) {
            if (out.number.equals(exp.number)) {
                eval = VALUE_FOUND;
            } else {
                eval += compareValues(out.number, exp.number);
                eval += compareValues(outDif.number, expDif.number);
            }
        } else if (out.bothBool(exp) && out.bool.equals(exp.bool)) {
            eval = VALUE_FOUND;
        } else if (exp.isBool() && exp.bool.equals(!out.isEmpty())) {
            eval = VALUE_FOUND;
        }
        return eval;
    }

    private static short compareValues(int out, int exp) {
        short eval = 0;
        eval += evaluate(Math.abs(out) == Math.abs(exp));
        eval += evaluate(out > 0 == exp > 0); //Sign
        eval += evaluate(out != 0 && exp % out == 0); //multiple of
        return eval;
    }

    public static short evaluate(boolean b) {
        return b ? SIMILAR_EVAL_BOOST : 0;
    }

    private static boolean absMatch(Value a, Value b) {
        return Math.abs(a.number) == Math.abs(b.number);
    }
}
