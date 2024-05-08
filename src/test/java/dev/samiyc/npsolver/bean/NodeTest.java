package dev.samiyc.npsolver.bean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.samiyc.npsolver.service.MainStaticService.MAX_OP;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NodeTest {
    @Test
    void compute_withInput_expSameOut() {
        Node n = new Node(0);
        InOut io = new InOut(4, 10);
        Assertions.assertEquals(4, io.in.size());

        for (int i = 0; i < 4; i++) {
            n.id = i;
            n.compute(io);
            Assertions.assertEquals(io.in.get(i), n.lastOut());
        }
    }

    @Test
    void compute_HighOp_IntBool_expInt() {
        List<Node> nodes = initInputNode(new Value(23), new Value(true));
        Node node = createNodeAndCompute(nodes, MAX_OP - 1);
        Assertions.assertEquals(nodes.getFirst().lastOut(), node.lastOut());
    }

    @Test
    void compute_HighOp_BoolInt_expInt() {
        List<Node> nodes = initInputNode(new Value(true), new Value(23));
        Node node = createNodeAndCompute(nodes, MAX_OP - 1);
        Assertions.assertEquals(nodes.getLast().lastOut(), node.lastOut());
    }

    @Test
    void compute_HighOp_BoolInt_expEmpty() {
        List<Node> nodes = initInputNode(new Value(false), new Value(23));
        Node node = createNodeAndCompute(nodes, MAX_OP - 1);
        Assertions.assertTrue(node.lastOut().isEmpty());
    }

    @Test
    void compute_LowOp_IntBool_expInt() {
        List<Node> nodes = initInputNode(new Value(-11), new Value(false));
        Node node = createNodeAndCompute(nodes, 1);
        Assertions.assertEquals(nodes.getFirst().lastOut(), node.lastOut());
    }

    @Test
    void compute_LowOp_BoolInt_expInt() {
        List<Node> nodes = initInputNode(new Value(false), new Value(-11));
        Node node = createNodeAndCompute(nodes, 1);
        Assertions.assertEquals(nodes.getLast().lastOut(), node.lastOut());
    }

    @Test
    void compute_LowOp_BoolInt_expEmpty() {
        List<Node> nodes = initInputNode(new Value(true), new Value(-11));
        Node node = createNodeAndCompute(nodes, 1);
        Assertions.assertTrue(node.lastOut().isEmpty());
    }

    @Test
    void compute_allOp_IntInt_expValue() {
        List<Node> nodes = initInputNode(new Value(5), new Value(-11));
        for (int i = 0; i < MAX_OP; i++) {
            Node nn = new Node(nodes, 2,200);
            Assertions.assertTrue(nn.lastOut().isEmpty());
            nn.compute(null);
            Assertions.assertFalse(nn.lastOut().isEmpty());
            nodes.add(nn);
        }
    }

    private static Node createNodeAndCompute(List<Node> nodes, int op) {
        Node node = new Node(nodes, 2,0);
        node.nodeA = nodes.getFirst();
        node.nodeB = nodes.getLast();
        node.op = op;
        node.compute(null);
        return node;
    }

    @Test
    void checkIds_expConflict() {
        List<Node> nodes = initInputNode(new Value(true), new Value(-11));
        RuntimeException thrown = assertThrows(
            RuntimeException.class, () -> {
                for (int i = 0; i <= MAX_OP; i++) {
                    nodes.add(new Node(nodes, 2,0));
                }
            },
            "Expected CONFLIC to be thrown, but it didn't"
        );
        Assertions.assertTrue(thrown.getMessage().contains("CONFLIC"));
    }

    @Test
    void evaluate() {
    }

    @Test
    void calcAverage_withPositiveValue() {
        List<Short> ls = new ArrayList<>();
        for (int i = 1; i < 5; i++) ls.add((short) i);
        Assertions.assertEquals(2.5, Node.calcAverage(ls));
    }

    @Test
    void calcAverage_withNegativeValue() {
        List<Short> ls = new ArrayList<>();
        for (int i = -5; i < 0; i++) ls.add((short) i);
        Assertions.assertEquals(-3, Node.calcAverage(ls));
    }

    @Test
    void calcAverage_withLargeList() {
        List<Short> ls = new ArrayList<>();
        for (int i = -1000000; i < 1000000; i++) ls.add((short) i);
        Assertions.assertEquals(-0.5, Node.calcAverage(ls));
    }

    @Test
    void backProp() {
    }

    @Test
    void forwardProp() {
    }

    @Test
    void removeDuplicates() {
    }

    @Test
    void cleanUp() {
    }

    @Test
    void reset() {
    }

    @Test
    void testToString() {
    }

    @Test
    void asParent() {
    }

    @Test
    void isInput() {
    }

    @Test
    void isCompute() {
    }

    @Test
    void getAvgEval() {
    }


    public static List<Node> initInputNode(Value va, Value vb) {
        Node a = new Node(0);
        a.outs.add(va);
        Node b = new Node(1);
        b.outs.add(vb);
        return new ArrayList<>(Arrays.asList(a, b));
    }
}