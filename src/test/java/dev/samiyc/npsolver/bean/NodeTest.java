package dev.samiyc.npsolver.bean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodeTest {

    @Test
    void baseEval() {
        Assertions.assertEquals(100, Node.baseEval(10, 10, -6, -6));
        Assertions.assertEquals(100, Node.baseEval(10, 10, -6, 7));
        Assertions.assertEquals(100, Node.baseEval(-10, -10, 5, 5));
        Assertions.assertEquals(100, Node.baseEval(-10, -10, -5, 5));
        Assertions.assertEquals(20, Node.baseEval(-14, 23, 1, 1));
        Assertions.assertEquals(35, Node.baseEval(-10, -30, -5, -5));
        Assertions.assertEquals(0, Node.baseEval(-1000, 31, -16, 23));
    }
}
