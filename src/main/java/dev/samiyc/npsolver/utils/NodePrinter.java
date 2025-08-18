package dev.samiyc.npsolver.utils;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.samiyc.npsolver.bean.Node;

/**
 * Small helper to print the latest 100%-scoring node and its dependencies,
 * sorted by id, plus simple edges.
 */
public final class NodePrinter {

    private NodePrinter() {
    }

    public static void printLatestSolutionWithDependencies(List<Node> nodes) {
        Optional<Node> latest = nodes.stream()
                .filter(n -> n.getAvgEval() == 100.0)
                .max(Comparator.comparingInt(n -> n.id));

        if (latest.isEmpty()) {
            System.out.println("No 100% node found.");
            return;
        } else {
            // Collect all contributing nodes (excluding the solution itself)
            Node sol = latest.get();
            Set<Node> ancestors = new HashSet<>();
            sol.collectAncestors(ancestors);

            // Order by id
            List<Node> byId = ancestors.stream()
                    .sorted(Comparator.comparingInt(n -> n.id))
                    .collect(Collectors.toList());

            System.out.println("-- Node Tree Solution --");
            System.out.println(metaString(nodes, sol));

            // Imprime les ancêtres en JSON
            byId.forEach(n -> System.out.println(n.toJsonString()));

            // Imprime aussi la solution en JSON
            System.out.println(sol.toJsonString());
            System.out.println();
        }
    }

    public static String metaString(List<Node> allNodes, Node solution) {
        try {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("sch", "np-solver/1.0");

            // Inputs A,B,C,D -> ids (on tente de les détecter, sinon fallback 0..3)
            Map<String, Integer> map = new LinkedHashMap<>();
            List<Node> inputs = allNodes.stream().filter(Node::isInput).toList();
            for (Node n : inputs)
                map.put(Node.toStrId(n), n.id);
            meta.put("inputs", map);

            // Opérateurs (math + booléen)
            meta.put("operators", Node.STR_OPERATOR.trim());
            meta.put("boolOp", "b");

            // Id de la solution (node max à 100%)
            if (solution != null) {
                meta.put("solution", solution.id);
            }

            // Date de génération
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            meta.put("generatedAt", formatter.format(new Date()));

            ObjectMapper om = new ObjectMapper();
            return om.writeValueAsString(meta);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
