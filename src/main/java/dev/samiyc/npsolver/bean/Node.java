package dev.samiyc.npsolver.bean;

import static dev.samiyc.npsolver.service.EvaluationStaticService.*;
import static dev.samiyc.npsolver.service.MainStaticService.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.samiyc.npsolver.service.EvaluationStaticService;
import dev.samiyc.npsolver.utils.OperatorEnum;

public class Node {
    public static final String STR_ABCD = "ABCD";
    public int id;
    public Node nodeA, nodeB;
    public OperatorEnum op;
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
        op = OperatorEnum.INPUT;
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
            idRdc = id > 20 + conflict ? 20 + conflict : id - 1;
            idRdc = id > MAX_ID / 2 ? id / 10 : idRdc;
            ida = random.nextInt(idRdc);
            idb = random.nextInt(idRdc);
            if (ida == idb)
                idb++;
            this.op = selectOperator(nodes, ida, idb);
            if (this.op == OperatorEnum.NOOP) {
                throw new RuntimeException("NOOP !!!");
            }

            // Duplicate ? retry 10x
            final int fida = ida, fidb = idb;
            any = nodes.stream().anyMatch(p -> p.nodeA != null && p.nodeB != null && p.op == op &&
                    ((p.nodeA.id == fida && p.nodeB.id == fidb) || (p.nodeA.id == fidb && p.nodeB.id == fida)));
        } while (any && ++conflict < 30);
        if (conflict > 29)
            throw new RuntimeException("WARNING CONFLIC i:" + id + " a:" + ida + " b:" + idb + " conflict:" + conflict);
        return Arrays.asList(ida, idb);
    }

    private OperatorEnum selectOperator(List<Node> nodes, int ida, int idb) {
        OperatorEnum operator;
        Node na = nodes.get(ida);
        Node nb = nodes.get(idb);
        if (na.op == null || nb.op == null) {
            throw new RuntimeException("NOOP !!!");
        } else if (na.op.isOutputTypeMath() && nb.op.isOutputTypeMath()) {
            operator = OperatorEnum.getRandomOpForInputType(OperatorEnum.Type.MATH);
        } else {
            operator = OperatorEnum.getRandomOpForInputType(OperatorEnum.Type.BOTH);// fix add BOOLEAN operator ?
        }
        return operator;
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
                if (op == OperatorEnum.ADD)
                    outs.add(a.add(b));
                else if (op == OperatorEnum.MINUS)
                    outs.add(a.minus(b));
                else if (op == OperatorEnum.MULT)
                    outs.add(a.mult(b));
                else if (op == OperatorEnum.MORE_THAN)
                    outs.add(a.sup(b));
                else if (op == OperatorEnum.ALT)
                    outs.add(a.alternative(b));
                else if (op == OperatorEnum.HYPOT)
                    outs.add(a.hypot(b));
                else if (op == OperatorEnum.MIN)
                    outs.add(a.min(b));
                else if (op == OperatorEnum.SQRT)
                    outs.add(a.sqrt());
                else if (op == OperatorEnum.ABS)
                    outs.add(a.abs());
            }
        }
    }

    private Value boolIntInteraction(Value bl, Value nt) {
        return bl.bool ? nt : new Value(); // fixme bool operator, invers etc
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
            chOp = op.getSymbol();
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
        return op == OperatorEnum.INPUT;
    }

    public boolean isCompute() {
        return op != OperatorEnum.INPUT;
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
            char chOp = op.getSymbol();
            m.put("op", chOp);
            Boolean onlyA = op.isUnary();
            m.put("onlyA", onlyA);
            if (onlyA) {
                m.put("label", chOp + " " + Node.toStrId(nodeA));
            } else {
                m.put("label", Node.toStrId(nodeA) + " " + chOp + " " + Node.toStrId(nodeB));
            }

            // Parents → IDs seulement (évite les cycles)
            List<Integer> parents = new ArrayList<>();
            boolean eqParent = false;

            if (this.nodeA != null) {
                parents.add(this.nodeA.id);
                // Comparaison des outs
                if (outsEqual(this.nodeA.outs, this.outs)) {
                    eqParent = true;
                }
            }
            if (this.nodeB != null && !onlyA) {
                parents.add(this.nodeB.id);
                // Comparaison des outs
                if (outsEqual(this.nodeB.outs, this.outs)) {
                    eqParent = true;
                }
            }

            m.put("parents", parents);
            m.put("matchParent", eqParent);

            // is constant ? toutes les sorties sont identiques
            m.put("constant", isConstantOuts());
            if (isConstantOuts()) {
                m.put("constantValue", String.valueOf(this.outs.get(0)));
            }

            // tag "asChildren" (a-t-il au moins un enfant effectif ?) ---
            m.put("asChildren", this.hasEffectiveChildren());
        }

        // outputs → les dernières valeurs en texte
        m.put("outputs", getLastXoutputsAsStrings(5));

        return m;
    }

    private List<String> getLastXoutputsAsStrings(int x) {
        if (this.outs == null || this.outs.isEmpty())
            return List.of();
        int n = this.outs.size();

        List<String> out = new ArrayList<>();
        for (int i = n - x; i < n; i++) {
            out.add(this.outs.get(Math.max(0, i)).toString());
        }
        return out;
    }

    /** Compare deux listes de Value élément par élément. */
    private static boolean outsEqual(List<Value> p, List<Value> c) {
        if (p == c)
            return true;
        if (p == null || c == null)
            return false;
        if (p.size() != c.size())
            return false;
        for (int i = 0; i < p.size(); i++) {
            if (!valueEquals(p.get(i), c.get(i)))
                return false;
        }
        return true;
    }

    /**
     * Égalité robuste pour une Value (null-safe).
     * Si Value.equals(...) est bien défini chez toi, il sera utilisé.
     * Sinon on retombe sur toString() pour éviter les surprises (true/N/num).
     */
    private static boolean valueEquals(Value a, Value b) {
        if (a == b)
            return true;
        if (a == null || b == null)
            return false;
        if (a.equals(b))
            return true;
        return String.valueOf(a).equals(String.valueOf(b));
    }

    /**
     * Noeud constant = toutes les valeurs out identiques (y compris 1 seul
     * élément).
     */
    private boolean isConstantOuts() {
        if (this.outs == null || this.outs.isEmpty())
            return false;
        return allOutsEqual(this.outs);
    }

    /** Toutes les sorties sont-elles identiques ? (liste vide -> false) */
    private static boolean allOutsEqual(List<Value> outs) {
        if (outs == null || outs.isEmpty())
            return false; // à toi de voir si 0 élément = constant
        Value first = outs.get(0);
        for (int i = 1; i < outs.size(); i++) {
            if (!valueEquals(first, outs.get(i)))
                return false;
        }
        return true;
    }

    public boolean hasEffectiveChildren() {
        boolean retour = false;
        for (Node child : this.childs)
            if (child.nodeA != null && child.nodeA.id == id || child.nodeB != null && child.nodeB.id == id) {
                retour = true;
            }
        return retour;
    }

    /**
     * Top-K valeurs les plus fréquentes dans une liste de Value
     * Null-safe, garde l'ordre d'apparition pour départager les ex-aequo.
     *
     * @param values
     * @param k
     * @return
     */
    private static List<String> mostCommon(List<Value> values, int k) {
        if (values == null || values.isEmpty() || k <= 0) {
            return java.util.Collections.emptyList();
        }

        // value -> [count, firstIndex]
        java.util.Map<String, int[]> freq = new java.util.LinkedHashMap<>();

        for (int i = 0; i < values.size(); i++) {
            String key = String.valueOf(values.get(i)).trim();
            int[] slot = freq.get(key);
            if (slot == null) {
                slot = new int[] { 0, i };
                freq.put(key, slot);
            }
            slot[0]++; // +1 occurrence
        }

        // Trie: count desc, puis firstIndex asc
        java.util.List<java.util.Map.Entry<String, int[]>> entries = new java.util.ArrayList<>(freq.entrySet());

        entries.sort((e1, e2) -> {
            int c = Integer.compare(e2.getValue()[0], e1.getValue()[0]); // count desc
            if (c != 0)
                return c;
            return Integer.compare(e1.getValue()[1], e2.getValue()[1]); // firstIndex asc
        });

        java.util.List<String> top = new java.util.ArrayList<>(Math.min(k, entries.size()));
        for (int i = 0; i < entries.size() && i < k; i++) {
            top.add(entries.get(i).getKey());
        }
        return top;
    }

    // Expose côté instance (solution.mostCommonOuts(5) pour la méta)
    public java.util.List<String> mostCommonOuts(int k) {
        return mostCommon(this.outs, k);
    }

}// End of Node
