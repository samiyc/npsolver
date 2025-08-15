package dev.samiyc.npsolver;

import java.util.List;

import dev.samiyc.npsolver.bean.Node;
import dev.samiyc.npsolver.service.MainStaticService;

public class Application {

    public static void main(String[] W) {
        List<Node> nodes = MainStaticService.run(17);
        Node.printLatestSolutionWithDependencies(nodes);
    }
}// End of Application
