package dev.samiyc.npsolver.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InOut {
    public List<Value> in;
    public Value out;

    public InOut(int nbInupt, int problemId) {
        in = new ArrayList<>();
        Random random = new Random();
        for (int i = nbInupt; i > 0; i--) in.add(new Value(random.nextInt(100) - 50));

        out = calcOut(problemId);
    }

    public Value calcOut(int problemId) {
        int a = in.get(0).number, b = in.get(1).number, c = in.get(2).number, d = in.get(3).number;
        Value rtn = new Value();

        //   /!\Input Code /!\
             if (problemId == 1) rtn = new Value(a == 0);//                            OK
        else if (problemId == 2) rtn = new Value(a > 0);//                             OK
        else if (problemId == 3) rtn = new Value(a >= 0);//                            OK
        else if (problemId == 4) rtn = new Value(a + b);//                             OK
        else if (problemId == 5) rtn = new Value(a + b + d + d);//                     OK
        else if (problemId == 6) rtn = new Value(a < b && c > d);//                    OK
        else if (problemId == 7) rtn = new Value(a * a - d * d);//                     OK
        else if (problemId == 8) rtn = new Value(a * a - b * b + c - d);//             OK  500n & 100io / c:64, 23, 41, 93, 2194, 104
        else if (problemId == 9) rtn = new Value(a < b ? d : b + c);//                 OK  500n & 100io / c:134, 75, 57, 208, 193, 76
        else if (problemId == 10) rtn = new Value(a < b ? d * d : b + c);//            OK  500n & 100io / c:115, 333, 941, 3220, 466, 110, 1138
        else if (problemId == 11) rtn = new Value(a > b && c > d ? a : b + c);//       OK  500n & 100io / c:11007, 13535, 7204, 1166, nope,
        else if (problemId == 12) rtn = new Value(a > b && c > d ? a * d : b + c);//   OK  500n & 100io / c:6415, nope, 1164, 6382, 2n, 1766
        return rtn;
    }

    @Override
    public String toString() {
        return in + " => " + out;
    }
}//End of InOut
