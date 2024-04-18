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
        int a = in.get(0).number, b = in.get(1).number, c = in.get(2).number, d = in.get(3).number;

        //  /!\Input Code /!\
        //out = new Value(a > b && c > d ? a * d : b + c);//    KO
        //out = new Value(a > b && c > d ? a : b + c);//        KO 500n
        //out = new Value(a < b ? d * d : b + c);//             OK
        //out = new Value(a < b ? d : b + c);//                 OK
        //out = new Value(a * a - b * b + c - d);//             OK
        //out = new Value(a * a - d * d);//                     OK
        //out = new Value(a + b + d + d);//                     OK
        //out = new Value(a + b);//                             OK
        out = new Value(a < b && c > d);//                     KO 100 Hidden.
        //out = new Value(a >= 0);//                            OK
        //out = new Value(a == 0);//                            OK
        //out = new Value(a > 0);//                             OK
    }

    @Override
    public String toString() {
        return in + " => " + out;
    }
}//End of InOut
