package dev.samiyc.npsolver.bean;

import static dev.samiyc.npsolver.service.MainStaticService.MSG_INFO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class InOut {
    public List<Value> in;
    public Value out;

    public InOut(int nbInupt, int problemId, int count) {
        in = new ArrayList<>();
        Random random = new Random();
        int max = 20 + count;
        for (int i = nbInupt; i > 0; i--) in.add(new Value(random.nextInt(max) - max / 2));
        out = calcOut(problemId);
    }

    public InOut(int problemId, List<Integer> list) {
        in = new ArrayList<>();
        for (Integer i : list) in.add(new Value(i));
        out = calcOut(problemId);
    }

    public Value calcOut(int problemId) {
        int a = in.get(0).number, b = in.get(1).number, c = in.get(2).number, d = in.get(3).number;
        Value rtn = new Value();

        //   /!\Input Code /!\
        if (problemId == 1) rtn = new Value(a == 0);
        else if (problemId == 2) rtn = new Value(a > 0);
        else if (problemId == 3) rtn = new Value(a >= 0);
        else if (problemId == 4) rtn = new Value(a + b);
        else if (problemId == 5) rtn = new Value(a + b + d + d);
        else if (problemId == 6) rtn = new Value(a < b && c > d);
        else if (problemId == 7) rtn = new Value(Math.abs(a));
        else if (problemId == 8) rtn = new Value(Math.min(a, b));
        else if (problemId == 9) rtn = new Value((int) Math.sqrt(a + b));
        else if (problemId == 10) rtn = new Value((int) Math.sqrt(Math.pow(a - c, 2) + Math.pow(b - d, 2)));
        else if (problemId == 11) rtn = new Value(a * a - d * d);
        else if (problemId == 12) rtn = new Value(a * a - b * b + c - d);
        else if (problemId == 13) rtn = new Value(a < b ? d : b + c);
        else if (problemId == 14) rtn = new Value(a < b ? d * d : b + c);
        else if (problemId == 15) rtn = new Value(a > b && c > d);
        else if (problemId == 16) rtn = a > b && a > c && c > d ? new Value(a) : new Value(false);
        else if (problemId == 17) rtn = new Value(a > b && c > d ? a : b + c); //KO

        return rtn;
    }

    public static List<InOut> initMap(int nbIOEntry, int nbInputs, int problemId, int count) {
        List<InOut> map = new ArrayList<>();

        if (nbInputs == 4) {
            if (problemId > 14) {
                map.add(new InOut(problemId, Arrays.asList(-5, -10, 50, 45)));
                map.add(new InOut(problemId, Arrays.asList(50, 45, -5, -10)));
                map.add(new InOut(problemId, Arrays.asList(50, -50, 71, -23)));
                map.add(new InOut(problemId, Arrays.asList(71, -23, 50, -50)));
                map.add(new InOut(problemId, Arrays.asList(71, 70, -50, -51)));
                map.add(new InOut(problemId, Arrays.asList(3, 2, -2, -3)));
                map.add(new InOut(problemId, Arrays.asList(-2, -3, 3, 2)));
                map.add(new InOut(problemId, Arrays.asList(5, 10, 5, 10)));
                map.add(new InOut(problemId, Arrays.asList(-10, 5, -10, 5)));
            } else {
                map.add(new InOut(problemId, Arrays.asList(-31, -27, -5, 2)));
                map.add(new InOut(problemId, Arrays.asList(-3, -2, 33, -5)));
                map.add(new InOut(problemId, Arrays.asList(2, 3, -33, 2)));
                map.add(new InOut(problemId, Arrays.asList(27, 31, 33, -2)));
            }
        }

        for (int j = map.size(); j < nbIOEntry; j++) map.add(new InOut(nbInputs, problemId, count));
        if (MSG_INFO) {
            System.out.println("\n### THE MAP - nbTest:" + map.size());
            System.out.println(map);
        }
        return map;
    }

    @Override
    public String toString() {
        return in + " => " + out;
    }

}//End of InOut
