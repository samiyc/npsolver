# NP-Solver

NP-Solver is a Java-based exploration and solver framework for discovering relationships between input values and expected outputs.
It attempts to reconstruct formulas, logical operations, and transformation rules by combining input values with various operators in multiple passes.

The algorithm uses a **forward-propagation** approach to generate candidate nodes, evaluate them, and identify matches against expected outputs.

✅ **Code coverage**: >95% only logging lines left uncovered  
✅ **No issues** found in VSCode problem tab.


## 🚀 Quick Start

To run a problem and print the **latest 100% match node** with all its contributing ancestors:

```java
package dev.samiyc.npsolver;

import java.util.List;
import dev.samiyc.npsolver.bean.Node;
import dev.samiyc.npsolver.service.MainStaticService;
import dev.samiyc.npsolver.utils.NodePrinter;

public class Application {
    public static void main(String[] W) {
        List<Node> nodes = MainStaticService.run(problemId: 16);
        NodePrinter.printLatestSolutionWithDependencies(nodes);
    }
}
```

**Example output:**

```
-- Node Tree Solution --
0[A A|5 218|0.0]
1[B B|131 -269|0.0]
2[C C|-177 440|0.0]
3[D D|531 -556|0.0]
17[AxD|2655 -121208|>> 100.0 <<]
23[B+C|-46 171|>> 100.0 <<]
458[17l23|-46 -121208|>> 100.0 <<]
```


## 🧠 How the Algorithm Works

1. **Initialization**

   * Loads problem definition: inputs (`InOut`) and expected outputs.
   * Creates base `Node` objects for each raw value.

2. **Forward Propagation**

   * Iteratively combines existing nodes using arithmetic and logical operators (`+`, `-`, `*`, `/`, comparisons, ternary-like structures).
   * Generates new nodes with results stored as `Value`.

3. **Evaluation**

   * Each node's output is compared with expected results.
   * An evaluation score (`avgEval`) is calculated — **100.0 means perfect match**.

4. **Selection of Best Solutions**

   * Nodes with perfect score are tracked.
   * `NodePrinter` can print the **latest** perfect node and all its **ancestors** (nodes required to produce it).


## ✅ Strengths

* **High Coverage** — nearly 100% test coverage ensures strong stability.
* **Flexible Node System** — easily extendable to support more operators.
* **Readable Outputs** — solution tree clearly shows dependencies.
* **Deterministic Evaluation** — same inputs always yield same results.


## ⚠️ Weaknesses

* **Performance** — for large input sets, the search space grows quickly.
* **Edge Cases** — overlapping conditions or equivalent formulas can mislead the evaluation.
* **Limited Optimization** — operator choice is currently exploratory rather than guided.
* **No Parallelization** — runs on a single thread.


## 📋 TODO / Future Improvements

1. **Algorithmic Optimization**

   * Add heuristics to reduce unnecessary node generation.
   * Implement pruning based on partial matches.
   * Introduce caching of intermediate results.

2. **Operator Set Expansion**

   * Support more advanced math functions.
   * Add string handling and bitwise operators.

3. **Parallel Execution**

   * Split search space across threads for faster exploration.

4. **Better Edge-Case Detection**

   * Distinguish logically different formulas producing identical outputs.
   * Add test cases targeting overlapping conditions.


## ⏱ Problems Solved Within \~15 Seconds Total

All tests, including the following problem IDs, complete in \~15 seconds total during runs (not per problem):

1. `a == 0`
2. `a > 0`
3. `a >= 0`
4. `a + b`
5. `a + b + d + d`
6. `a < b && c > d`
7. `Math.abs(a)`
8. `Math.min(a, b)`
9. `(int) Math.sqrt(a + b)`
10. `(int) Math.sqrt(Math.pow(a - c, 2) + Math.pow(b - d, 2))`
11. `a * a - d * d`
12. `a * a - b * b + c - d`
13. `a < b ? d : b + c`
14. `a < b ? d * d : b + c`
15. `a > b && c > d`
16. `a > b && a > c && c > d ? a : false`

⚠️ **Known Limitation:** Problem 17 — `a > b && c > d ? a : b + c` — is currently **not solvable** by the solver.
