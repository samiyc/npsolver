package dev.samiyc.npsolver.service;

import dev.samiyc.npsolver.bean.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static dev.samiyc.npsolver.service.EvaluationStaticService.VALUE_FOUND;

class EvaluationStaticServiceTest {
    @Test
    void callEval_withOkValueAndBadDelta_expectValueFound() {
        Assertions.assertEquals(VALUE_FOUND, EvaluationStaticService.eval(10, 10, -6, -6));
        Assertions.assertEquals(VALUE_FOUND, EvaluationStaticService.eval(10, 10, -6, 7));
        Assertions.assertEquals(VALUE_FOUND, EvaluationStaticService.eval(-10, -10, 5, 5));
        Assertions.assertEquals(VALUE_FOUND, EvaluationStaticService.eval(-10, -10, -5, 5));
    }
    @Test
    void callEval_withClothEnoughValue_expectLowValue() {
        Assertions.assertEquals(51, EvaluationStaticService.eval(10, 20, 2, 2));
        Assertions.assertEquals(51, EvaluationStaticService.eval(-10, -30, -5, -5));
    }
    @Test
    void callEval_withFarAwayValue_ExpectNoMatch() {
        Assertions.assertEquals(0, EvaluationStaticService.eval(-14, 203, 2, -51));
        Assertions.assertEquals(0, EvaluationStaticService.eval(1000, -3, 1605, -21));
        Assertions.assertEquals(0, EvaluationStaticService.eval(-1000, 31, -1602, 23));
        Assertions.assertEquals(0, EvaluationStaticService.eval(-1000, 31, -1602, 23));
        Assertions.assertEquals(0, EvaluationStaticService.eval(-1000, 31, -1602, 23));
        Assertions.assertEquals(0, EvaluationStaticService.eval(-14, 203, 2, -5));
    }
    @Test
    void callEval_withBool_ExpectTrueFalse() {
        Assertions.assertEquals(VALUE_FOUND, EvaluationStaticService.eval(true, true));
        Assertions.assertEquals(0, EvaluationStaticService.eval(true, false));
        Assertions.assertEquals(0, EvaluationStaticService.eval(false, true));
    }
    @Test
    void callEval_withBoolAndNumber_ExpectTrueFalse() {
        //true
        Assertions.assertEquals(VALUE_FOUND, EvaluationStaticService.eval(new Value(-13), new Value(true), new Value(), new Value()));
        Assertions.assertEquals(VALUE_FOUND, EvaluationStaticService.eval(new Value(13), new Value(true), new Value(), new Value()));
        Assertions.assertEquals(VALUE_FOUND, EvaluationStaticService.eval(new Value(), new Value(false), new Value(), new Value()));
        //false
        Assertions.assertEquals(0, EvaluationStaticService.eval(new Value(), new Value(true), new Value(), new Value()));
        Assertions.assertEquals(0, EvaluationStaticService.eval(new Value(13), new Value(false), new Value(), new Value()));
        Assertions.assertEquals(0, EvaluationStaticService.eval(new Value(-13), new Value(false), new Value(), new Value()));
    }
}
