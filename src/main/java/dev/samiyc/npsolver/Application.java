package dev.samiyc.npsolver;

import java.util.List;

import dev.samiyc.npsolver.bean.Node;
import dev.samiyc.npsolver.service.MainStaticService;
import dev.samiyc.npsolver.utils.NodePrinter;

public class Application {

    public static void main(String[] args) {
        int testId = 1;
        List<Node> nodes = MainStaticService.run(testId);
        NodePrinter.printLatestSolutionWithDependencies(nodes, testId);
    }
}// End of Application
