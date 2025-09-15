package dev.samiyc.npsolver.service;

import static dev.samiyc.npsolver.service.MainStaticService.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import dev.samiyc.npsolver.bean.InOut;
import dev.samiyc.npsolver.bean.Node;
import dev.samiyc.npsolver.bean.NodeTest;
import dev.samiyc.npsolver.bean.Value;
import dev.samiyc.npsolver.utils.OperatorEnum;

class MainStaticServiceTest {

    @Test
    void doOneCycle() {
    }

    @Test
    void initNodes() {
        List<Node> nodes = MainStaticService.initNodes();
        int nbInputs = (int) nodes.stream().filter(n -> n.op == OperatorEnum.INPUT).count();

        Assertions.assertEquals(MAX_ID, nodes.size());
        Assertions.assertEquals(NB_INPUT, nbInputs);
    }

    @Test
    void cleanUp_BelowNoiseLimit() {
        List<Node> nodes = NodeTest.initInputNode(new Value(-11), new Value(-11));
        Node n = new Node(nodes, 2, 0);
        n.avgEval = 0.1;
        nodes.add(n);
        Assertions.assertNotNull(n.parents.get(0));
        Assertions.assertTrue(n.op.isUnary() || n.parentB() != null);
        
        //CLEAN UP
        MainStaticService.cleanUp(nodes, 3, 10);

        //Disconnected node
        Assertions.assertNull(n.parentA());
        Assertions.assertNull(n.parentB());
        Assertions.assertEquals(44.44, n.avgEval);

        Node na = nodes.get(0);
        Node nb = nodes.get(1);
        Node newnode = nodes.get(2);

        //Reset des inputs
        Assertions.assertEquals(0, na.lastOut().number);
        Assertions.assertEquals(0, nb.lastOut().number);
        Assertions.assertEquals(OperatorEnum.INPUT, na.op);
        Assertions.assertEquals(OperatorEnum.INPUT, nb.op);
        
        //delete + new calc node
        Assertions.assertEquals(3, nodes.size());
        Assertions.assertNotEquals(n, newnode);
        Assertions.assertNotEquals(OperatorEnum.INPUT, newnode.op);
    }

    @Test
    void cleanUp_NoCleanUp() {
        List<Node> nodes = NodeTest.initInputNode(new Value(-11), new Value(-11));
        Node n = new Node(nodes, 2, 0);
        n.avgEval = 50;
        nodes.add(n);
        MainStaticService.cleanUp(nodes, 3, 10);
        Assertions.assertTrue(nodes.contains(n.parentA()));
        Assertions.assertTrue(n.op.isUnary() || nodes.contains(n.parentB()));
        Assertions.assertEquals(n, nodes.get(2));
    }

    @Test
    void callinitMap_checkOuts() {
        int problemId = 16;
        List<InOut> inOuts = InOut.initMap(11, 7, problemId, 100);
        Assertions.assertEquals(11, inOuts.size());
        Assertions.assertEquals(7, inOuts.getFirst().in.size());

        for (InOut io : inOuts) {
            Assertions.assertEquals(io.calcOut(problemId), io.out);
        }
        // Quickly check InOut toString()
        Assertions.assertTrue(inOuts.getFirst().toString().matches(".* => .*"));
    }

    @Test
    void callgetMin_withNumber_expectMinimum() {
        List<Node> nodes = Arrays.asList(
                new Node(0),
                new Node(1),
                new Node(2),
                new Node(3)
        );
        nodes.get(0).avgEval = 3;
        Assertions.assertEquals(3.0, MainStaticService.getMinZeroExcluded(nodes));
        nodes.get(1).avgEval = 0;
        Assertions.assertEquals(3.0, MainStaticService.getMinZeroExcluded(nodes));
        nodes.get(1).avgEval = 0.06;
        Assertions.assertEquals(0.06, MainStaticService.getMinZeroExcluded(nodes));
        nodes.get(2).avgEval = 11;
        Assertions.assertEquals(0.06, MainStaticService.getMinZeroExcluded(nodes));
        nodes.get(3).avgEval = -12;
        Assertions.assertEquals(-12.0, MainStaticService.getMinZeroExcluded(nodes));
    }

    @Test
    void callgetMax_withNumber_expectMaximum() {
        List<Node> nodes = Arrays.asList(
                new Node(0),
                new Node(1),
                new Node(2),
                new Node(3)
        );
        nodes.get(0).avgEval = 3;
        Assertions.assertEquals(3.0, MainStaticService.getMax(nodes));
        nodes.get(1).avgEval = 0;
        Assertions.assertEquals(3.0, MainStaticService.getMax(nodes));
        nodes.get(2).avgEval = 11;
        Assertions.assertEquals(11.0, MainStaticService.getMax(nodes));
        nodes.get(3).avgEval = -12;
        Assertions.assertEquals(11.0, MainStaticService.getMax(nodes));
    }

    @Test
    void run_withProblemId_1() {
        run_withProblemId(1);
    }

    @Test
    void run_withProblemId_2() {
        run_withProblemId(2);
    }

    @Test
    void run_withProblemId_3() {
        run_withProblemId(3);
    }

    @Test
    void run_withProblemId_4() {
        run_withProblemId(4);
    }

    @Test
    void run_withProblemId_5() {
        run_withProblemId(5);
    }

    @Test
    void run_withProblemId_6() {
        run_withProblemId(6);
    }

    @Test
    void run_withProblemId_7() {
        run_withProblemId(7);
    }

    @Test
    void run_withProblemId_8() {
        run_withProblemId(8);
    }

    @Test
    void run_withProblemId_9() {
        run_withProblemId(9);
    }

    @Test
    void run_withProblemId_10() {
        run_withProblemId(10);
    }

    @Test
    void run_withProblemId_11() {
        run_withProblemId(11);
    }

    @Test
    @Tag("flaky") // Unstable test
    void run_withProblemId_12() {
        run_withProblemId(12);
    }

    @Test
    void run_withProblemId_13() {
        run_withProblemId(13);
    }

    @Test
    void run_withProblemId_14() {
        run_withProblemId(14);
    }

    @Test
    void run_withProblemId_15() {
        run_withProblemId(15);
    }

    @Test
    @Tag("flaky") // Unstable test
    void run_withProblemId_16() {
        run_withProblemId(16);
    }

    @Test
    @Tag("flaky") // Unstable test
    void run_withProblemId_17() {
        run_withProblemId(17);
    }

    @Test
    @Tag("flaky") // Unstable test
    void run_withProblemId_18() {
        run_withProblemId(18);
    }

    private static void run_withProblemId(int problemId) {
        List<Node> nodes = MainStaticService.run(problemId);
        Assertions.assertTrue(nodes.stream().anyMatch(n -> n.avgEval == 100));
    }
}