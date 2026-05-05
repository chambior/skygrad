package fr.tchkll.skygrad.utils.algo;

import java.util.ArrayList;
import java.util.List;

public class Drawer {
    public static List<Pixel> line(int x1, int z1, int x2, int z2) {
        List<Pixel> points = new ArrayList<>();

        int dx = x2 - x1;
        int dz = z2 - z1;

        int steps = Math.max(Math.abs(dx), Math.abs(dz));
        if (steps == 0) {
            points.add(new Pixel(x1, z1));
            return points;
        }

        double xInc = (double) dx / steps;
        double zInc = (double) dz / steps;

        double x = x1;
        double z = z1;

        for (int i = 0; i <= steps; i++) {
            points.add(new Pixel((int) Math.round(x), (int) Math.round(z)));
            x += xInc;
            z += zInc;
        }

        return points;
    }
}
