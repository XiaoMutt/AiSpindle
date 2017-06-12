/*
 * GPLv3
 */
package AiSpindle;

import ij.ImagePlus;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author Xiao Zhou
 */
public class SpindleContourAnalyzer {

    public final List<FullPolygon> mtFullPolygons;
    public final List<FullPolygon> dnaFullPolygons;
    public final List<PointPair> mtAxis;
    public final List<PointPair> dnaAxis;

    public static final int UNDEFINED = -1;
    public static final int SINGLE_MT_NO_DNA = 0;
    public static final int SINGLE_MT_SINGLE_DNA = 1;
    public static final int SINGLE_MT_MULTIPLE_DNA = 2;
    public static final int MULTIPLE_MT_SINGLE_DNA = 3;
    public static final int MULTIPLE_MT_MULTIPLE_DNA = 4;

    private int spindleType;
    private final ImagePlus mtImp;
    private final ImagePlus dnaImp;
    private static final double DNA_POLYGON_PAIR_ANGLE_CUTOFF = 0.1;
    private static final double DNA_POLYGON_ASPECT_RATIO_CUTOFF = 1.8;

    //private static final double MT_FIT_GOODNESS_CUTOFF=0.5;//cutoff value to assess the direction analysis of a microtubule polygon;
    public SpindleContourAnalyzer(List<Polygon> mtPolygons, List<Polygon> dnaPolygons, ImagePlus mtImagePlus, ImagePlus dnaImagePlus) {
        mtFullPolygons = new ArrayList<>();
        dnaFullPolygons = new ArrayList<>();
        for (Polygon pg : mtPolygons) {
            mtFullPolygons.add(new FullPolygon(pg));
        }
        for (Polygon pg : dnaPolygons) {
            dnaFullPolygons.add(new FullPolygon(pg));
        }
        mtImp = mtImagePlus;
        dnaImp = dnaImagePlus;
        mtAxis = new ArrayList<>();
        dnaAxis = new ArrayList<>();
        spindleType = UNDEFINED;
        calDnaPolygonsAxises();
        calMtPolygonsAxises();
        setSpindleType();
    }

    public int getSpindleType() {
        return spindleType;
    }

    /**
     * Calculate all the axises of all the mtPolygon and return the PointPair of
     * the main axis of all mtPolygons. The PointPair of the main axis is the
     * two Points which have the longest distance among all points.
     *
     * @return the PointPair of the main axis of all mtPolygons.
     */
    private void calMtPolygonsAxises() {
        //calculate axis for each mtPolygon;
        if (dnaAxis.size() < 2) {
            //one DNA axis found and one mtPolygons found, use mtPolygons microtubule direction axis.
            if (mtFullPolygons.size() == 1) {
                FullPolygon fpg = mtFullPolygons.get(0);
                DirecionAnalysisResult dar = calDirection(mtImp, fpg.getPolygon());
                mtAxis.add(fpg.getFarthestInnerLinePointPairInOneDirection(dar.angle));
            } else {
                for (FullPolygon fpg : mtFullPolygons) {
                    DirecionAnalysisResult dar = calDirection(mtImp, fpg.getPolygon());
                    mtAxis.add(fpg.getFarthestInnerLinePointPairInOneDirection(dar.angle));
                }
            }
        } else {
            //multiple DNA axis found; possible clustered spindle; use DNA axis as reference.
            for (PointPair pp : dnaAxis) {
                for (FullPolygon fpg : mtFullPolygons) {
                    PointPair axisPP = fpg.getFarthestInnerLinePointPairInOneDirectionAndIntersectALine(pp.getDirection()
                            + Math.PI / 2, pp.shrinkPointPair(0.90));//spindle axis should pass DNA axis center 20% region(ie shrink 90%).
                    if (axisPP != null) {
                        mtAxis.add(axisPP);
                    }
                }
            }

        }
    }

    /**
     * set spindle type based on DNA and microtubule axis
     */
    private void setSpindleType() {
        int mt = mtAxis.size();
        int dna = dnaAxis.size();
        if (mt == 1) {
            if (dna == 0) {
                spindleType = SINGLE_MT_NO_DNA;
            } else if (dna == 1) {
                spindleType = SINGLE_MT_SINGLE_DNA;
            } else if (dna > 1) {
                spindleType = SINGLE_MT_MULTIPLE_DNA;
            }
        } else if (mt > 1) {
            if (dna == 1) {
                spindleType = MULTIPLE_MT_SINGLE_DNA;
            } else if (dna > 1) {
                spindleType = MULTIPLE_MT_MULTIPLE_DNA;
            }
        }
    }

    private void calDnaPolygonsAxises() {
        //calculate axis for each mtPolygon;
        HashSet<PointPair> dnaAxisPairSet = new HashSet<>();
        double distanceCutoff = 0;
        for (FullPolygon fpg : dnaFullPolygons) {
            PointPair longpp = fpg.getFarthestInnerLinePointPair();
            double dnaDirection = longpp.getDirection();
            PointPair shortpp = fpg.getFarthestInnerLinePointPairInOneDirection(dnaDirection + Math.PI / 2);

            if (longpp.getDistance() / shortpp.getDistance() < DNA_POLYGON_ASPECT_RATIO_CUTOFF) {
                dnaDirection = calDirection(dnaImp, fpg.getPolygon()).angle;
                longpp = fpg.getFarthestInnerLinePointPairInOneDirection(dnaDirection + Math.PI / 2);
            }

            distanceCutoff += longpp.getDistance();
            dnaAxisPairSet.add(longpp);
        }
        //set distanceCutoff to be the average of longAxis and then 0.618
        distanceCutoff = 0.618 * distanceCutoff / dnaAxisPairSet.size();

        for (PointPair pp : dnaAxisPairSet) {
            int i;
            for (i = 0; i < dnaAxis.size(); i++) {
                PointPair dnaAxi = dnaAxis.get(i);
                double dAngle = Math.abs(Math.abs(dnaAxi.getDirection()) - Math.abs(pp.getDirection()));
                double dis = pp.getShortestDistanceTo(dnaAxi);
                if ((dAngle < DNA_POLYGON_PAIR_ANGLE_CUTOFF//dna axis should be small
                        || pp.getDistance() < distanceCutoff//also incooperate small blobs
                        || dnaAxi.getDistance() < distanceCutoff)//also incooperate small blobs
                        && dis < distanceCutoff) {//distance should be short
                    //merge PointPair into the current axis
                    PointPair newPP = pp.getAveragePointPair(dnaAxi);
                    dnaAxis.set(i, newPP);
                    break;
                }
            }
            if (i == dnaAxis.size()) {
                dnaAxis.add(pp);
            }
        }
    }

    /**
     * @param imp The ImagePlus object that contains the image
     * @param pg The Polygon objects need to be analyzed
     * @return the direction of the image within the polygon
     */
    private DirecionAnalysisResult calDirection(ImagePlus imp, Polygon pg) {
        imp.setRoi(pg.getBounds());
        ImagePlus cropped = imp.crop();
        imp.deleteRoi();
        return DirectionDetector.calculateDirection(cropped);
    }

    public PointPair getMainMtAxis() {
        if (mtAxis.size() > 0) {
            PointPair pp = mtAxis.get(0);
            for (int i = 1; i < mtAxis.size(); i++) {
                pp = pp.getFurthestPointPair(mtAxis.get(i));
            }
            return pp;
        }

        return null;
    }

}
