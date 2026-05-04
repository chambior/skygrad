package fr.tchkll.skygrad.utils.algo;

public class Drawer {
    public static java.util.List<int[]> line(int x1, int y1, int x2, int y2) {
        java.util.List<int[]> points = new java.util.ArrayList<>();

        int dx = x2 - x1;
        int dy = y2 - y1;

        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) {
            points.add(new int[]{x1, y1});
            return points;
        }

        double xInc = (double) dx / steps;
        double yInc = (double) dy / steps;

        double x = x1;
        double y = y1;

        for (int i = 0; i <= steps; i++) {
            points.add(new int[]{(int)Math.round(x), (int)Math.round(y)});
            x += xInc;
            y += yInc;
        }

        return points;
    }
}
