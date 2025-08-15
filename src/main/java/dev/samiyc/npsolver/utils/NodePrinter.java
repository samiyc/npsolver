package dev.samiyc.npsolver.utils;

import dev.samiyc.npsolver.bean.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Small helper to print the latest 100%-scoring node and its dependencies,
 * sorted by id, plus simple edges.
 */
public final class NodePrinter {

    private NodePrinter() {}

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
            byId.forEach(System.out::println);
            System.out.println(sol);
            System.out.println();
        }
    }
}
