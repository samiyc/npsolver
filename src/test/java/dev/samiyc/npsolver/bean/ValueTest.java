package dev.samiyc.npsolver.bean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValueTest {

    @Test
    void add_withNumber_expectSomme() {
        Value out;
        // 23 - 11 => 12
        out = new Value(23).add(new Value(-11));
        Assertions.assertEquals(12, out.number);
        Assertions.assertTrue(out.isInt());
        // 0 - 11 => -11
        out = new Value(-11).add(new Value(0));
        Assertions.assertEquals(-11, out.number);
        Assertions.assertTrue(out.isInt());
        // Empty + 23 => Empty
        out = new Value().add(new Value(0));
        Assertions.assertTrue(out.isEmpty());
    }

    @Test
    void add_withBool_expectOr() {
        Value out;
        //FF => F
        out = new Value(false).add(new Value(false));
        Assertions.assertEquals(false, out.bool);
        Assertions.assertTrue(out.isBool());
        //TF => T
        out = new Value(true).add(new Value(false));
        Assertions.assertEquals(true, out.bool);
        Assertions.assertTrue(out.isBool());
        //FT => T
        out = new Value(false).add(new Value(true));
        Assertions.assertEquals(true, out.bool);
        Assertions.assertTrue(out.isBool());
        //TT => T
        out = new Value(true).add(new Value(true));
        Assertions.assertEquals(true, out.bool);
        Assertions.assertTrue(out.isBool());
    }

    @Test
    void mult_WithNumber_ExpectMultiplied() {
        Value out;
        // 23 * (-11) => -253
        out = new Value(23).mult(new Value(-11));
        Assertions.assertEquals(-253, out.number);
        Assertions.assertTrue(out.isInt());
        // 23 * 0 => 0
        out = new Value(23).mult(new Value(0));
        Assertions.assertEquals(0, out.number);
        Assertions.assertTrue(out.isInt());
        // 23 * Empty => Empty
        out = new Value(23).mult(new Value());
        Assertions.assertTrue(out.isEmpty());
    }

    @Test
    void mult_WithBool_ExpectAnd() {
        Value out;
        //FF => F
        out = new Value(false).mult(new Value(false));
        Assertions.assertEquals(false, out.bool);
        Assertions.assertTrue(out.isBool());
        //TF => F
        out = new Value(true).mult(new Value(false));
        Assertions.assertEquals(false, out.bool);
        Assertions.assertTrue(out.isBool());
        //FT => F
        out = new Value(false).mult(new Value(true));
        Assertions.assertEquals(false, out.bool);
        Assertions.assertTrue(out.isBool());
        //TT => T
        out = new Value(true).mult(new Value(true));
        Assertions.assertEquals(true, out.bool);
        Assertions.assertTrue(out.isBool());
    }

    @Test
    void sup_withNumber_expTrueFalse() {
        Value out;
        // 23 > -11 => True
        out = new Value(23).sup(new Value(-11));
        Assertions.assertEquals(true, out.bool);
        Assertions.assertTrue(out.isBool());
        // -11 > 23 => False
        out = new Value(-11).sup(new Value(23));
        Assertions.assertEquals(false, out.bool);
        Assertions.assertTrue(out.isBool());
    }

    @Test
    void sup_withBool_expEmpty() {
        Value out;
        // True > False => Empty
        out = new Value(true).sup(new Value(false));
        Assertions.assertTrue(out.isEmpty());
        // True > Empty => Empty
        out = new Value(true).sup(new Value());
        Assertions.assertTrue(out.isEmpty());
        // Empty > True => Empty
        out = new Value().sup(new Value(true));
        Assertions.assertTrue(out.isEmpty());
    }

    @Test
    void alternative_withBoolAndEmpty_expBool() {
        Value out;
        Value a = new Value(true);
        // Empty : A => A
        out = new Value().alternative(a);
        Assertions.assertEquals(a, out);
        // A : Empty => A
        out = a.alternative(new Value());
        Assertions.assertEquals(a, out);
    }

    @Test
    void alternative_withNumberAndEmpty_ExpectBool() {
        Value out;
        Value a = new Value(23);
        // Empty : A => A
        out = new Value().alternative(a);
        Assertions.assertEquals(a, out);
        // A : Empty => A
        out = a.alternative(new Value());
        Assertions.assertEquals(a, out);
    }

    @Test
    void alternative_withBoolAndNumber_ExpectBool() {
        Value out;
        Value a = new Value(false);
        Value b = new Value(-11);
        // A : B => A
        out = a.alternative(b);
        Assertions.assertEquals(a, out);
        // B : A => B
        out = b.alternative(a);
        Assertions.assertEquals(b, out);
    }

    @Test
    void minus_withNumber_expSubstration() {
        Value out;
        Value a = new Value(23);
        Value b = new Value(-11);
        // 23 - (-11) => 34
        out = a.minus(b);
        Assertions.assertEquals(34, out.number);
        Assertions.assertTrue(out.isInt());
        // (-11) - 23 => -34
        out = b.minus(a);
        Assertions.assertEquals(-34, out.number);
        Assertions.assertTrue(out.isInt());
    }

    @Test
    void minus_withBool_expDifferent() {
        Value out;
        Value a = new Value(false);
        Value b = new Value(true);
        // A != B => True
        out = a.minus(b);
        Assertions.assertEquals(true, out.bool);
        Assertions.assertTrue(out.isBool());
        // A != A => False
        out = a.minus(a);
        Assertions.assertEquals(false, out.bool);
        Assertions.assertTrue(out.isBool());
    }

    @Test
    void minus_withBoolAndNumber_expEmpty() {
        Value out;
        Value a = new Value(false);
        Value b = new Value(-11);
        Value c = new Value();

        // A - B => Empty
        out = a.minus(b);
        Assertions.assertTrue(out.isEmpty());
        // B - A => Empty
        out = b.minus(a);
        Assertions.assertTrue(out.isEmpty());
        // C - C => Empty
        out = c.minus(c);
        Assertions.assertTrue(out.isEmpty());
    }

    @Test
    void equals_withNumber_expTrueFalse() {
        Value out;
        Value a = new Value(23);
        Value b = new Value(-11);
        Value c = new Value(23);

        // A == C => True
        Assertions.assertEquals(a, c);
        // A == B => False
        Assertions.assertNotEquals(a, b);
    }

    @Test
    void equals_withBool_expTrueFalse() {
        Value out;
        Value a = new Value(true);
        Value b = new Value(false);
        Value c = new Value(true);

        // A == C => True
        Assertions.assertEquals(a, c);
        // A == B => False
        Assertions.assertNotEquals(a, b);
    }

    @Test
    void equals_withEmpty_expOnlyFalse() {
        Value out;
        Value a = new Value(12);
        Value b = new Value(true);
        Value c = new Value();

        // A == B => False
        Assertions.assertNotEquals(a, b);
        // B == C => False
        Assertions.assertNotEquals(b, c);
        // A == C => False
        Assertions.assertNotEquals(a, c);
    }

    @Test
    void testToString() {
        Assertions.assertEquals("N", new Value().toString());
        Assertions.assertEquals("12", new Value(12).toString());
        Assertions.assertEquals("true", new Value(true).toString());
    }

    @Test
    void bothInt_test() {
        Assertions.assertTrue(new Value(-11).bothInt(new Value(23)));//HERE
        Assertions.assertFalse(new Value(11).bothInt(new Value()));
        Assertions.assertFalse(new Value().bothInt(new Value(23)));
        Assertions.assertFalse(new Value(11).bothInt(new Value(true)));
        Assertions.assertFalse(new Value(true).bothInt(new Value()));
        Assertions.assertFalse(new Value(true).bothInt(new Value(false)));
    }

    @Test
    void bothBool_SimpleTest() {
        Assertions.assertFalse(new Value(-11).bothBool(new Value(23)));
        Assertions.assertFalse(new Value(11).bothBool(new Value()));
        Assertions.assertFalse(new Value().bothBool(new Value(23)));
        Assertions.assertFalse(new Value(11).bothBool(new Value(true)));
        Assertions.assertFalse(new Value(true).bothBool(new Value()));
        Assertions.assertTrue(new Value(true).bothBool(new Value(false)));//HERE
    }

    @Test
    void isInt_SimpleTest() {
        Assertions.assertTrue(new Value(-11).isInt());//HERE
        Assertions.assertFalse(new Value().isInt());
        Assertions.assertFalse(new Value(true).isInt());
    }

    @Test
    void isBool_SimpleTest() {
        Assertions.assertFalse(new Value(-11).isBool());
        Assertions.assertFalse(new Value().isBool());
        Assertions.assertTrue(new Value(true).isBool());
    }

    @Test
    void isEmpty_SimpleTest() {
        Assertions.assertFalse(new Value(-11).isEmpty());
        Assertions.assertTrue(new Value().isEmpty());
        Assertions.assertFalse(new Value(true).isEmpty());
    }
}