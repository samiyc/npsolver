package dev.samiyc.npsolver.service;

import dev.samiyc.npsolver.bean.InOut;
import dev.samiyc.npsolver.bean.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainStaticService {
    public static final boolean MSG_INFO = false;
    public static final short SIMILAR_EVAL_BOOST = 5;
    public static final int BACK_PROP_LOSS = 1, MAX_OP = 6;
    public static final int FOWARD_PROP_UPPER_LIMIT = 80;
    public static final int MAX_ID = 500, NB_INPUT = 4;
    public static final int MAX_CYCLE = 10000, IO_MAP_NB_ENTRY = 100;
    public static final double NOISE_LIMIT = 1.0;
    public static final Random random = new Random();

    private MainStaticService() {
        /* Lock the constructor to make sure it stay static */
    }

    /**
     * Main entry point
     */
    public static void run() {
        List<Node> nodes = initNodes();

        double max = 0; int count = 0;
        while (max < 100 && count++ < MAX_CYCLE) {
            max = DoOneCycle(nodes, IO_MAP_NB_ENTRY, NB_INPUT, count);
        }
        System.out.println();
    }

    public static double DoOneCycle(List<Node> nodes, int ioNbEntry, int nbInput, int count) {
        double max;
        double min;
        long cycleStartCt = System.currentTimeMillis();
        List<InOut> map = initMap(ioNbEntry, nbInput);

        long initMapCt = System.currentTimeMillis();
        for (InOut io : map) nodes.forEach(n -> n.compute(io));
        //messageInfoLog("\n### SIM > OUTS", nodes);

        long computeCt = System.currentTimeMillis();
        nodes.forEach(n -> n.evaluate(map));
        messageInfoLog("\n### EVALUATE > AVG EVALS", nodes);

        long evalCt = System.currentTimeMillis();
        for (int i = nodes.size() - 1; i >= 0; i--) nodes.get(i).backProp();
        messageInfoLog("\n### AVG EVALS & BACK PROPAGATION", nodes);
        min = getMinZeroExcluded(nodes);
        max = getMax(nodes);

        nodes.forEach(Node::forwardProp);
        nodes.forEach(n -> n.removeDuplicates(nodes));
        long backPropCt = System.currentTimeMillis();

        if (MSG_INFO || max > 95.0 || count + 5 > MAX_CYCLE) {
            System.out.println("\n### FORWARD PROPAGATION");
            System.out.println(nodes);
        }
        if (max < 100) cleanUp(nodes);
        System.out.println("\n###" + count + " CLEAN UP - max:" + max + " min:" + min + " exp:" + map.get(map.size() - 2).out + " " + map.getLast().out);
        messageInfoLog("", nodes);

        performanceInfos(cycleStartCt, initMapCt, computeCt, evalCt, backPropCt);
        return max;
    }

    public static void messageInfoLog(String x, List<Node> nodes) {
        if (MSG_INFO) {
            if (!x.isEmpty()) System.out.println(x);
            System.out.println(nodes);
        }
    }

    public static void performanceInfos(long cycleStartCt, long initMapCt, long computeCt, long evalCt, long backPropCt) {
        long endCycleCt = System.currentTimeMillis();
        long totalCt = endCycleCt - cycleStartCt;
        long cleanUpCt = endCycleCt - backPropCt;
        backPropCt -= evalCt;
        evalCt -= computeCt;
        computeCt -= initMapCt;
        initMapCt -= cycleStartCt;
        System.out.println("Cycle:" + totalCt + " initMap:" + initMapCt + " compute:" + computeCt + " eval:" + evalCt + " backProp:" + backPropCt + " cleanUp:" + cleanUpCt);
    }

    public static List<Node> initNodes() {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < NB_INPUT; i++) nodes.add(new Node(i)); //INPUT NODE
        for (int i = NB_INPUT; i < MAX_ID; i++) nodes.add(new Node(nodes, i)); //COMPUTE NODE
        //System.out.println("\n### NODES - nbNodes:" + nodes.size()); System.out.println(nodes);
        return nodes;
    }

    public static List<InOut> initMap(int nbIOEntry, int nbInputs) {
        List<InOut> map = new ArrayList<>();
        for (int j = 0; j < nbIOEntry; j++) map.add(new InOut(nbInputs));
        //System.outprintln("\n### THE MAP - nbTest:" + map.size()); System.out.println(map);
        return map;
    }

    public static void cleanUp(List<Node> nodes) {
        nodes.forEach(n -> n.cleanUp(NOISE_LIMIT));
        nodes.removeIf(n -> n.isCompute() && !n.asParent());
        nodes.forEach(Node::reset);
        for (int i = 0; i < MAX_ID; i++) {
            if (i < nodes.size()) nodes.get(i).id = i;
            else {
                nodes.add(new Node(nodes, i));
            }
        }
    }

    public static double getMinZeroExcluded(List<Node> nodes) {
        return nodes.stream().mapToDouble(Node::getAvgEval)
                .filter(value -> value != 0.0)
                .reduce(Double.MAX_VALUE, Double::min);
    }

    public static double getMax(List<Node> nodes) {
        return nodes.stream().mapToDouble(Node::getAvgEval)
                .reduce(Double.MIN_VALUE, Double::max);
    }
}
