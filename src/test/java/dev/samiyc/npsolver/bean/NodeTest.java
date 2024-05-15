package dev.samiyc.npsolver.bean;

import dev.samiyc.npsolver.service.EvaluationStaticService;
import dev.samiyc.npsolver.service.MainStaticService;
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
            Node nn = new Node(nodes, 2, 200);
            Assertions.assertTrue(nn.lastOut().isEmpty());
            nn.compute(null);
            Assertions.assertFalse(nn.lastOut().isEmpty());
            nodes.add(nn);
        }
    }

    private static Node createNodeAndCompute(List<Node> nodes, int op) {
        Node node = new Node(nodes, 2, 0);
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
                        nodes.add(new Node(nodes, 2, 0));
                    }
                },
                "Expected CONFLIC to be thrown, but it didn't"
        );
        Assertions.assertTrue(thrown.getMessage().contains("CONFLIC"));
    }

    @Test
    void evaluate_withSumOp_expSumAndAvgEvalAsValueFound() {
        int problemId = 4;

        //Init nodes
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0));
        nodes.add(new Node(1));
        Node thrdNod = new Node(nodes, 2, 0);
        thrdNod.nodeA = nodes.getFirst();
        thrdNod.nodeB = nodes.getLast();
        thrdNod.op = 0;
        nodes.add(thrdNod);

        //Init the map
        List<InOut> map = new ArrayList<>();
        map.add(new InOut(problemId, Arrays.asList(-5, -10, 50, 45)));
        map.add(new InOut(problemId, Arrays.asList(50, 45, -5, -10)));
        map.add(new InOut(problemId, Arrays.asList(50, -50, 71, -23)));
        map.add(new InOut(problemId, Arrays.asList(71, -23, 50, -50)));

        //Compute & Evaluate
        for (InOut io : map) nodes.forEach(n -> n.compute(io));
        nodes.forEach(n -> n.evaluate(map));

        //Asserts
        Assertions.assertEquals(EvaluationStaticService.VALUE_FOUND, thrdNod.avgEval);
        Assertions.assertEquals(-15, thrdNod.outs.get(0).number);
        Assertions.assertEquals(95, thrdNod.outs.get(1).number);
        Assertions.assertEquals(0, thrdNod.outs.get(2).number);
        Assertions.assertEquals(48, thrdNod.outs.get(3).number);
    }

    @Test
    void callEvaluate_withBadOp_expLowAvgEval() {
        int problemId = 4;

        //Init nodes
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0));
        nodes.add(new Node(1));
        Node thrdNod = new Node(nodes, 2, 0);
        thrdNod.nodeA = nodes.getFirst();
        thrdNod.nodeB = nodes.getLast();
        thrdNod.op = 1;
        nodes.add(thrdNod);

        //Init the map
        List<InOut> map = new ArrayList<>();
        map.add(new InOut(problemId, Arrays.asList(-5, -10, 50, 45)));
        map.add(new InOut(problemId, Arrays.asList(50, 45, -5, -10)));
        map.add(new InOut(problemId, Arrays.asList(50, -50, 71, -23)));
        map.add(new InOut(problemId, Arrays.asList(71, -23, 50, -50)));

        //Compute & Evaluate
        for (InOut io : map) nodes.forEach(n -> n.compute(io));
        nodes.forEach(n -> n.evaluate(map));

        //Asserts
        Assertions.assertNotEquals(EvaluationStaticService.VALUE_FOUND, thrdNod.avgEval);
        Assertions.assertEquals(5, thrdNod.outs.get(0).number);
        Assertions.assertEquals(5, thrdNod.outs.get(1).number);
        Assertions.assertEquals(100, thrdNod.outs.get(2).number);
        Assertions.assertEquals(94, thrdNod.outs.get(3).number);
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
    void backProp_withParentLower_expParentAvgEvalIncrease() {
        //Init nodes
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0));
        nodes.add(new Node(1));
        Node thrdNod = createCustomNode(nodes, 0, 1, 2, 1, 10);
        Node frthNod = createCustomNode(nodes, 0, 2, 3, 1, 21);
        //Function call
        frthNod.backProp();
        //Evals
        Assertions.assertEquals(21.0, thrdNod.avgEval);
    }

    @Test
    void backProp_withParentHigher_expNoChange() {
        //Init nodes
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0));
        nodes.add(new Node(1));
        Node thrdNod = createCustomNode(nodes, 0, 1, 2, 1, 21);
        Node frthNod = createCustomNode(nodes, 0, 2, 3, 1, 11);
        //Function call
        frthNod.backProp();
        //Evals
        Assertions.assertEquals(21.0, thrdNod.avgEval);
    }

    private static Node createCustomNode(List<Node> nodes, int ida, int idb, int curId, int op, int avgEval) {
        Node customNode = new Node(curId);
        customNode.outs = new ArrayList<>();
        customNode.evals = new ArrayList<>();
        customNode.childs = new ArrayList<>();
        customNode.nodeA = nodes.get(ida);
        customNode.nodeB = nodes.get(idb);
        customNode.nodeA.addChild(customNode);
        customNode.nodeB.addChild(customNode);
        customNode.op = op;
        customNode.avgEval = avgEval;
        nodes.add(customNode);
        return customNode;
    }

    @Test
    void forwardProp_withParentHigher_expFrthNodeRemoved() {
        //Init nodes
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0));
        nodes.add(new Node(1));
        Node thrdNod = createCustomNode(nodes, 0, 1, 2, 1, 21);
        Node frthNod = createCustomNode(nodes, 0, 2, 3, 1, 10);
        //Function call
        frthNod.forwardProp();

        //Evals
        //Node 3
        Assertions.assertTrue(thrdNod.isComputeWithParent());
        Assertions.assertEquals(21.0, thrdNod.avgEval);
        //Node 4
        Assertions.assertFalse(frthNod.isComputeWithParent());
        Assertions.assertNull(frthNod.nodeA);
        Assertions.assertNull(frthNod.nodeB);
        Assertions.assertNull(frthNod.outs);
        Assertions.assertNull(frthNod.evals);
        Assertions.assertNull(frthNod.childs);
    }

    @Test
    void forwardProp_withParentLower_expNoChange() {
        //Init nodes
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0));
        nodes.add(new Node(1));
        Node thrdNod = createCustomNode(nodes, 0, 1, 2, 1, 10);
        Node frthNod = createCustomNode(nodes, 0, 2, 3, 1, 21);
        //Function call
        frthNod.forwardProp();

        //Evals
        //Node 3
        Assertions.assertTrue(thrdNod.isComputeWithParent());
        Assertions.assertEquals(10.0, thrdNod.avgEval);
        //Node 4
        Assertions.assertTrue(frthNod.isComputeWithParent());
        Assertions.assertEquals(21.0, frthNod.avgEval);
        Assertions.assertNotNull(frthNod.nodeA);
        Assertions.assertNotNull(frthNod.nodeB);
        Assertions.assertNotNull(frthNod.outs);
        Assertions.assertNotNull(frthNod.evals);
        Assertions.assertNotNull(frthNod.childs);
    }

    @Test
    void removeDuplicates() {
        int problemId = 3;

        //Init the map
        List<InOut> map = new ArrayList<>();
        map.add(new InOut(problemId, Arrays.asList(-5, -10, 50, 45)));
        map.add(new InOut(problemId, Arrays.asList(50, 45, -5, -10)));
        map.add(new InOut(problemId, Arrays.asList(50, -50, 71, -23)));
        map.add(new InOut(problemId, Arrays.asList(71, -23, 50, -50)));

        //Init nodes
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0));
        nodes.add(new Node(1));
        nodes.add(new Node(2));
        nodes.add(new Node(3));
        Node nodeA = createCustomNode(nodes, 0, 1, 4, 2, 10);
        Node nodeB = createCustomNode(nodes, 0, 4, 5, 4, 10);
        Node nodeC = createCustomNode(nodes, 0, 5, 6, 0, 10);

        //Function call
        for (InOut io : map) nodes.forEach(n -> n.compute(io));
        nodes.forEach(n -> n.evaluate(map));
        nodes.forEach(n -> n.removeDuplicates(nodes));
        Assertions.assertEquals("4[AxB|-2500 -1633|75.0]", nodeA.toString());
        Assertions.assertEquals("_", nodeB.toString());
        Assertions.assertEquals("6[A+4|-2450 -1562|75.0]", nodeC.toString());
        //clean up
        MainStaticService.cleanUp(nodes, 6, 10);
        Assertions.assertEquals("4[AxB|---|75.0]", nodeA.toString());
        Assertions.assertEquals("5[A+4|---|75.0]", nodeC.toString());

        //Verifications
        //Node A
        Assertions.assertTrue(nodeA.isComputeWithParent());
        Assertions.assertFalse(nodeA.childs.contains(nodeB));
        Assertions.assertTrue(nodeA.childs.contains(nodeC));
        //Node B
        Assertions.assertFalse(nodes.contains(nodeB));
        Assertions.assertFalse(nodeB.isComputeWithParent());
        Assertions.assertNull(nodeB.nodeA);
        Assertions.assertNull(nodeB.nodeB);
        Assertions.assertNull(nodeB.outs);
        Assertions.assertNull(nodeB.evals);
        Assertions.assertNull(nodeB.childs);
        //Node C
        Assertions.assertTrue(nodeC.isComputeWithParent());
        Assertions.assertEquals(0, nodeC.nodeA.id);
        Assertions.assertEquals(4, nodeC.nodeB.id);
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