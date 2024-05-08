package dev.samiyc.npsolver.service;

import dev.samiyc.npsolver.bean.InOut;
import dev.samiyc.npsolver.bean.Node;
import dev.samiyc.npsolver.bean.NodeTest;
import dev.samiyc.npsolver.bean.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.util.Arrays;
import java.util.List;

import static dev.samiyc.npsolver.service.MainStaticService.*;

class MainStaticServiceTest {

    public static final boolean INTEGRATION = true;

    boolean isIntegration() {
        return INTEGRATION;
    }

    @Test
    void doOneCycle() {
    }

    @Test
    void initNodes() {
        List<Node> nodes = MainStaticService.initNodes();
        int nbInputs = (int) nodes.stream().filter(n -> n.op == MAX_OP).count();

        Assertions.assertEquals(MAX_ID, nodes.size());
        Assertions.assertEquals(NB_INPUT, nbInputs);
    }

    @Test
    void cleanUp_BelowNoiseLimit() {
        List<Node> nodes = NodeTest.initInputNode(new Value(-11), new Value(-11));
        Node n = new Node(nodes, 2);
        n.avgEval = 0.1;
        nodes.add(n);
        Assertions.assertNotNull(n.nodeA);
        Assertions.assertNotNull(n.nodeB);
        MainStaticService.cleanUp(nodes, 3, 10);
        Assertions.assertNull(n.nodeA);
        Assertions.assertNull(n.nodeB);
        Assertions.assertEquals(44.44, n.avgEval);
        Assertions.assertEquals(0, nodes.get(0).lastOut().number);
        Assertions.assertEquals(0, nodes.get(1).lastOut().number);
        Node nn = nodes.get(2);
        Assertions.assertNotEquals(n, nn);
    }

    @Test
    void cleanUp_NoCleanUp() {
        List<Node> nodes = NodeTest.initInputNode(new Value(-11), new Value(-11));
        Node n = new Node(nodes, 2);
        n.avgEval = 50;
        nodes.add(n);
        MainStaticService.cleanUp(nodes, 3, 10);
        Assertions.assertEquals(nodes.get(0), n.nodeA);
        Assertions.assertEquals(nodes.get(1), n.nodeB);
        Assertions.assertEquals(n, nodes.get(2));
    }

    @Test
    void callinitMap_checkOuts() {
        int problemId = 15;
        List<InOut> inOuts = MainStaticService.initMap(11, 7, problemId);
        Assertions.assertEquals(11, inOuts.size());
        Assertions.assertEquals(7, inOuts.getFirst().in.size());

        for (InOut io : inOuts) {
            Assertions.assertEquals(io.calcOut(problemId), io.out);
        }
        //Quickly check InOut toString()
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
    @EnabledIf("isIntegration")
    void run_withProblemId_1() {
        run_withProblemId(1);
    }

    @Test
    @EnabledIf("isIntegration")
    void run_withProblemId_2() {
        run_withProblemId(2);
    }

    @Test
    @EnabledIf("isIntegration")
    void run_withProblemId_3() {
        run_withProblemId(3);
    }

    @Test
    @EnabledIf("isIntegration")
    void run_withProblemId_4() {
        run_withProblemId(4);
    }

    @Test
    @EnabledIf("isIntegration")
    void run_withProblemId_5() {
        run_withProblemId(5);
    }

    @Test
    @EnabledIf("isIntegration")
    void run_withProblemId_6() {
        run_withProblemId(6);
    }

    @Test
    @EnabledIf("isIntegration")
    void run_withProblemId_7() {
        run_withProblemId(7);
    }

    @Test
    @EnabledIf("isIntegration")
    void run_withProblemId_8() {
        run_withProblemId(8);
    }

    @Test
    @EnabledIf("isIntegration")
    void run_withProblemId_9() {
        run_withProblemId(9);
    }

    @Test
    @EnabledIf("isIntegration")
    void run_withProblemId_10() {
        run_withProblemId(10);
    }

    @Test
    @EnabledIf("isIntegration")
    void run_withProblemId_11() {
        run_withProblemId(11);
    }

    @Test
    @EnabledIf("isIntegration")
    void run_withProblemId_12() {
        run_withProblemId(12);
    }

    @Test
    @EnabledIf("isIntegration")
    void run_withProblemId_13() {
        run_withProblemId(13);
    }

    private static void run_withProblemId(int problemId) {
        List<Node> nodes = MainStaticService.run(problemId);
        Assertions.assertTrue(nodes.stream().anyMatch(n -> n.avgEval == 100));
    }
}