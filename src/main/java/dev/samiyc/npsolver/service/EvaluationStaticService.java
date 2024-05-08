package dev.samiyc.npsolver.service;

import dev.samiyc.npsolver.bean.Value;

public class EvaluationStaticService {
    public static final int VALUE_FOUND = 100;

    private EvaluationStaticService() {
        /* Lock the constructor to make sure it stay static */
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
        if (out.equals(exp)) {
            eval = VALUE_FOUND;
        } else if (out.bothInt(exp) && outDif.number.equals(expDif.number)) {
            eval = 51;
        } else if (!out.isBool() && exp.isBool() && exp.bool.equals(!out.isEmpty())) {
            eval = VALUE_FOUND;
        }
        return eval;
    }

}//End of Eval.SS
