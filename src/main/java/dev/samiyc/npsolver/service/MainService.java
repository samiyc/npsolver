package dev.samiyc.npsolver.service;

import dev.samiyc.npsolver.bean.InOut;
import dev.samiyc.npsolver.bean.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainService {
    private static final int MAX_ID = 20, NB_INPUT = 3;
    public static final boolean GHOST_NODE_ALLOWED = true;
    public static Random random = new Random();

    /**
     * Main method
     */
    public void run() {
        List<Node> nodes = initNodes();
        List<InOut> map = new ArrayList<>();

        double min = 0, max=0; int count = 0;
        while (max < 100 && count++ < 10000) {
            initMap(map, 100);

            for (InOut io : map) nodes.forEach(n -> n.compute(io));
            //System.out.println("\n### SIM > OUTS"); System.out.println(nodes);

            nodes.forEach(n -> n.evaluate(map));
            //System.out.println("\n### EVALUATE > EVALS"); System.out.println(nodes);

            nodes.forEach(n -> n.backProp(nodes));
            System.out.println("\n### AVG EVALS & BACK PROPAGATION");
            System.out.println(nodes);

            min = getMin(nodes);
            max = getMax(nodes);
            if (min >= max) System.out.println("WARNING: min > max");
            if (max < 100) cleanUp(nodes, min, max);
            System.out.println("\n###" + count + " CLEAN UP - max:" + max + " min:" + min + " exp:"+map.get(map.size()-2).out+" "+map.getLast().out); //System.out.println(nodes);
        }
        System.out.println();
    }

    private static List<Node> initNodes() {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < NB_INPUT; i++) nodes.add(new Node(i)); //INPUT NODE
        for (int i = NB_INPUT; i < MAX_ID; i++) nodes.add(new Node(nodes, i)); //COMPUTE NODE
        //System.out.println("\n### NODES - nbNodes:" + nodes.size()); System.out.println(nodes);
        return nodes;
    }

    private static void initMap(List<InOut> map, int nb_entry) {
        map.clear();
        for (int j = 0; j < nb_entry; j++) map.add(new InOut(NB_INPUT));
        //System.out.println("\n### THE MAP - nbTest:" + map.size()); System.out.println(map);
    }

    private static void cleanUp(List<Node> nodes, double min, double max) {
        if (min < max || GHOST_NODE_ALLOWED) nodes.forEach(n -> n.cleanUp(min));
        nodes.removeIf(n -> n.isCompute() && (!n.asParent() || n.avgEval == 0.0));
        nodes.forEach(Node::reset);
        for (int i = 0; i < MAX_ID; i++) {
            if (i < nodes.size()) nodes.get(i).id = i;
            else {
                nodes.add(new Node(nodes, i));
            }
        }
    }

    private static double getMin(List<Node> nodes) {
        return nodes.stream().mapToDouble(Node::getAvgEval)
                .filter(value -> value != 0.0)
                .reduce(Double.MAX_VALUE, Double::min);
    }

    private static double getMax(List<Node> nodes) {
        return nodes.stream().mapToDouble(Node::getAvgEval)
                .reduce(Double.MIN_VALUE, Double::max);
    }
}
