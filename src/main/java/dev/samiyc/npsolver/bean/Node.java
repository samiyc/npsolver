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
import dev.samiyc.npsolver.utils.OperatorEnum.Type;

public class Node {

    public static final String STR_ABCD = "ABCD";

    public int id;
    public OperatorEnum op;
    public double avgEval = 0.0;

    // Nouvelles collections
    public List<Node> parents; // A puis B (si binaire)
    public List<Node> childs;

    public List<Value> outs;
    public List<Short> evals;
    public boolean isConstant = false;

    /**
     * INPUT node
     */
    public Node(int curId) {
        this.id = curId;
        this.op = OperatorEnum.INPUT;
        this.parents = new ArrayList<>(0);
        this.childs = new ArrayList<>();
        this.outs = new ArrayList<>();
        this.evals = new ArrayList<>();
    }

    /**
     * Compute node (random wiring + operator selection)
     * 
     * @param nodes pool pour choisir des parents existants
     * @param curId id de ce node
     * @param count unused ici (préservé pour signature)
     */
    public Node(List<Node> nodes, int curId, int count) {
        this.id = curId;
        this.parents = new ArrayList<>(2);
        this.childs = new ArrayList<>();
        this.outs = new ArrayList<>();
        this.evals = new ArrayList<>();

        List<Integer> ids = chooseParentIdsAndOperator(nodes);
        // Parent A
        Node na = nodes.get(ids.getFirst());
        this.parents.add(na);
        na.addChild(this);

        // Parent B (si binaire)
        if (!this.op.isUnary() && ids.getLast() != -1) {
            Node nb = nodes.get(ids.getLast());
            this.parents.add(nb);
            nb.addChild(this);
        }
    }

    /*
     * ========================= Core helpers (arity/parents)
     * ==========================
     */

    private int arity() {
        if (isInput())
            return 0;
        return op.isUnary() ? 1 : 2; // prêt pour >2 si un jour tu ajoutes de nouveaux opérateurs
    }

    public Node parent(int index) {
        return (parents != null && index >= 0 && index < parents.size()) ? parents.get(index) : null;
    }

    public Node parentA() {
        return parent(0);
    }

    public Node parentB() {
        return parent(1);
    }

    public boolean hasAllRequiredParents() {
        return parents != null && parents.size() >= arity();
    }

    public boolean isInput() {
        return op == OperatorEnum.INPUT;
    }

    public boolean isCompute() {
        return op != OperatorEnum.INPUT;
    }

    public boolean isComputeWithParent() {
        return isCompute() && hasAllRequiredParents();
    }

    /* ========================= Wiring & selection ========================== */

    private List<Integer> chooseParentIdsAndOperator(List<Node> nodes) {
        int conflict = 0, ida, idb, idRdc;
        boolean dup;
        do {
            idRdc = id > 20 + conflict*2 ? 20 + conflict*2 : id - 1;
            idRdc = id > MAX_ID / 2 ? id / 10 : idRdc;

            ida = random.nextInt(idRdc);
            idb = random.nextInt(idRdc);
            // if (ida == idb)
            //     idb++;

            this.op = selectOperator(nodes, ida, idb);
            if (this.op == OperatorEnum.NOOP) {
                throw new RuntimeException("NOOP !!!");
            }
            if (this.op.isUnary()) {
                idb = -1; // un seul parent
            }

            final int fida = ida, fidb = idb;
            // Duplicate detection (comme avant : ordre indifférent)
            dup = nodes.stream().anyMatch(p -> p != null &&
                    p.parents != null &&
                    p.op == this.op &&
                    p.parents.size() == (this.op.isUnary() ? 1 : 2) &&
                    sameParentsIgnoringOrder(p, fida, fidb));
        } while (dup && ++conflict < 30);

        if (conflict > 29)
            throw new RuntimeException("WARNING CONFLIC i:" + id + " a:" + ida + " b:" + idb + " conflict:" + conflict);

        return Arrays.asList(ida, idb);
    }

    private boolean sameParentsIgnoringOrder(Node p, int ida, int idb) {
        if (p.parents.isEmpty())
            return false;
        if (op.isUnary()) {
            return p.parents.get(0).id == ida;
        } else {
            int pa = p.parents.get(0).id;
            int pb = p.parents.get(1).id;
            return (pa == ida && pb == idb) || (pa == idb && pb == ida);
        }
    }

    private OperatorEnum selectOperator(List<Node> nodes, int ida, int idb) {
        List<OperatorEnum> excluded = new ArrayList<>();
        Node na = nodes.get(ida);
        Node nb = nodes.get(idb);

        if (na.id != ida || nb.id != idb)
            throw new RuntimeException("GET(i) KO !!");
        if (na.op == null || nb.op == null)
            throw new RuntimeException("NOOP !!!");
        if (na.isInput() || na.parentA().isInput())
            excluded.add(OperatorEnum.CORRUPTED_GATE);
        
        if (na.op.isOutputType(Type.MATH) && nb.op.isOutputType(Type.MATH)) {
            return OperatorEnum.randomOpOfInputType(OperatorEnum.Type.MATH, excluded);
        } else if (na.op.isOutputType(Type.BOOLEAN) && nb.op.isOutputType(Type.BOOLEAN)) {
            return OperatorEnum.randomOpOfInputType(OperatorEnum.Type.BOOLEAN, null);
        }
        return OperatorEnum.BOOL_INT;
    }

    public void addChild(Node node) {
        if (childs == null)
            childs = new ArrayList<>();
        childs.add(node);
    }

    /* ========================= Compute / Evaluate ========================== */

    public void compute(InOut io, Value coruptedValue) {
        if (isConstant) return;
        if (isInput()) {
            outs.add(io.in.get(id));
            return;
        }

        if (!isComputeWithParent()) {
            throw new RuntimeException("Missing parent(s) for compute node id=" + id + " op=" + op);
        }

        // Récup des sorties parents
        Node na = parentA();
        Node nb = parentB();
        if (na.outs == null) {
            throw new RuntimeException("Empty outs ! this:" + this + " a:" + na + " b:" + nb);
        }
        if (!op.isUnary() && parentB().outs == null) {
            throw new RuntimeException("parentB() NULL ! this:" + this + " a:" + na + " b:" + nb);
        }
        Value a = coruptedValue != null ? coruptedValue : safeLastOut(parentA(), "A");
        Value b = op.isUnary() ? null : safeLastOut(parentB(), "B");

        switch (op) {
            case ABS -> outs.add(a.abs());
            case SQRT -> outs.add(a.sqrt());

            case ADD -> outs.add(a.add(b));
            case MINUS -> outs.add(a.minus(b));
            case MULT -> outs.add(a.mult(b));
            case DIV -> outs.add(a.div(b));
            case HYPOT -> outs.add(a.hypot(b));
            case MIN -> outs.add(a.min(b));
            case MODULO -> outs.add(a.modulo(b));
            
            case MORE_THAN -> outs.add(a.sup(b));
            case BOOL_INT -> outs.add(a.boolIntInteraction(b));
            case ALT -> outs.add(a.alternative(b));
            
            case AND -> outs.add(a.and(b));
            case OR -> outs.add(a.or(b));
            case XOR -> outs.add(a.xor(b));

            case CORRUPTED_GATE -> this.currupt();
            case BIN_LSHIFT -> outs.add(a.lshift(b));
            case BIN_RSHIFT -> outs.add(a.rshift(b));
            case BIN_AND -> outs.add(a.binAnd(b));

            default -> throw new RuntimeException("OP not supported op:" + op + " a:" + a + " b:" + b);
        }
    }

    private void currupt() {
        if (parentA().lastOut() == null) return; // Too soon
        if (parentA().isInput()) throw new RuntimeException("I CAN'T CORRUPT THAT !!! (input)");

        List<Node> familyLine = getLifeLine();
        if (familyLine.isEmpty()) throw new RuntimeException("I CAN'T CORRUPT THAT !!! (emptyLine)");
        familyLine = new ArrayList<>(familyLine.reversed());
        familyLine.forEach(n -> n.outs.removeLast());
        
        // TRUNK
        Node trunk = familyLine.removeFirst();
        trunk.parents.add(this);
        trunk.compute(null, parentB().lastOut());
        // RECALCULATE LINE
        if (!familyLine.isEmpty()) familyLine.forEach(n -> compute(null, null));
    }

    private List<Node> getLifeLine() {
        List<Node> out = new ArrayList<>();
        Node cur = this;
        for (int i = 0; i < 5; i++) {
            Node pa = cur.parentA();
            if (pa.isInput()) return out;
            cur = pa;
            out.add(cur);
        }
        return out;
    }

    private Value safeLastOut(Node p, String label) {
        if (p == null || p.outs == null)
            throw new RuntimeException("Parent " + label + " issue !!! (null outs) node=" + id);
        return p.lastOut();
    }

    public void evaluate(List<InOut> map) {
        if (!isComputeWithParent())
            return;

        Value out, exp, lout = new Value(), lexp = new Value();
        for (int i = 0; i < outs.size(); i++) {
            out = outs.get(i);
            exp = map.get(i).out;
            evals.add(EvaluationStaticService.eval(out, exp, out.minus(lout), exp.minus(lexp)));
            lout = out;
            lexp = exp;
        }
        avgEval = calcAverage(evals);
        this.isConstant = isConstantOuts();
    }

    public static double calcAverage(List<Short> numbers) {
        if (numbers == null || numbers.isEmpty())
            return 0.0;
        return numbers.stream().mapToInt(Short::shortValue).average().orElse(0.0);
    }

    /*
     * ========================= Propagation & GC logique ==========================
     */

    public void backProp() {
        if (!isComputeWithParent())
            return;
        for (Node p : parents) {
            if (p != null && p.isComputeWithParent()) {
                if (p.avgEval < this.avgEval) {
                    p.avgEval = this.avgEval; // credit aux parents
                    p.backProp();
                }
            }
        }
    }

    public void forwardProp() {
        if (!isComputeWithParent())
            return;
        for (Node p : parents) {
            if (p != null && p.isComputeWithParent() && p.avgEval > avgEval) {
                // parent meilleur → ce node devient redondant
                prepareForDelete(11.11);
                break;
            }
        }
    }

    public void removeDuplicates(List<Node> nodes) {
        if (!isComputeWithParent() || avgEval <= 0.0 || avgEval >= 100.0)
            return;

        for (int i = id + 1; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            if (n.id != i)
                throw new RuntimeException("GET(i) KO !!");
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

    private void connectChildToGrandParentAndClean(Node redundant) {
        // rattache ses enfants à "this"
        if (redundant.childs != null) {
            for (Node c : redundant.childs) {
                replaceParent(c, redundant, this);
                this.addChild(c);
            }
        }
        // coupe le redundant
        if (this.childs != null)
            this.childs.remove(redundant);
        redundant.childs = null; // ignore ses enfants désormais
        redundant.prepareForDelete(33.33);
    }

    private static void replaceParent(Node child, Node oldP, Node newP) {
        if (child.parents == null)
            return;
        for (int i = 0; i < child.parents.size(); i++) {
            if (child.parents.get(i) == oldP) {
                child.parents.set(i, newP);
            }
        }
    }

    public void cleanUp(double min) {
        Node matchingParentNode = getEqualParentIfPresent();
        if (matchingParentNode != null) {
            mapChildToGrandParentThenDelete(matchingParentNode);
        } else if (isComputeWithParent() && avgEval < min) {
            prepareForDelete(44.44);
        }
    }

    private void mapChildToGrandParentThenDelete(Node matchingParentNode) {
        this.childs.forEach(c -> {
            if (c.parents != null && !c.parents.isEmpty()) {
                if (!c.parents.contains(this))
                    throw new RuntimeException("Child not connected !");
                c.parents.set(c.parents.indexOf(this), matchingParentNode);
                if (!matchingParentNode.childs.contains(c))
                    matchingParentNode.addChild(c);
            }
        });
        this.parents.forEach(p -> p.childs.remove(this));
        this.parents.clear();
        this.childs.clear();
        this.outs.clear();
        this.evals.clear();
        this.parents = null;
        this.childs = null;
        this.outs = null;
        this.evals = null;
        this.avgEval = 55.55;
    }

    private void prepareForDelete(double og) {
        removeChilds(og);
        childs = null;
    }

    private void removeChilds(double og) {
        avgEval = og;
        if (parents != null) {
            parents.clear();
            parents = null;
        }
        outs = null;
        evals = null;
        if (childs != null)
            childs.forEach(n -> n.removeChilds(22.22));
    }

    public void reset() {
        if (isConstant) return;

        // Reset outs pour la prochaine simulation
        if (outs == null)
            outs = new ArrayList<>();
        else
            outs.clear();

        if (evals == null)
            evals = new ArrayList<>();
        else
            evals.clear();
    }

    /* ========================= Introspection / JSON ========================== */

    @Override
    public String toString() {
        String nodeSrcAndOp = "---";
        if (!isInput() && hasAllRequiredParents()) {
            char chOp = op.getSymbol();
            String ida = toStrId(parentA());
            String idb = op.isUnary() ? "N" : toStrId(parentB());
            nodeSrcAndOp = ida + chOp + idb;
        }
        String outss = "---";
        if (outs != null && outs.size() >= 2) {
            outss = outs.get(outs.size() - 2) + " " + lastOut();
        } else if (outs != null && outs.size() == 1) {
            outss = lastOut().toString();
        }
        String strEval = avgEval == VALUE_FOUND ? ">> " + avgEval + " <<" : "" + avgEval;

        return !MSG_INFO && isCompute() && !hasAllRequiredParents() ? "_"
                : id + "[" + nodeSrcAndOp + "|" + outss + "|" + strEval + "]";
    }

    public static String toStrId(Node n) {
        if (n == null)
            return "N";
        return n.id < NB_INPUT ? "" + STR_ABCD.charAt(n.id) : Integer.toString(n.id);
    }

    public Value lastOut() {
        if (outs == null || outs.isEmpty())
            return new Value();
        return outs.get(outs.size() - 1);
    }

    public double getAvgEval() {
        return avgEval;
    }

    public void collectAncestors(java.util.Set<Node> acc) {
        if (parents == null)
            return;
        for (Node p : parents) {
            if (p != null && acc.add(p))
                p.collectAncestors(acc);
        }
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

        if (isInput()) {
            m.put("name", toStrId(this));
        } else {
            char chOp = op.getSymbol();
            boolean onlyA = op.isUnary();
            m.put("op", chOp);
            m.put("onlyA", onlyA);

            String label;
            if (onlyA) {
                label = chOp + " " + toStrId(parentA());
            } else {
                label = toStrId(parentA()) + " " + chOp + " " + toStrId(parentB());
            }
            m.put("label", label);
            m.put("parents", getParentIds());
            m.put("matchParent", isEqualParent());

            m.put("constant", isConstantOuts());
            if (isConstantOuts())
                m.put("constantValue", String.valueOf(this.outs.get(0)));

            m.put("asChildren", hasEffectiveChildren());
        }

        m.put("outputs", getLastXoutputsAsStrings(5));
        return m;
    }

    private List<Integer> getParentIds() {
        List<Integer> pids = new ArrayList<>();
        if (parentA() != null) {
            pids.add(parentA().id);
        }
        if (!op.isUnary() && parentB() != null) {
            pids.add(parentB().id);
        }
        return pids;
    }

    public boolean isEqualParent() {
        return getEqualParentIfPresent() != null;
    }

    public Node getEqualParentIfPresent() {
        if (parentA() != null) {
            if (outsEqual(parentA().outs, this.outs))
                return parentA();
        }
        if (!op.isUnary() && parentB() != null) {
            if (outsEqual(parentB().outs, this.outs))
                return parentB();
        }
        return null;
    }

    private List<String> getLastXoutputsAsStrings(int x) {
        if (this.outs == null || this.outs.isEmpty())
            return List.of();
        int n = this.outs.size();
        List<String> out = new ArrayList<>();
        for (int i = Math.max(0, n - x); i < n; i++)
            out.add(this.outs.get(i).toString());
        return out;
    }

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

    private static boolean valueEquals(Value a, Value b) {
        if (a == b)
            return true;
        if (a == null || b == null)
            return false;
        if (a.equals(b))
            return true;
        return String.valueOf(a).equals(String.valueOf(b));
    }

    public boolean isConstantOuts() {
        if (this.outs == null || this.outs.isEmpty())
            return false;
        return allOutsEqual(this.outs);
    }

    private static boolean allOutsEqual(List<Value> outs) {
        if (outs == null || outs.isEmpty())
            return false;
        Value first = outs.get(0);
        for (int i = 1; i < outs.size(); i++) {
            if (!valueEquals(first, outs.get(i)))
                return false;
        }
        return true;
    }

    public boolean hasEffectiveChildren() {
        if (this.childs == null)
            return false;
        for (Node child : this.childs) {
            if (child != null && child.parents != null && child.parents.contains(this)) {
                return true;
            }
        }
        return false;
    }

    /* ========================= Meta ========================== */

    public java.util.List<String> mostCommonOuts(int k) {
        return mostCommon(this.outs, k);
    }

    private static List<String> mostCommon(List<Value> values, int k) {
        if (values == null || values.isEmpty() || k <= 0)
            return java.util.Collections.emptyList();
        java.util.Map<String, int[]> freq = new java.util.LinkedHashMap<>();
        for (int i = 0; i < values.size(); i++) {
            String key = String.valueOf(values.get(i)).trim();
            int[] slot = freq.get(key);
            if (slot == null) {
                slot = new int[] { 0, i };
                freq.put(key, slot);
            }
            slot[0]++;
        }
        java.util.List<java.util.Map.Entry<String, int[]>> entries = new java.util.ArrayList<>(freq.entrySet());
        entries.sort((e1, e2) -> {
            int c = Integer.compare(e2.getValue()[0], e1.getValue()[0]);
            if (c != 0)
                return c;
            return Integer.compare(e1.getValue()[1], e2.getValue()[1]);
        });
        java.util.List<String> top = new java.util.ArrayList<>(Math.min(k, entries.size()));
        for (int i = 0; i < entries.size() && i < k; i++)
            top.add(entries.get(i).getKey());
        return top;
    }
}
