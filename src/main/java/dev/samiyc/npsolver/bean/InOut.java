package dev.samiyc.npsolver.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InOut {
    public List<Value> in;
    public Value out;

    public InOut(int nbInupt) {
        in = new ArrayList<>();
        Random random = new Random();
        for (int i = nbInupt; i > 0; i--) in.add(new Value(random.nextInt(100) - 50));

        out = calcOut();
    }

    public Value calcOut() {
        int a = in.get(0).number, b = in.get(1).number, c = in.get(2).number, d = in.get(3).number;
        Value out;
        //  /!\Input Code /!\
        //out = new Value(a > b && c > d ? a * d : b + c);//    OK  500n & 100io / c:6415, nope, 1164
        //out = new Value(a > b && c > d ? a : b + c);//        OK  500n & 100io / c:11007, 13535, 7204, 1166, nope,
        out = new Value(a < b ? d * d : b + c);//             OK  500n & 100io / c:115, 333, 941, 3220, 466, 110, 1138
        //out = new Value(a < b ? d : b + c);//                 OK  500n & 100io / c:134, 75, 57, 208, 193, 76
        //out = new Value(a * a - b * b + c - d);//             OK  500n & 100io / c:64, 23, 41, 93, 2194, 104
        //out = new Value(a * a - d * d);//                     OK
        //out = new Value(a + b + d + d);//                     OK
        //out = new Value(a + b);//                             OK
        //out = new Value(a < b && c > d);//                     KO 100 Hidden.
        //out = new Value(a >= 0);//                            OK
        //out = new Value(a == 0);//                            OK
        //out = new Value(a > 0);//                             OK
        return out;
    }

    @Override
    public String toString() {
        return in + " => " + out;
    }
}//End of InOut
