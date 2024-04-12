package dev.samiyc.npsolver.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EvaluationServiceUtilTest {
    @Test
    void baseEval() {
        Assertions.assertEquals(100, EvaluationServiceUtil.eval(10, 10, -6, -6));
        Assertions.assertEquals(100, EvaluationServiceUtil.eval(10, 10, -6, 7));
        Assertions.assertEquals(100, EvaluationServiceUtil.eval(-10, -10, 5, 5));
        Assertions.assertEquals(100, EvaluationServiceUtil.eval(-10, -10, -5, 5));
        Assertions.assertEquals(65, EvaluationServiceUtil.eval(10, 20, 2, 2));
        Assertions.assertEquals(60, EvaluationServiceUtil.eval(-10, -30, -5, -5));
        Assertions.assertEquals(55, EvaluationServiceUtil.eval(22, 23, 1, 1));
        Assertions.assertEquals(45, EvaluationServiceUtil.eval(-14, 23, 1, 1));
        Assertions.assertEquals(35, EvaluationServiceUtil.eval(-14, 23, 1, -1));
        Assertions.assertEquals(20, EvaluationServiceUtil.eval(-14, 203, 2, -5));
        Assertions.assertEquals(15, EvaluationServiceUtil.eval(-14, 203, 2, -51));
        Assertions.assertEquals(5, EvaluationServiceUtil.eval(1000, -3, 1605, -21));
        Assertions.assertEquals(0, EvaluationServiceUtil.eval(-1000, 31, -1602, 23));

        Assertions.assertEquals(100, EvaluationServiceUtil.eval(true, true));
        Assertions.assertEquals(0, EvaluationServiceUtil.eval(true, false));
    }

}
