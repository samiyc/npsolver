package dev.samiyc.npsolver.bean;

import static dev.samiyc.npsolver.service.EvaluationStaticService.VALUE_FOUND;
import static dev.samiyc.npsolver.service.MainStaticService.MAX_ID;
import static dev.samiyc.npsolver.service.MainStaticService.MAX_OP;
import static dev.samiyc.npsolver.service.MainStaticService.MSG_INFO;
import static dev.samiyc.npsolver.service.MainStaticService.NB_INPUT;
import static dev.samiyc.npsolver.service.MainStaticService.random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.samiyc.npsolver.service.EvaluationStaticService;

public class Node {
    public static final String STR_ABCD = "ABCD";
    public static final String STR_OPERATOR = "+-x>:hl#a ";
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
        nodeA = this;
        nodeB = this;
    }

    /**
     * New Compute node
     *
     * @param nodes use to verif if node is unique
     * @param curId upper limit for node id as source
     */
    public Node(List<Node> nodes, int curId, int count) {
        id = curId;
        outs = new ArrayList<>();
        evals = new ArrayList<>();
        childs = new ArrayList<>();
        List<Integer> ids = checkIds(nodes, count);
        nodeA = nodes.stream().filter(p -> p.id == ids.getFirst()).findFirst().get();
        nodeB = nodes.stream().filter(p -> p.id == ids.getLast()).findFirst().get();
        nodeA.addChild(this);
        nodeB.addChild(this);
    }

    private List<Integer> checkIds(List<Node> nodes, int count) {
        // Randomly choose a unique ida, idb and operator
        int conflict = 0, ida, idb, idRdc;
        boolean any;
        do {
            idRdc = id > 20 ? 20 + conflict : id - 1;
            idRdc = id > MAX_ID / 2 ? id / 10 : idRdc;
            ida = random.nextInt(idRdc);
            idb = random.nextInt(idRdc);
            if (ida == idb)
                idb++;
            op = random.nextInt(count % 1000 < 100 ? 3 : MAX_OP);

            // Duplicate ? retry 10x
            final int fida = ida, fidb = idb;
            any = nodes.stream().anyMatch(p -> p.nodeA != null && p.nodeB != null && p.op == op &&
                    ((p.nodeA.id == fida && p.nodeB.id == fidb) || (p.nodeA.id == fidb && p.nodeB.id == fida)));
        } while (any && ++conflict < 30);
        if (conflict > 29)
            throw new RuntimeException("WARNING CONFLIC i:" + id + " a:" + ida + " b:" + ida + " conflict:" + conflict);
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
                if (op == 0)      outs.add(a.add(b));
                else if (op == 1) outs.add(a.minus(b));
                else if (op == 2) outs.add(a.mult(b));
                else if (op == 3) outs.add(a.sup(b));
                else if (op == 4) outs.add(a.alternative(b));
                else if (op == 5) outs.add(a.hypot(b));
                else if (op == 6) outs.add(a.min(b));
                else if (op == 7) outs.add(a.sqrt());
                else if (op == 8) outs.add(a.abs());
            }
        }
    }

    private Value boolIntInteraction(Value bl, Value nt) {
        return bl.bool == (op >= MAX_OP / 2) ? nt : new Value();
    }

    public void evaluate(List<InOut> map) {
        if (isComputeWithParent()) {
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
        if (numbers.isEmpty())
            return 0.0;
        return numbers.stream()
                .mapToInt(Short::shortValue)
                .average().getAsDouble();
    }

    public void backProp() {
        if (isComputeWithParent()) {
            for (Node p : Arrays.asList(this.nodeA, this.nodeB)) {
                if (p.isComputeWithParent()) {
                    if (p.avgEval < this.avgEval) {
                        // Give credit to parents
                        p.avgEval = this.avgEval;
                        p.backProp();
                    }
                }
            }
        }
    }

    public void forwardProp() {
        if (isComputeWithParent()) {
            for (Node p : Arrays.asList(this.nodeA, this.nodeB)) {
                if (p.isComputeWithParent() && p.avgEval > avgEval) {
                    // Better parent. remove the child(s)
                    prepareForDelete(11.11);
                }
            }
        }
    }

    public void removeDuplicates(List<Node> nodes) {
        if (isComputeWithParent() && avgEval > 0.0 && avgEval < 100.0) {
            for (int i = id + 1; i < nodes.size(); i++) {
                Node n = nodes.get(i);
                if (n.outs != null && n.isComputeWithParent() && n.avgEval == avgEval) {
                    int j = 0;
                    while (j < n.outs.size() && outs.get(j).equals(n.outs.get(j)))
                        j++;
                    if (j >= n.outs.size()) {
                        connectChildToGrandParentAndClean(n);
                    }
                }
            }
        }
    }

    private void connectChildToGrandParentAndClean(Node p) {
        // Connect childs to gand parent (this)
        childs.addAll(p.childs);
        // Update child upper links to parent
        for (Node c : p.childs) {
            if (c.nodeA == p)
                c.nodeA = this;
            if (c.nodeB == p)
                c.nodeB = this;
        }
        // Delete
        childs.remove(p);
        p.childs = null; // Ignore the childs in this case
        p.prepareForDelete(33.33);
    }

    public void cleanUp(double min) {
        if (isComputeWithParent() && avgEval < min) {
            prepareForDelete(44.44);
        }
    }

    private void prepareForDelete(double og) {
        removeChilds(og);
        childs = null;
    }

    private void removeChilds(double og) {
        avgEval = og;
        nodeA = null;
        nodeB = null;
        outs = null;
        evals = null;
        if (childs != null)
            childs.forEach(n -> n.removeChilds(22.22));
    }

    public void reset() {
        // Reset outs for next simulation
        outs = new ArrayList<>();
        evals = new ArrayList<>();
    }

    @Override
    public String toString() {
        char chOp;
        String nodeSrcAndOp = "---";
        if (nodeA != null) {
            chOp = nodeA.lastOut().isBool() || nodeB.lastOut().isBool() ? 'b' : STR_OPERATOR.charAt(op);
            String ida = toStrId(nodeA);
            String idb = toStrId(nodeB);
            nodeSrcAndOp = ida + chOp + idb;
        }
        String outss = "---";
        if (outs != null && !outs.isEmpty()) {
            outss = outs.get(outs.size() - 2) + " " + lastOut();
        }
        String strEval = avgEval == VALUE_FOUND ? ">> " + avgEval + " <<" : "" + avgEval;
        return !MSG_INFO && isCompute() && !asParent() ? "_"
                : id + "[" + nodeSrcAndOp + "|" + outss + "|" + strEval + "]";
    }

    public static String toStrId(Node n) {
        if (n == null)
            return "N";
        return n.id < NB_INPUT ? "" + STR_ABCD.charAt(n.id) : Integer.toString(n.id);
    }

    public void addChild(Node node) {
        if (isComputeWithParent())
            childs.add(node);
    }

    public boolean isComputeWithParent() {
        return isCompute() && asParent();
    }

    public Value lastOut() {
        if (outs.isEmpty())
            return new Value();
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

    public void collectAncestors(java.util.Set<Node> acc) {
        if (nodeA != null && acc.add(nodeA))
            nodeA.collectAncestors(acc);
        if (nodeB != null && acc.add(nodeB))
            nodeB.collectAncestors(acc);
    }

    public String toJsonString() {
        try {
            ObjectMapper om = new ObjectMapper();
            return om.writeValueAsString(toJsonDto());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> toJsonDto() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", this.id);
        m.put("kind", isInput() ? "input" : "op");

        // name pour inputs A/B/C/D si utile
        if (isInput()) {
            // adapte si tu as déjà un champ name; sinon calcule-le
            m.put("name", toStrId(this));
        } else {
            // expr lisible (ex: "B a A") ou raw (ex: "BaA") – ajuste selon tes champs
            char chOp = nodeA.lastOut().isBool() || nodeB.lastOut().isBool() ? 'b' : STR_OPERATOR.charAt(op);
            m.put("op", chOp);
            Boolean onlyA = "#a".contains("" + chOp);
            m.put("onlyA", onlyA);
            if (onlyA) {
                m.put("label", chOp + " " + Node.toStrId(nodeA));
            } else {
                m.put("label", Node.toStrId(nodeA) + " " + chOp + " " + Node.toStrId(nodeB));
            }

            // parents → IDs seulement (évite les cycles)
            List<Integer> parents = new ArrayList<>();
            if (this.nodeA != null)
                parents.add(this.nodeA.id);
            if (this.nodeB != null)
                parents.add(this.nodeB.id);
            // si tu as plus de 2 sources, ajoute-les ici
            m.put("parents", parents);
        }

        // outputs → les 2 dernières valeurs en CHAÎNES
        m.put("outputs", getLastTwoOutputsAsStrings());

        return m;
    }

    private List<String> getLastTwoOutputsAsStrings() {
        if (this.outs == null || this.outs.isEmpty())
            return List.of();
        int n = this.outs.size();
        // convertis en String (true/N/num → toString)
        return List.of(this.outs.get(Math.max(0, n - 2)).toString(),
                this.outs.get(n - 1).toString());
    }

}// End of Node
