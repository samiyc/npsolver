package dev.samiyc.npsolver.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EvaluationServiceUtilTest {
    @Test
    void baseEval() {
        Assertions.assertEquals(100, EvaluationStaticService.eval(10, 10, -6, -6));
        Assertions.assertEquals(100, EvaluationStaticService.eval(10, 10, -6, 7));
        Assertions.assertEquals(100, EvaluationStaticService.eval(-10, -10, 5, 5));
        Assertions.assertEquals(100, EvaluationStaticService.eval(-10, -10, -5, 5));
        Assertions.assertEquals(90, EvaluationStaticService.eval(10, 20, 2, 2));
        Assertions.assertEquals(90, EvaluationStaticService.eval(-10, -30, -5, -5));
        Assertions.assertEquals(90, EvaluationStaticService.eval(22, 23, 1, 1));
        Assertions.assertEquals(90, EvaluationStaticService.eval(-14, 23, 1, 1));
        Assertions.assertEquals(70, EvaluationStaticService.eval(-14, 23, 1, -1));
        Assertions.assertEquals(40, EvaluationStaticService.eval(-14, 203, 2, -5));
        Assertions.assertEquals(30, EvaluationStaticService.eval(-14, 203, 2, -51));
        Assertions.assertEquals(10, EvaluationStaticService.eval(1000, -3, 1605, -21));
        Assertions.assertEquals(0, EvaluationStaticService.eval(-1000, 31, -1602, 23));

        Assertions.assertEquals(100, EvaluationStaticService.eval(true, true));
        Assertions.assertEquals(0, EvaluationStaticService.eval(true, false));
    }

}
