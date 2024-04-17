package dev.samiyc.npsolver.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EvaluationServiceUtilTest {
    @Test
    void baseEval() {
        Assertions.assertEquals(100, EvaluationStaticService.eval(10, 10, -6, -6, 1, 1));
        Assertions.assertEquals(100, EvaluationStaticService.eval(10, 10, -6, 7, 1, 1));
        Assertions.assertEquals(100, EvaluationStaticService.eval(-10, -10, 5, 5, 1, 1));
        Assertions.assertEquals(100, EvaluationStaticService.eval(-10, -10, -5, 5, 1, 1));
        Assertions.assertEquals(90, EvaluationStaticService.eval(10, 20, 2, 2, 1, 1));
        Assertions.assertEquals(90, EvaluationStaticService.eval(-10, -30, -5, -5, 1, 1));
        Assertions.assertEquals(90, EvaluationStaticService.eval(22, 23, 1, 1, 1, 1));
        Assertions.assertEquals(80, EvaluationStaticService.eval(-14, 23, 1, 1, 1, 1));
        Assertions.assertEquals(70, EvaluationStaticService.eval(-14, 23, 1, -1, 1, 1));
        Assertions.assertEquals(60, EvaluationStaticService.eval(-14, 203, 2, -5, 1, 1));
        Assertions.assertEquals(55, EvaluationStaticService.eval(-14, 203, 2, -51, 1, 1));
        Assertions.assertEquals(40, EvaluationStaticService.eval(1000, -3, 1605, -21, 1, 1));
        Assertions.assertEquals(35, EvaluationStaticService.eval(-1000, 31, -1602, 23, 1, 1));
        Assertions.assertEquals(25, EvaluationStaticService.eval(-1000, 31, -1602, 23, -3, 6));
        Assertions.assertEquals(0, EvaluationStaticService.eval(-1000, 31, -1602, 23, -3001, 6));

        Assertions.assertEquals(100, EvaluationStaticService.eval(true, true));
        Assertions.assertEquals(0, EvaluationStaticService.eval(true, false));
    }

}
