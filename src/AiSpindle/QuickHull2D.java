package AiSpindle;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Xiao Zhou Implemented the QuickHull2D Algorithm. Inspried by
 * http://www.ahristov.com/tutorial/geometry-games/convex-hull.html. Take a set
 * of polygons and return a polygon the convex hull of the input polygons.
 *
 */
public class QuickHull2D {

    private static ArrayList<Point> convexHull;

    /**
     *
     * @param points a set of points that for which need to find the common
     * convex hull.
     * @return the polygon representing the convex hull.
     */
    public static Polygon findConvexHull(Set<Point> points) {
        convexHull = new ArrayList<>();
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        Point minP = null, maxP = null;

        for (Point p : points) {
                //find left most and right most points on the x axis.
            //they are guaranteed to be on the convex hull.
            if (p.x < minX) {
                minX = p.x;
                minP = p;
            }

            if (p.x > maxX) {
                maxX = p.x;
                maxP = p;
            }
        }

        //add left and right most points on the x axis.
        //meanwhile delete them in the points pool.
        convexHull.add(minP);
        convexHull.add(maxP);
        points.remove(maxP);
        points.remove(minP);

        HashSet<Point> leftSet = new HashSet<>();
        HashSet<Point> rightSet = new HashSet<>();

        for (Point p : points) {
            if (findSide(minP, maxP, p)) {
                leftSet.add(p);
            } else {
                rightSet.add(p);
            }
        }

        findHull(minP, maxP, leftSet);//find hull on left side of the line minP-maxP
        findHull(maxP, minP, rightSet);//find hull on right side of the line minP-maxP

        //construct new Polygon;
        int n = convexHull.size();
        int[] x = new int[n];
        int[] y = new int[n];

        for (int i = 0; i < n; i++) {
            Point p = convexHull.get(i);
            x[i] = p.x;
            y[i] = p.y;
        }

        //release convexHull;
        convexHull = null;

        return new Polygon(x, y, n);
    }

    /**
     * From points, find a convex-hull point that is "left" of the line AB made
     * up by point a and b. Left means line AP is counterclockwise of line AB.
     *
     * @param a point a that makes up one end of the line AB.
     * @param b point b that makes up the other end of the line AB.
     * @param points the points from which a hull point need to be find.
     */
    private static void findHull(Point a, Point b, HashSet<Point> points) {
        if (points.isEmpty()) {
            return;
        }

        int longestDistance = Integer.MIN_VALUE;
        Point p = null;
        for (Point x : points) {
            int distance = findDistance(a, b, x);
            if (longestDistance < distance) {
                longestDistance = distance;
                p = x;
            }
        }

        points.remove(p);
        Integer insertIndex = convexHull.indexOf(b);
        //inset the point into convexHull;
        convexHull.add(insertIndex, p);

        //find "left" points of line AP.
        HashSet<Point> apLeftSet = new HashSet<>();
        for (Point x : points) {
            if (findSide(a, p, x)) {
                apLeftSet.add(x);
            }
        }

        //find "left" points of line BP.
        HashSet<Point> pbLeftSet = new HashSet<>();
        for (Point x : points) {
            if (findSide(p, b, x)) {
                pbLeftSet.add(x);
            }
        }

        findHull(a, p, apLeftSet);
        findHull(p, b, pbLeftSet);
    }

    /**
     *
     * @param a point a that makes up one end of the line AB
     * @param b point b that makes up the other end of the line AB
     * @param p point p, the point to be tested
     * @return ture if line AP is counter clockwise of line AB, otherwise return
     * false
     *
     */
    private static boolean findSide(Point a, Point b, Point p) {
        return ((b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x)) > 0;
    }

    /**
     *
     * @param a point a that makes up one end of the line AB
     * @param b point b that makes up the other end of the line AB
     * @param p point p, the point to be tested
     * @return the pseudo distance from point p to line AB (orthogonal to line
     * AB)
     */
    private static int findDistance(Point a, Point b, Point p) {
        int ABx = b.x - a.x;
        int ABy = b.y - a.y;
        return Math.abs(ABx * (p.y - a.y) - ABy * (p.x - a.x));
    }

}
