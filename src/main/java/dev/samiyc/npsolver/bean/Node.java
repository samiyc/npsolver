package dev.samiyc.npsolver.bean;

import dev.samiyc.npsolver.service.EvaluationStaticService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.samiyc.npsolver.service.MainService.*;

public class Node {
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

    private List<Integer> checkIds(List<Node> nodes) {
        //Randomly choose a unique ida, idb and operator
        int count = 0, ida, idb, idRdc;
        boolean any;
        do {
            idRdc = id > 40 ? id / 8 : id - 1;
            ida = random.nextInt(idRdc);
            idb = random.nextInt(idRdc);
            if (ida == idb) idb++;
            op = random.nextInt(MAX_OP);

            //Duplicate ? retry 10x
            final int fida = ida, fidb = idb;
            any = nodes.stream().anyMatch(p -> p.nodeA != null && p.nodeB != null && p.op == op &&
                    ((p.nodeA.id == fida && p.nodeB.id == fidb) || (p.nodeA.id == fidb && p.nodeB.id == fida))
            );
        } while (
                any && ++count < 20
        );
        if (count > 19)
            throw new RuntimeException("WARNING CONFLIC i:" + id + " a:" + ida + " b:" + ida + " count:" + count);
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
        return bl.bool ? nt : new Value();
    }

    public void evaluate(List<InOut> map) {
        if (isLegitComputeWithParent()) {
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

    public static double calcAverage(List<Short> numbers) {
        if (numbers.isEmpty()) return 0.0;
        return numbers.stream()
                .mapToInt(Short::shortValue)
                .average().getAsDouble();
    }

    public void backProp() {
        if (isLegitComputeWithParent()) {
            double lowerLimit = this.avgEval - BACK_PROP_LOSS;
            List<Node> parents = Arrays.asList(this.nodeA, this.nodeB);

            for (Node p : parents) {
                if (p.isLegitComputeWithParent()) {
                    if (p.avgEval < lowerLimit) {
                        //Give credit to parents
                        p.avgEval = lowerLimit;
                        p.backProp();
                    }
                }
            }
        }
    }

    public void forwardPropChild() {
        if (isLegitComputeWithParent()) {
            List<Node> parents = Arrays.asList(this.nodeA, this.nodeB);

            for (Node p : parents) {
                if (p.isLegitComputeWithParent()) {
                    if (p.avgEval >= avgEval) {
                        //Better parent. remove the child(s)
                        prepareForDelete(25.25);
                    }
                }
            }
        }
    }

    public void removeDuplicates(List<Node> nodes) {
        if (isLegitComputeWithParent() && avgEval > 0.0 && avgEval < 80.0) {
            for (int i = id + 1; i < nodes.size(); i++) {
                Node n = nodes.get(i);
                if (n.isLegitComputeWithParent() && avgEval == n.avgEval) {
                    n.prepareForDelete(33.33);
                    n.childs = null;
                }
            }
        }
    }

    public void cleanUp(double min) {
        if (isLegitComputeWithParent() && avgEval < min) {
            prepareForDelete(44.44);
            childs = null;
        }
    }

    private void prepareForDelete(double og) {
        avgEval = og;
        nodeA = null;
        nodeB = null;
        outs = null;
        evals = null;
        if (childs != null) childs.forEach(n -> n.prepareForDelete(11.11));
    }

    public void reset() {
        //Reset outs for next simulation
        outs = new ArrayList<>();
        evals = new ArrayList<>();
    }

    @Override
    public String toString() {
        String ida = nodeA != null ? Integer.toString(nodeA.id) : "N";
        String idb = nodeB != null ? Integer.toString(nodeB.id) : "N";
        String src_op = isInput() || nodeA == null ? "---" : ida + " " + idb + " " + op;
        String outss = (outs == null || outs.isEmpty()) ? "---" : outs.get(outs.size() - 2) + " " + lastOut();
        return isCompute() && !asParent() ? "_" : id + "[" + src_op + "|" + outss + "|" + avgEval + "]"; //isCompute() && !asParent() ? "_" :
    }

    private void addChild(Node node) {
        if (isLegitComputeWithParent()) childs.add(node);
    }

    private boolean isLegitComputeWithParent() {
        return isCompute() && asParent();
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
