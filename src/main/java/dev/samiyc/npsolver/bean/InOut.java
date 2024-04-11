package dev.samiyc.npsolver.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InOut {
    List<Value> in;
    Value out;

    public InOut(int nbInupt) {
        in = new ArrayList<>();
        Random random = new Random();
        for (int i = nbInupt; i > 0; i--) in.add(new Value(random.nextInt(100) - 50));
        int a = in.get(0).number, b = in.get(1).number, c = in.get(2).number;

        //  /!\Input Code /!\
        //if (a+b > c) out = new Value(a);
        //else out = new Value(b);
        out = new Value(a + b + c);
    }

    @Override
    public String toString() {
        return in + " => " + out;
    }
}//End of InOut
