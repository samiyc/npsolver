package dev.samiyc.npsolver.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Node {
    private static final int MAX_OP = 8;
    public Node nodeA, nodeB;
    public int id, op;
    public double avgEval = 0.0;
    List<Value> outs;
    List<Integer> evals;
    List<Node> childs;

    //New Input nodes
    public Node(String s, int curId) {
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

    public static int baseEval(int valOut, int valExp, int valOutDif, int valExpDif) {
        return baseEval(new Value(valOut), new Value(valExp), new Value(valOutDif), new Value(valExpDif));
    }

    public static int baseEval(Value valOut, Value valExp, Value valOutDif, Value valExpDif) {

        int eval = 0;
        if (valOut.isBool() && valExp.isBool()) {
            if (valOut.bool == valExp.bool) eval = 100;
        } else {
            int out = valOut.number, exp = valExp.number, outDif = valOutDif.number, expDif = valExpDif.number;
            if (outDif == expDif) eval += 5; //Comparaison des delta n avec n-1
            if ((outDif >= 0 && expDif >= 0) || (outDif < 0 && expDif < 0)) eval += 5; //Verification du signe du delta
            if (out / 10 == exp / 10) eval += 5;
            if (out / 100 == exp / 100) eval += 5;
            if (out / 1000 == exp / 1000) eval += 5;
            if ((out >= 0 && exp >= 0) || (out < 0 && exp < 0)) eval += 5; //Vérification du signe
            if (out != 0 && exp % out == 0) eval += 5;   //exp multiple de out
            if (exp != 0 && out % exp == 0) eval += 5;   //out multiple de out
            if (out % 2 == 0 && exp % 2 == 0) eval += 5; //Multiple de 2
            if (out % 3 == 0 && exp % 3 == 0) eval += 5; //Multiple de 3
            if (out == exp) eval = 100;
        }

        return eval;
    }

    public static double calcAverage(List<Integer> numbers) {
        if (numbers.isEmpty()) return 0.0;
        int sum = 0;
        for (int number : numbers) sum += number;
        return (double) sum / numbers.size();
    }

    private void addChild(Node node) {
        childs.add(node);
    }

    private List<Integer> checkIds(List<Node> nodes) {
        //Randomly choose a unique ida, idb and operator
        Random random = new Random();
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
        if (op == MAX_OP) {
            outs.add(io.in.get(id));
        } else {
            Value a = nodeA.lastOut(), b = nodeB.lastOut();
            if (op == 0) outs.add(a.add(b));   //A+B
            if (op == 1) outs.add(a.inf(b));   //Supp & Inf
            if (op == 2) outs.add(b.inf(a));
            if (op == 3) outs.add(b.alternative(a));  //Replace 0 by
            if (op == 4) outs.add(a.alternative(b));
            if (op == 5) outs.add(a.mult(b));
            if (op == 6) outs.add(a.minus(b));
            if (op == 7) outs.add(b.minus(a));
        }
    }

    public void evaluate(List<InOut> map) {
        if (isCompute()) {
            Value lout = new Value(), lexp = new Value();
            for (int i = 0; i < outs.size(); i++) {
                Value out = outs.get(i);
                Value exp = map.get(i).out;
                Value outDif = out.minus(lout), expDif = exp.minus(lexp);

                evals.add(baseEval(out, exp, outDif, expDif));
                lout = out;
                lexp = exp;
            }
            avgEval = calcAverage(evals);
        }
    }

    public void backProp(List<Node> nodes) {
        if (isCompute() && asParent()) {
            double lowerLimit = this.avgEval - 10;
            List<Node> parents = Arrays.asList(this.nodeA, this.nodeB);

            //Give credit to parents -10pts
            for (Node n : parents)
                if (n.isCompute() && n.avgEval < lowerLimit) {
                    n.avgEval = lowerLimit;
                    n.backProp(nodes);
                }
            //Better parent. remove the child...
            for (Node n : parents)
                if (n.avgEval > this.avgEval) {
                    prepareForDelete();
                }
        }
    }

    public void cleanUp(double min) {
        if (isCompute() && avgEval <= min) {
            prepareForDelete();
        }
    }

    private void prepareForDelete() {
        this.avgEval = 0.0;
        nodeA = null;
        nodeB = null;
        childs.forEach(n -> n.prepareForDelete());
    }

    public void reset() {
        //Reset outs for next simulation
        outs = new ArrayList<>();
        evals = new ArrayList<>();
        avgEval = 0.0;
    }

    @Override
    public String toString() {
        int ida = nodeA != null ? nodeA.id : 0;
        int idb = nodeB != null ? nodeB.id : 0;
        return "[id:" + id + " in:" + ida + " " + idb + " " + op + " ev:" + avgEval + "]";
    }

    private Value lastOut() {
        if (outs.isEmpty()) return new Value();
        return outs.get(outs.size() - 1);
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
