package dev.samiyc.npsolver.bean;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.samiyc.npsolver.service.EvaluationStaticService;
import dev.samiyc.npsolver.service.MainStaticService;
import dev.samiyc.npsolver.utils.OperatorEnum;

public class NodeTest {
    @Test
    void compute_withInput_expSameOut() {
        Node n = new Node(0);
        InOut io = new InOut(4, 10, 100);
        Assertions.assertEquals(4, io.in.size());

        for (int i = 0; i < 4; i++) {
            n.id = i;
            n.compute(io);
            Assertions.assertEquals(io.in.get(i), n.lastOut());
        }
    }

    @Test
    void compute_abs_expError_absWithBool() {
        List<Node> nodes = initInputNode(new Value(false), new Value(23));
        RuntimeException thrown = assertThrows(
                RuntimeException.class, () -> createNodeAndCompute(nodes, OperatorEnum.ABS),
                "Expected 'ABS WITH BOOL' error to be thrown, but it didn't");
        Assertions.assertTrue(thrown.getMessage().contains("ABS WITH BOOL"));
    }

    @Test
    void compute_IntBool_withTrue_expInt() {
        List<Node> nodes = initInputNode(new Value(-11), new Value(true));
        Node node = createNodeAndCompute(nodes, OperatorEnum.BOOL_INT);
        Assertions.assertEquals(nodes.getFirst().lastOut(), node.lastOut());
    }

    @Test
    void compute_alt_withTrue_expInt() {
        List<Node> nodes = initInputNode(new Value(-11), new Value(true));
        Node node = createNodeAndCompute(nodes, OperatorEnum.ALT);
        Assertions.assertEquals(nodes.getFirst().lastOut(), node.lastOut());
    }

    @Test
    void compute_IntBool2_withFalse_expEmpty() {
        List<Node> nodes = initInputNode(new Value(-11), new Value(false));
        Node node = createNodeAndCompute(nodes, OperatorEnum.BOOL_INT);
        Assertions.assertTrue(node.lastOut().isEmpty());
    }

    @Test
    void compute_alt2_withFalse_expInt() {
        List<Node> nodes = initInputNode(new Value(-11), new Value(false));
        Node node = createNodeAndCompute(nodes, OperatorEnum.ALT);
        Assertions.assertEquals(nodes.getFirst().lastOut(), node.lastOut());
    }

    @Test
    void compute_IntBool3_withFalseFirst_expEmpty() {
        List<Node> nodes = initInputNode(new Value(false), new Value(-11));
        Node node = createNodeAndCompute(nodes, OperatorEnum.BOOL_INT);
        Assertions.assertTrue(node.lastOut().isEmpty());
    }

    @Test
    void compute_alt3_withFalseFirst_expInt() {
        List<Node> nodes = initInputNode(new Value(false), new Value(-11));
        Node node = createNodeAndCompute(nodes, OperatorEnum.ALT);
        Assertions.assertEquals(nodes.getFirst().lastOut(), node.lastOut());
    }

    @Test
    void compute_allOp_IntInt_expValue() {
        List<Node> nodes = initInputNode(new Value(5), new Value(-11));
        for (int i = 0; i < OperatorEnum.opsOfType(OperatorEnum.Type.MATH).size(); i++) {
            Node nn = new Node(nodes, 2, 200);
            Assertions.assertTrue(nn.lastOut().isEmpty());
            nn.compute(null);
            Assertions.assertFalse(nn.lastOut().isEmpty());
            nodes.add(nn);
        }
    }

    private static Node createNodeAndCompute(List<Node> nodes, OperatorEnum op) {
        Node node = new Node(nodes, 2, 0);
        node.parents = nodes;
        node.op = op;
        node.compute(null);
        return node;
    }

    @Test
    void checkIds_expConflict() {
        List<Node> nodes = initInputNode(new Value(true), new Value(-11));
        RuntimeException thrown = assertThrows(
                RuntimeException.class, () -> {
                    for (int i = 0; i <= OperatorEnum.opsOfType(OperatorEnum.Type.MATH).size(); i++) {
                        nodes.add(new Node(nodes, 2, 0));
                    }
                },
                "Expected CONFLIC to be thrown, but it didn't");
        Assertions.assertTrue(thrown.getMessage().contains("CONFLIC"));
    }

    @Test
    void evaluate_withSumOp_expSumAndAvgEvalAsValueFound() {
        int problemId = 4;

        // Init nodes
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0));
        nodes.add(new Node(1));
        Node thrdNod = new Node(nodes, 2, 0);
        thrdNod.parents = nodes;
        thrdNod.op = OperatorEnum.ADD;
        nodes.add(thrdNod);

        // Init the map
        List<InOut> map = new ArrayList<>();
        map.add(new InOut(problemId, Arrays.asList(-5, -10, 50, 45)));
        map.add(new InOut(problemId, Arrays.asList(50, 45, -5, -10)));
        map.add(new InOut(problemId, Arrays.asList(50, -50, 71, -23)));
        map.add(new InOut(problemId, Arrays.asList(71, -23, 50, -50)));

        // Compute & Evaluate
        for (InOut io : map)
            nodes.forEach(n -> n.compute(io));
        nodes.forEach(n -> n.evaluate(map));

        // Asserts
        Assertions.assertEquals(EvaluationStaticService.VALUE_FOUND, thrdNod.avgEval);
        Assertions.assertEquals(-15, thrdNod.outs.get(0).number);
        Assertions.assertEquals(95, thrdNod.outs.get(1).number);
        Assertions.assertEquals(0, thrdNod.outs.get(2).number);
        Assertions.assertEquals(48, thrdNod.outs.get(3).number);
    }

    @Test
    void callEvaluate_withBadOp_expLowAvgEval() {
        int problemId = 4;

        // Init nodes
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0));
        nodes.add(new Node(1));
        Node thrdNod = new Node(nodes, 2, 0);
        thrdNod.parents = nodes;
        thrdNod.op = OperatorEnum.MINUS;
        nodes.add(thrdNod);

        // Init the map
        List<InOut> map = new ArrayList<>();
        map.add(new InOut(problemId, Arrays.asList(-5, -10, 50, 45)));
        map.add(new InOut(problemId, Arrays.asList(50, 45, -5, -10)));
        map.add(new InOut(problemId, Arrays.asList(50, -50, 71, -23)));
        map.add(new InOut(problemId, Arrays.asList(71, -23, 50, -50)));

        // Compute & Evaluate
        for (InOut io : map)
            nodes.forEach(n -> n.compute(io));
        nodes.forEach(n -> n.evaluate(map));

        // Asserts
        Assertions.assertNotEquals(EvaluationStaticService.VALUE_FOUND, thrdNod.avgEval);
        Assertions.assertEquals(5, thrdNod.outs.get(0).number);
        Assertions.assertEquals(5, thrdNod.outs.get(1).number);
        Assertions.assertEquals(100, thrdNod.outs.get(2).number);
        Assertions.assertEquals(94, thrdNod.outs.get(3).number);
    }

    @Test
    void calcAverage_withPositiveValue() {
        List<Short> ls = new ArrayList<>();
        for (int i = 1; i < 5; i++)
            ls.add((short) i);
        Assertions.assertEquals(2.5, Node.calcAverage(ls));
    }

    @Test
    void calcAverage_withNegativeValue() {
        List<Short> ls = new ArrayList<>();
        for (int i = -5; i < 0; i++)
            ls.add((short) i);
        Assertions.assertEquals(-3, Node.calcAverage(ls));
    }

    @Test
    void calcAverage_withLargeList() {
        List<Short> ls = new ArrayList<>();
        for (int i = -1000000; i < 1000000; i++)
            ls.add((short) i);
        Assertions.assertEquals(-0.5, Node.calcAverage(ls));
    }

    @Test
    void backProp_withParentLower_expParentAvgEvalIncrease() {
        // Init nodes
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0));
        nodes.add(new Node(1));
        Node thrdNod = createCustomNode(nodes, 0, 1, 2, OperatorEnum.MINUS, 10);
        Node frthNod = createCustomNode(nodes, 0, 2, 3, OperatorEnum.MINUS, 21);
        // Function call
        frthNod.backProp();
        // Evals
        Assertions.assertEquals(21.0, thrdNod.avgEval);
    }

    @Test
    void backProp_withParentHigher_expNoChange() {
        // Init nodes
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0));
        nodes.add(new Node(1));
        Node thrdNod = createCustomNode(nodes, 0, 1, 2, OperatorEnum.MINUS, 21);
        Node frthNod = createCustomNode(nodes, 0, 2, 3, OperatorEnum.MINUS, 11);
        // Function call
        frthNod.backProp();
        // Evals
        Assertions.assertEquals(21.0, thrdNod.avgEval);
    }

    private static Node createCustomNode(List<Node> nodes, int ida, int idb, int curId, OperatorEnum op, int avgEval) {
        Node customNode = new Node(curId);
        customNode.outs = new ArrayList<>();
        customNode.evals = new ArrayList<>();
        customNode.childs = new ArrayList<>();
        Node nodeA = nodes.get(ida);
        Node nodeB = nodes.get(idb);
        customNode.parents.add(nodeA);
        customNode.parents.add(nodeB);
        nodeA.addChild(customNode);
        nodeB.addChild(customNode);
        customNode.op = op;
        customNode.avgEval = avgEval;
        nodes.add(customNode);
        return customNode;
    }

    @Test
    void forwardProp_withParentHigher_expFrthNodeRemoved() {
        // Init nodes
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0));
        nodes.add(new Node(1));
        Node thrdNod = createCustomNode(nodes, 0, 1, 2, OperatorEnum.MINUS, 21);
        Node frthNod = createCustomNode(nodes, 0, 2, 3, OperatorEnum.MINUS, 10);
        // Function call
        frthNod.forwardProp();

        // Evals
        // Node 3
        Assertions.assertTrue(thrdNod.isComputeWithParent());
        Assertions.assertEquals(21.0, thrdNod.avgEval);
        // Node 4
        Assertions.assertFalse(frthNod.isComputeWithParent());
        Assertions.assertNull(frthNod.parentA());
        Assertions.assertNull(frthNod.parentB());
        Assertions.assertNull(frthNod.outs);
        Assertions.assertNull(frthNod.evals);
        Assertions.assertNull(frthNod.childs);
    }

    @Test
    void forwardProp_withParentLower_expNoChange() {
        // Init nodes
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0));
        nodes.add(new Node(1));
        Node thrdNod = createCustomNode(nodes, 0, 1, 2, OperatorEnum.MINUS, 10);
        Node frthNod = createCustomNode(nodes, 0, 2, 3, OperatorEnum.MINUS, 21);
        // Function call
        frthNod.forwardProp();

        // Evals
        // Node 3
        Assertions.assertTrue(thrdNod.isComputeWithParent());
        Assertions.assertEquals(10.0, thrdNod.avgEval);
        // Node 4
        Assertions.assertTrue(frthNod.isComputeWithParent());
        Assertions.assertEquals(21.0, frthNod.avgEval);
        Assertions.assertNotNull(frthNod.parentA());
        Assertions.assertNotNull(frthNod.parentB());
        Assertions.assertNotNull(frthNod.outs);
        Assertions.assertNotNull(frthNod.evals);
        Assertions.assertNotNull(frthNod.childs);
    }

    @Test
    void removeDuplicates() {
        int problemId = 3;

        // Init the map
        List<InOut> map = new ArrayList<>();
        map.add(new InOut(problemId, Arrays.asList(-5, -10, 50, 45)));
        map.add(new InOut(problemId, Arrays.asList(50, 45, -5, -10)));
        map.add(new InOut(problemId, Arrays.asList(50, -50, 71, -23)));
        map.add(new InOut(problemId, Arrays.asList(71, -23, 50, -50)));

        // Init nodes
        List<Node> nodes = new ArrayList<>();
        Node inputA = new Node(0);
        nodes.add(inputA);
        nodes.add(new Node(1));
        nodes.add(new Node(2));
        nodes.add(new Node(3));
        Node nodeA = createCustomNode(nodes, 0, 1, 4, OperatorEnum.MULT, 10);
        Node nodeB = createCustomNode(nodes, 0, 4, 5, OperatorEnum.ALT, 10);
        Node nodeC = createCustomNode(nodes, 0, 5, 6, OperatorEnum.ADD, 10);

        // Function call
        for (InOut io : map)
            nodes.forEach(n -> n.compute(io));
        nodes.forEach(n -> n.evaluate(map));
        nodes.forEach(n -> n.cleanUp(-1.0));

        // Connect child to parent
        Assertions.assertEquals("4[AxB|-2500 -1633|75.0]", nodeA.toString());
        Assertions.assertEquals("_", nodeB.toString()); // Avant cleanup 5[A:4|50 71|75.0]
        Assertions.assertEquals("6[A+A|100 142|75.0]", nodeC.toString());

        // Remove from main list
        MainStaticService.cleanUp(nodes, 6, 10);
        Assertions.assertEquals("4[AxB|---|75.0]", nodeA.toString());
        Assertions.assertEquals("5[A+A|---|75.0]", nodeC.toString());

        // Verifications
        // inputs
        Assertions.assertEquals(2, inputA.childs.size());
        Assertions.assertTrue(inputA.childs.contains(nodeA));
        Assertions.assertTrue(inputA.childs.contains(nodeA));
        // Node A
        Assertions.assertTrue(nodes.contains(nodeA));
        Assertions.assertTrue(nodeA.isComputeWithParent());
        Assertions.assertFalse(nodeA.childs.contains(nodeB));
        Assertions.assertFalse(nodeA.childs.contains(nodeC));
        // Node B
        Assertions.assertFalse(nodes.contains(nodeB));
        Assertions.assertFalse(nodeB.isComputeWithParent());
        Assertions.assertNull(nodeB.parents);
        Assertions.assertNull(nodeB.outs);
        Assertions.assertNull(nodeB.childs);
        Assertions.assertNull(nodeB.evals);
        Assertions.assertEquals(55.55, nodeB.avgEval);
        // Node C
        Assertions.assertTrue(nodes.contains(nodeC));
        Assertions.assertTrue(nodeC.isComputeWithParent());
        Assertions.assertEquals(0, nodeC.parentA().id);
        Assertions.assertEquals(0, nodeC.parentB().id);
    }

    @Test
    void compute_Ternary_pid15() {
        // On cible problemId=15 : a < b ? c : d
        final int problemId = 15;

        // Jeu de données : couvre les deux branches (true/false) du ternaire
        List<InOut> map = new ArrayList<>();
        map.add(new InOut(problemId, Arrays.asList(0, 1, 5, 9))); // a<b → c (=5)
        map.add(new InOut(problemId, Arrays.asList(2, -1, 7, 8))); // a<b false → d (=8)
        map.add(new InOut(problemId, Arrays.asList(3, 10, -5, 42))); // a<b → c (=-5)
        map.add(new InOut(problemId, Arrays.asList(9, 9, 1, 2))); // a<b false (égalité) → d (=2)

        // Inputs A=0, B=1, C=2, D=3
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0));
        nodes.add(new Node(1));
        nodes.add(new Node(2));
        nodes.add(new Node(3));

        // Node 4 : MORE_THAN(1,0) // b > a ≡ a < b
        createCustomNode(nodes, 1, 0, 4, OperatorEnum.MORE_THAN, 10);

        // Node 5 : BOOL_INT(4,2) // if (node4==true) then C else EMPTY
        createCustomNode(nodes, 4, 2, 5, OperatorEnum.BOOL_INT, 10);

        // Node 6 : ALT(5,3) // Node5 != EMPTY ? Node5 : D
        Node node6 = createCustomNode(nodes, 5, 3, 6, OperatorEnum.ALT, 10);

        // Compute !
        for (InOut io : map)
            nodes.forEach(n -> n.compute(io));

        // Vérifie que le ternaire reconstruit matche exactement la vérité du
        // problemId=15
        for (int i = 0; i < map.size(); i++) {
            assertEquals(map.get(i).out, node6.outs.get(i), "Mismatch on sample #" + i);
        }

        // On peut aussi scorer pour confirmer le 100%
        nodes.forEach(n -> n.evaluate(map));
        assertTrue(node6.getAvgEval() >= 100.0, "Expected perfect match (>=100) for reconstructed ternary");
    }

    @Test
    void collectAncestors_and_depth_behave_as_expected_on_problem16() {
        int problemId = 5;
        List<Node> nodes = MainStaticService.run(problemId);

        Node sol = latestPerfect(nodes);

        // 1) collect all ancestors
        Set<Node> ancestors = new HashSet<>();
        sol.collectAncestors(ancestors);

        assertFalse(ancestors.contains(sol), "Ancestors should not include solution itself");
        assertTrue(ancestors.size() > 0, "Expect non-empty ancestors for composite solutions");

        // 2) ensure uniqueness (Set already guarantees, but sanity check)
        long distinct = ancestors.stream().distinct().count();
        assertEquals(distinct, ancestors.size(), "Ancestors must be unique");

        // 3) sorted views should be stable
        List<Node> byId = ancestors.stream()
                .sorted(Comparator.comparingInt(n -> n.id))
                .collect(Collectors.toList());
        List<Integer> ids = byId.stream().map(n -> n.id).collect(Collectors.toList());
        List<Integer> sorted = new ArrayList<>(ids);
        Collections.sort(sorted);
        assertEquals(sorted, ids, "Ancestors should be sortable by id consistently");
    }

    private Node latestPerfect(List<Node> nodes) {
        return nodes.stream()
                .filter(n -> n.getAvgEval() >= 100.0)
                .max(Comparator.comparingInt(n -> n.id)) // use field directly if no getter
                .orElseThrow(() -> new AssertionError("No node with 100.0"));
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