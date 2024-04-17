package dev.samiyc.npsolver.bean;

import dev.samiyc.npsolver.service.EvaluationStaticService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.samiyc.npsolver.service.MainService.random;

public class Node {
    private static final int BACK_PROP_LOSS = 2;
    private static final int MAX_OP = 6;

    public Node nodeA, nodeB;
    public int id, op;
    public double avgEval = 0.0;
    public List<Value> outs;
    public List<Short> evals;
    public List<Node> childs;

    /**
     * New Input nodes
     *
     * @param curId
     */
    public Node(int curId) {
        id = curId;
        op = MAX_OP;
        outs = new ArrayList<>();
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

    public static double calcAverage(List<Short> numbers) {
        if (numbers.isEmpty()) return 0.0;
        return numbers.stream()
                .mapToInt(Short::shortValue)
                .average().getAsDouble();
    }

    private void addChild(Node node) {
        if (isCompute()) childs.add(node);
    }

    private List<Integer> checkIds(List<Node> nodes) {
        //Randomly choose a unique ida, idb and operator
        int count = 0, ida, idb;
        boolean any;
        do {
            ida = random.nextInt(id - 1);
            idb = random.nextInt(id - 1);
            if (ida == idb) idb++;
            op = random.nextInt(MAX_OP);

            //Duplicate ? retry 10x
            final int fida = ida, fidb = idb;
            any = nodes.stream().anyMatch(p ->
                    p.nodeA != null && p.nodeB != null && (
                            (p.nodeA.id == fida && p.nodeB.id == fidb)
                                    || (p.nodeA.id == fidb && p.nodeB.id == fida))
                            && p.op == op);
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
            if (a.isBool() && b.isInt()) {
                outs.add(boolIntInteraction(a, b));
            } else if (b.isBool() && a.isInt()) {
                outs.add(boolIntInteraction(b, a));
            } else {
                if (op == 0) outs.add(a.add(b));
                else if (op == 1) outs.add(a.sup(b));
                else if (op == 2) outs.add(a.alternative(b));
                else if (op == 3) outs.add(a.minus(b));
                else if (op == 4) outs.add(a.mult(b));
                else if (op == 5) outs.add(a.eq(b));
            }
        }
    }

    private Value boolIntInteraction(Value bl, Value nt) {
        return bl.bool == (op < MAX_OP / 2) ? nt : new Value();
    }

    public void evaluate(List<InOut> map) {
        if (isCompute()) {
            Value out, exp, lout = new Value(), lexp = new Value();
            for (int i = 0; i < outs.size(); i++) {
                out = outs.get(i);
                exp = map.get(i).out;

                evals.add(EvaluationStaticService.eval(out, exp, out.minus(lout), exp.minus(lexp)));
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

            for (Node p : parents) {
                if (p.isCompute()) {
                    if (p.avgEval < lowerLimit) {
                        //Give credit to parents
                        p.avgEval = lowerLimit;
                        p.backProp(nodes);
                    } else if (p.avgEval >= avgEval) {
                        //Better parent. remove the child(s)
                        prepareForDelete();
                    }
                }
            }
        }
    }

    public void cleanUp(double min) {
        if (isCompute() && avgEval <= min) {
            prepareForDelete();
            childs = null;
        }
    }

    private void prepareForDelete() {
        avgEval = 0.0;
        nodeA = null;
        nodeB = null;
        outs = null;
        evals = null;
        if (childs != null) childs.forEach(Node::prepareForDelete);
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
        return isCompute() && avgEval == 0 ? "_" : id + "[" + src_op + "|" + outs.get(outs.size() - 2) + " " + lastOut() + "|" + avgEval + "]";
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
