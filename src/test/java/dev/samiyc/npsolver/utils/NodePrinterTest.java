package dev.samiyc.npsolver.utils;

import dev.samiyc.npsolver.bean.Node;
import dev.samiyc.npsolver.service.MainStaticService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers Debug/Printer helper for showing latest 100% solution and its dependencies.
 * Requires a utility with: printLatestSolutionWithDependencies(List<Node>).
 */
public class NodePrinterTest {

    @Test
    void printLatestSolutionWithDependencies_prints_solution_and_ancestors() {
        int problemId = 5;
        List<Node> nodes = MainStaticService.run(problemId);

        Node expected = nodes.stream()
                .filter(n -> n.getAvgEval() >= 100.0)
                .max(Comparator.comparingInt(n -> n.id))
                .orElseThrow(() -> new AssertionError("No node at 100%"));

        // Capture stdout
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(bout));
        try {
            NodePrinter.printLatestSolutionWithDependencies(nodes, problemId);
        } finally {
            System.setOut(old);
        }

        String out = bout.toString();
        assertTrue(out.contains(String.valueOf(expected.id))
                || out.contains("id=" + expected.id), "Should mention solution id");

        // It should print some parents (can't know exact ids beforehand, but at least one)
        // We look for arrows or multiple lines to infer dependencies were printed.
        assertTrue(out.split("\\R").length > 3, "Should print multiple lines (solution + dependencies)");
    }
}
