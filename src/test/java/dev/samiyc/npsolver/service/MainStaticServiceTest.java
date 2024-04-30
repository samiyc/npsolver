package dev.samiyc.npsolver.service;

import dev.samiyc.npsolver.bean.InOut;
import dev.samiyc.npsolver.bean.Node;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class MainStaticServiceTest {

    @Test
    void run() {
    }

    @Test
    void doOneCycle() {
    }

    @Test
    void messageInfoLog() {
    }

    @Test
    void performanceInfos() {
    }

    @Test
    void initNodes() {
    }

    @Test
    void callinitMap__() {
        List<InOut> inOuts = MainStaticService.initMap(11, 7);
        Assertions.assertEquals(11, inOuts.size());
        Assertions.assertEquals(7, inOuts.get(0).in.size());

        for(InOut io : inOuts) {
            Assertions.assertEquals(io.calcOut(), io.out);
        }
    }

    @Test
    void cleanUp_with_expect() {
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
}