/*
 * GPLv3
 */
package AiSpindle;

import java.awt.Point;
import java.awt.Polygon;
import java.util.HashSet;
import java.util.Set;

/**
 * Define a pair of points
 *
 * @author Xiao Zhou
 */
public class PointPair {

    public final Point A;
    public final Point B;
    private final int dx;
    private final int dy;

    /**
     *
     * @param a point a put into the PointPair
     * @param b point b put into the PointPair
     */
    public PointPair(Point a, Point b) {
        A = a;
        B = b;
        dx = A.x - B.x;
        dy = A.y - B.y;
    }

    /**
     *
     * @param x1 x coordinate of point 1 put into the PointPair
     * @param y1 y coordinate of point 1 put into the PointPair
     * @param x2 x coordinate of point 2 put into the PointPair
     * @param y2 y coordinate of point 2 put into the PointPair
     */
    public PointPair(int x1, int y1, int x2, int y2) {
        A = new Point(x1, y1);
        B = new Point(x2, y2);
        dx = B.x - A.x;
        dy = B.y - A.y;
    }

    /**
     *
     * @return squared distance between the pair of points.
     */
    public long getSquaredDistance() {
        return dx * dx + dy * dy;
    }

    public double getDirection() {
        if (dx != 0) {
            return Math.atan(-((double)dy )/ dx);
        } else {
            return (dy > 0 ? -Math.PI / 2 : Math.PI / 2);
        }
    }
    
    public double getPerpendicularDirection(){
        if(dy!=0){
            return Math.atan(((double)dx)/dy);
        }else{
            return Math.PI/2;
        }
    }

    public double getDistance() {
        return Math.sqrt(dx * dx + dy * dy);
    }
    /**
     *
     * @param pointPairB the other PointPair to be processed with current
     * PointPair
     * @return a new PointPair contains the furthest two points among all four
     * points.
     */
    public PointPair getFurthestPointPair(PointPair pointPairB) {
        PointPair res = this;
        if (pointPairB == null) {
            return res;
        }
        long distance = 0;
        PointPair[] pps = new PointPair[4];
        pps[0] = new PointPair(A, pointPairB.A);
        pps[1] = new PointPair(A, pointPairB.B);
        pps[2] = new PointPair(B, pointPairB.A);
        pps[3] = new PointPair(B, pointPairB.B);
        for (PointPair pp : pps) {
            long dis = pp.getSquaredDistance();
            if (dis > distance) {
                distance = dis;
                res = pp;
            }
        }
        return res;
    }

    /**
     * Given that the line segment of pointPairB does not cross the line segment
     * of current PointPair return the shortest distance between the pointPairB
     * and the line made by the current PointPairs.
     *
     * @param pointPairB
     * @return the shortest distance between pointPairB and the line made by the
     * points of this PointPair Object.
     */
    public double getShortestDistanceTo(PointPair pointPairB) {
        //TODO: below four lines need to be corrected. If PointPair A and/or B are both single point.
        if (dy == 0 && dx == 0) {
            PointPair pp = getFurthestPointPair(pointPairB);
            return pp.getDistance();
        }

        int a = -dy;//the cooridates are reversed in y axis for screen display.
        int b = -dx;
        int c = dx * B.y + dy * B.x;
        Point p1 = pointPairB.A, p2 = pointPairB.B;

        double d1 = Math.abs(a * p1.x + b * p1.y + c);
        double d2 = Math.abs(a * p2.x + b * p2.y + c);

        return Math.min(d1, d2) / Math.sqrt(a * a + b * b);

    }
    
    /**
     * Merge two PointPairs and return the merged PointPair
     * @param ppB PointPair to merge with.
     * @return merged PointPair
     */

    public PointPair getAveragePointPair(PointPair ppB) {
        Set<Point> pSet=new HashSet<>();
        pSet.add(A);
        pSet.add(B);
        pSet.add(ppB.A);
        pSet.add(ppB.B);
               

        Polygon pg=QuickHull2D.findConvexHull(pSet);
        //TODO: replaced this with a rotating caliper function in QuickHull2D class.
        FullPolygon fpp=new FullPolygon(pg);
        
        return fpp.getFarthestInnerLinePointPair();
    }
    
    public PointPair shrinkPointPair(double percentage){
        percentage=1-percentage;
        
        int cx=(A.x+B.x)/2;
        int cy=(A.y+B.y)/2;
        
        int x1=cx-(int)(percentage*(cx-A.x));
        int y1=cy-(int)(percentage*(cy-A.y));
        int x2=cx+(int)(percentage*(B.x-cx));
        int y2=cy+(int)(percentage*(B.y-cy));
        
        return new PointPair(x1, y1, x2, y2);
        
    }
    
    


}
