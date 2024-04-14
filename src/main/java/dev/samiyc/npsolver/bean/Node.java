package dev.samiyc.npsolver.bean;

import dev.samiyc.npsolver.service.EvaluationStaticService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.samiyc.npsolver.service.MainService.GHOST_NODE_ALLOWED;
import static dev.samiyc.npsolver.service.MainService.random;

public class Node {
    public static final int BACK_PROP_LOSS = 5;
    private static final int MAX_OP = 9;

    public Node nodeA, nodeB;
    public int id, op;
    public double avgEval = 0.0;
    List<Value> outs;
    List<Integer> evals;
    List<Node> childs;

    /**
     * New Input nodes
     *
     * @param curId
     */
    public Node(int curId) {
        id = curId;
        op = MAX_OP;
        outs = new ArrayList<>();
        evals = new ArrayList<>();
        childs = new ArrayList<>();
    }

    /**
     * New Compute node
     *
     * @param nodes use to verif if node is unique
     * @param curId upper limit for node id as source
     */
    public Node(List<Node> nodes, int curId) {
        id = curId;
        outs = new ArrayList<>();
        evals = new ArrayList<>();
        childs = new ArrayList<>();
        List<Integer> ids = checkIds(nodes);
        nodeA = nodes.stream().filter(p -> p.id == ids.getFirst()).findFirst().get();
        nodeB = nodes.stream().filter(p -> p.id == ids.getLast()).findFirst().get();
        nodeA.addChild(this);
        nodeB.addChild(this);
    }

    private void addChild(Node node) {
        if (!GHOST_NODE_ALLOWED) childs.add(node);
    }

    public static double calcAverage(List<Integer> numbers) {
        if (numbers.isEmpty()) return 0.0;
        int sum = 0;
        for (int number : numbers) sum += number;
        return (double) sum / numbers.size();
    }

    private List<Integer> checkIds(List<Node> nodes) {
        //Randomly choose a unique ida, idb and operator

        int count = 0, ida, idb;
        boolean any;
        do {
            ida = random.nextInt(id - 1);
            idb = random.nextInt(id - 1);
            if (ida == idb) idb++;
            if (ida > idb) { //ida < idb only. Simplify duplicate checking
                int tmp = idb;
                idb = ida;
                ida = tmp;
            }
            op = random.nextInt(MAX_OP);

            //Duplicate ? retry 10x
            final int fida = ida, fidb = idb;
            any = nodes.stream().anyMatch(p ->
                    p.nodeA != null && p.nodeA.id == fida
                            && p.nodeB != null && p.nodeB.id == fidb && p.op == op);
        } while (
                any && ++count < 10
        );
        if (count > 9) throw new RuntimeException("WARNING CONFLIC i:" + id + " count:" + count);
        return Arrays.asList(ida, idb);
    }

    public void compute(InOut io) {
        if (isInput()) {
            outs.add(io.in.get(id));
        } else {
            Value a = nodeA.lastOut(), b = nodeB.lastOut();
            if (op == 0) outs.add(a.add(b));
            if (op == 1) outs.add(a.sup(b));
            if (op == 2) outs.add(b.sup(a));
            if (op == 3) outs.add(a.alternative(b));
            if (op == 4) outs.add(b.alternative(a));
            if (op == 5) outs.add(a.minus(b));
            if (op == 6) outs.add(b.minus(a));
            if (op == 7) outs.add(a.mult(b));
            if (op == 8) outs.add(a.eq(b));
        }
    }

    public void evaluate(List<InOut> map) {
        if (isCompute()) {
            Value lout = new Value(), lexp = new Value();
            for (int i = 0; i < outs.size(); i++) {
                Value out = outs.get(i);
                Value exp = map.get(i).out;
                Value outDif = out.minus(lout), expDif = exp.minus(lexp);

                evals.add(EvaluationStaticService.eval(out, exp, outDif, expDif));
                lout = out;
                lexp = exp;
            }
            avgEval = calcAverage(evals);
        }
    }

    public void backProp(List<Node> nodes) {
        if (isCompute() && asParent()) {
            double lowerLimit = this.avgEval - BACK_PROP_LOSS;
            List<Node> parents = Arrays.asList(this.nodeA, this.nodeB);

            for (Node p : parents) if (p.isCompute()) {
                if (p.avgEval < lowerLimit) {
                    //Give credit to parents
                    p.avgEval = lowerLimit;
                    p.backProp(nodes);
                } else if (p.avgEval > avgEval) {
                    //Better parent. remove the child(s)
                    prepareForDelete();
                }
            }
        }
    }

    public void cleanUp(double min) {
        if (isCompute() && avgEval <= min) {
            prepareForDelete();
        }
    }

    private void prepareForDelete() {
        avgEval = 0.0;
        nodeA = null;
        nodeB = null;
        childs.forEach(Node::prepareForDelete);
    }

    public void reset() {
        //Reset outs for next simulation
        outs = new ArrayList<>();
        evals = new ArrayList<>();
        avgEval = 0.0;
    }

    @Override
    public String toString() {
        String ida = nodeA != null ? Integer.toString(nodeA.id) : "N";
        String idb = nodeB != null ? Integer.toString(nodeB.id) : "N";
        String src_op = isInput() ? "---" : ida + " " + idb + " " + op;
        return isCompute()&&avgEval==0?"_": id + "[" + src_op + "|" + outs.get(outs.size() - 2) + " " + lastOut() + "|" + avgEval + "]";
    }

    private Value lastOut() {
        if (outs.isEmpty()) return new Value();
        return outs.getLast();
    }

    public boolean asParent() {
        return nodeA != null && nodeB != null;
    }

    public boolean isInput() {
        return op == MAX_OP;
    }

    public boolean isCompute() {
        return op != MAX_OP;
    }

    public double getAvgEval() {
        return avgEval;
    }
}
