/**
 * @author Xiao Zhou
 */
package AiSpindle;

import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.util.HashMap;
import java.util.TreeMap;
import org.jtransforms.fft.DoubleFFT_1D;

public class FullPolygon {

    private final Polygon fullPol;

    public FullPolygon(Polygon polygon) {
        super();
        fullPol = toFullPointsPolygon(polygon);
    }

    public Polygon getPolygon() {
        return new Polygon(fullPol.xpoints, fullPol.ypoints, fullPol.npoints);
    }

    /**
     *
     * Modified from the source code of ShapeSmoothingUtil (Author: Thorsten
     * Wagner)
     *
     * @param percentualThresholdValue percentualThresholdValue% of frequencies
     * need to be keep
     * @return smoothed Polygon object
     */
    public Polygon getSmoothedContour(int percentualThresholdValue) {
        if (fullPol.npoints < 2) {
            return fullPol;
        }

        int numOfContourPoints = fullPol.npoints;

        percentualThresholdValue = (percentualThresholdValue * numOfContourPoints) / 100;
        percentualThresholdValue = percentualThresholdValue < 1 ? 1 : percentualThresholdValue;

        /* 
         * prepare contour points for DoubleFFT_1D filter
         * Contour points into the "correct" data structure a[2*k] = Re[k],
         * a[2*k+1] = Im[k], 0<=k<n
         */
        double[] contourPoints = new double[2 * numOfContourPoints];

        for (int i = 0; i < numOfContourPoints; i++) {
            contourPoints[2 * i] = fullPol.xpoints[i];
            contourPoints[2 * i + 1] = fullPol.ypoints[i];
        }
        DoubleFFT_1D ft = new DoubleFFT_1D(numOfContourPoints);

        // Fourier-Hintransformation
        ft.complexForward(contourPoints);
        // Filtering
        int loopFrom = percentualThresholdValue;
        if (loopFrom % 2 != 0) {
            loopFrom = loopFrom + 1;
        }
        int loopUntil = contourPoints.length - percentualThresholdValue;
        if (loopUntil % 2 != 0) {
            loopUntil = loopUntil + 1;
        }

        for (int i = loopFrom; i < loopUntil; i++) {
            contourPoints[i] = 0;
        }
        // reverse Tranformation
        ft.complexInverse(contourPoints, true);
        int[] xpoints = new int[contourPoints.length / 2];
        int[] ypoints = new int[contourPoints.length / 2];

        int j = 0;
        for (int i = 0; i < contourPoints.length; i += 2) {
            xpoints[j] = (int) Math.round(contourPoints[i]);
            ypoints[j] = (int) Math.round(contourPoints[i + 1]);
            j++;
        }

        return new Polygon(xpoints, ypoints, j);
    }

    /**
     * Fill polygon with contour points, i.e. the polygon points are
     * point-touching-point (right up, down, left or right to each other).
     *
     * @param polygon polygon object need to be processed
     * @return polygon with full contour points
     */
    public static Polygon toFullPointsPolygon(Polygon polygon) {
        Polygon fullPol = new Polygon();
        for (int i = 0; i < polygon.npoints; i++) {
            int nextPixel = ((i + 1) == polygon.npoints ? 0 : i + 1);
            int ax = polygon.xpoints[i], bx = polygon.xpoints[nextPixel];
            int ay = polygon.ypoints[i], by = polygon.ypoints[nextPixel];
            double dx = bx - ax, dy = by - ay;

            int ystep = (dy > 0 ? 1 : -1);
            if (dx != 0) {
                int xstep = (dx > 0 ? 1 : -1);
                if (dy == 0) {
                    for (int xi = ax; xstep * (bx - xi) >= 0; xi += xstep) {
                        fullPol.addPoint(xi, by);
                    }
                } else {
                    int previousy = ay;
                    for (int x = ax + xstep; xstep * (bx - x) >= 0; x += xstep) {
                        int y = (int) ((dy / dx) * (x - ax) + ay);
                        for (int yi = previousy; ystep * (y - yi) >= 0; yi += ystep) {
                            fullPol.addPoint(x, yi);
                        }
                        previousy = y;
                    }
                }
            } else {
                if (dy != 0) {
                    for (int yi = ay; ystep * (by - yi) >= 0; yi += ystep) {
                        fullPol.addPoint(ax, yi);
                    }
                }

            }
        }
        return fullPol;
    }


    /**
     *
     * @param radianAngle direction defined in radian
     * @param pp PointPair that form a line segment
     * @return a PointPair containing two points that are on the contour of the
     * Polygon pg, form a longest inner line segment in direction that intersects with the
     * line segment formed by the PointPair pp,
     */
    public PointPair getFarthestInnerLinePointPairInOneDirectionAndIntersectALine(double radianAngle, PointPair pp) {
        Line2D.Double targetLine = new Line2D.Double(pp.A.x, pp.A.y, pp.B.x, pp.B.y);
        int[] ox = fullPol.xpoints, oy = fullPol.ypoints;
        ////HashMap <transformed y, TreeMap<transformed x, original i index>
        HashMap<Integer, TreeMap<Integer, Integer>> mapYXI = new HashMap<>();
        int x0 = ox[0], y0 = oy[0];
        TreeMap<Integer, Integer> treeMap = new TreeMap<>();
        treeMap.put(0, 0);
        mapYXI.put(0, treeMap);

        //transfrom with radianAngle, center at x[0], y[0];
        for (int i = 1; i < fullPol.npoints; i++) {
            int dx = ox[i] - x0, dy = y0 - oy[i];
            double angle = Math.atan2(dy, dx) - radianAngle;

            double l = Math.sqrt(dx * dx + dy * dy);
            int x = (int) (l * Math.cos(angle));
            int y = (int) (l * Math.sin(angle));

            if (mapYXI.containsKey(y)) {
                treeMap = mapYXI.get(y);
                treeMap.put(x, i);
            } else {
                treeMap = new TreeMap<>();
                treeMap.put(x, i);
                mapYXI.put(y, treeMap);
            }
        }

        //line scan through all y lines;
        int maxD = 0, maxi = 0, maxj = 0;
        for (Integer yi : mapYXI.keySet()) {
            treeMap = mapYXI.get(yi);
            Integer[] keys = treeMap.keySet().toArray(new Integer[treeMap.size()]);
            if (keys.length > 1) {
                for (int k = 1; k < keys.length; k++) {
                    int i = treeMap.get(keys[k]), j = treeMap.get(keys[k - 1]);
                    int dx = Math.abs(keys[k] - keys[k - 1]);
                    Line2D.Double tempL = new Line2D.Double(ox[i], oy[i], ox[j], oy[j]);
                    if (tempL.intersectsLine(targetLine)) {
                        if (maxD < dx//find a longer distance pair
                                && fullPol.contains((ox[i] + ox[j]) / 2, (oy[i] + oy[j]) / 2)) {//the middle point should be inside the fullPol

                            maxD = dx;
                            maxi = i;
                            maxj = j;
                        }
                    }
                }

            }
        }
        if(maxi==0&&maxj==0){
            return null;
        }else{
            return new PointPair(ox[maxi], oy[maxi], ox[maxj], oy[maxj]);
        }

    }

    /**
     * Return a PointPair containing two points that are on the contour of the
     * polygon and making the longest inner line of the polygon.
     *
     * @return A PointPair along the Polygon contour that makes the longest
     * inner line of the polygon
     *
     */
    public PointPair getFarthestInnerLinePointPair() {
        double maxD = 0;
        PointPair res = null;

        double step = Math.PI/180;
        for (double theta = -Math.PI; theta < Math.PI; theta += step) {
            PointPair temp = getFarthestInnerLinePointPairInOneDirection(theta);
            double l = temp.getDistance();
            if (maxD < l) {
                res = temp;
                maxD = l;
            }
        }
        return res;
    }

    /**
     * Return a PointPair containing two points that are on the contour of the
     * polygon and making the longest inner line of the polygon in the direction
     * defined by radianAngle.
     *
     * @param radianAngle the angle pointing the direction in radian
     *
     * @return A PointPair containing two points that are on the contour of the
     * polygon and making the longest inner line of the polygon in the direction
     * defined by radianAngle.
     *
     */
    public PointPair getFarthestInnerLinePointPairInOneDirection(double radianAngle) {
        int[] ox = fullPol.xpoints, oy = fullPol.ypoints;
        ////HashMap <transformed y, TreeMap<transformed x, original i index>
        HashMap<Integer, TreeMap<Integer, Integer>> mapYXI = new HashMap<>();
        int x0 = ox[0], y0 = oy[0];
        TreeMap<Integer, Integer> treeMap = new TreeMap<>();
        treeMap.put(0, 0);
        mapYXI.put(0, treeMap);

        //transfrom with radianAngle, center at x[0], y[0];
        for (int i = 1; i < fullPol.npoints; i++) {
            int dx = ox[i] - x0, dy = y0 - oy[i];
            double angle = Math.atan2(dy, dx) - radianAngle;

            double l = Math.sqrt(dx * dx + dy * dy);
            int x = (int) (l * Math.cos(angle));
            int y = (int) (l * Math.sin(angle));

            if (mapYXI.containsKey(y)) {
                treeMap = mapYXI.get(y);
                treeMap.put(x, i);
            } else {
                treeMap = new TreeMap<>();
                treeMap.put(x, i);
                mapYXI.put(y, treeMap);
            }
        }

        //line scan through all y lines;
        int maxD = 0, maxi = 0, maxj = 0;
        for (Integer yi : mapYXI.keySet()) {
            treeMap = mapYXI.get(yi);
            Integer[] keys = treeMap.keySet().toArray(new Integer[treeMap.size()]);
            if (keys.length > 1 ) {
                for (int k = 1; k < keys.length; k++) {
                    int i = treeMap.get(keys[k]), j = treeMap.get(keys[k - 1]);
                    int dx = Math.abs(keys[k] - keys[k - 1]);
                    if (maxD < dx//find a longer distance pair
                            && fullPol.contains((ox[i] + ox[j]) / 2, (oy[i] + oy[j]) / 2)) {//the middle point should be inside the fullPol
                        maxD = dx;
                        maxi = i;
                        maxj = j;
                    }
                }

            }
        }
        return new PointPair(ox[maxi], oy[maxi], ox[maxj], oy[maxj]);

    }

}
