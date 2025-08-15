package dev.samiyc.npsolver;

import java.util.List;

import dev.samiyc.npsolver.bean.Node;
import dev.samiyc.npsolver.service.MainStaticService;
import dev.samiyc.npsolver.utils.NodePrinter;

public class Application {

    public static void main(String[] W) {
        List<Node> nodes = MainStaticService.run(16);
        NodePrinter.printLatestSolutionWithDependencies(nodes);
    }
}// End of Application
