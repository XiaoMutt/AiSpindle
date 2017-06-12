/**
 *
 * @author Xiao zhou
 *
 * This package "AiSpindle" utilizes the following packages to automatically
 * identify Xenopus laevis spindles from images.
 *
 * ImageJ and several of its extensions: ij-blob: https://imagej.net/IJ_Blob
 * Shape Smoothing: http://imagej.net/Shape_Smoothing which uses JTransforms:
 * https://github.com/wendykierp/JTransforms
 * VIB TubenessProcessor:http://imagej.net/VIB_Protocol
 * Directionality: https://imagej.net/Directionality
 *
 *
 * GPLv3
 */
package AiSpindle;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.RankFilters;
import ij.process.ImageConverter;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

public class SpindleIdentifier {

    private final List<SpindleContourAnalyzer> spindles;
    private final ImagePlus mtImp;
    private final ImagePlus dnaImp;

    /**
     *
     * @param imagePlus imagePlus need to be processed
     * @param mtChannel the microtubule channel number in the imagePlus
     * @param dnaChannel the dna Channel number in the imagePlus
     */
    public SpindleIdentifier(ImagePlus imagePlus, int mtChannel, int dnaChannel) {
        spindles = new ArrayList<>();
        ImageStack imageStack = imagePlus.getImageStack();
        mtImp = new ImagePlus("mt", imageStack.getProcessor(mtChannel));
        dnaImp = new ImagePlus("dna", imageStack.getProcessor(dnaChannel));

        new RankFilters().rank(mtImp.getChannelProcessor(), 3, RankFilters.MEDIAN);
        mtImp.resetDisplayRange();//if not reset the ImageConverter will convert according to the display range.
        new ImageConverter(mtImp).convertToGray8();
        //RubberBandBaseLineCorrecter.CorrectBaseline(mtImp.getProcessor());

        new RankFilters().rank(dnaImp.getChannelProcessor(), 3, RankFilters.MEDIAN);
        dnaImp.resetDisplayRange();//if not reset the ImageConverter will not function properly (do not why).
        new ImageConverter(dnaImp).convertToGray8();
        //RubberBandBaseLineCorrecter.CorrectBaseline(dnaImp.getProcessor());

        MicrotubuleBlobs mb = new MicrotubuleBlobs(mtImp);
        DNABlobs db = new DNABlobs(dnaImp);
        pairMicrotubuleAndDNA(mb.getOuterContour(), db.getOuterContour());
    }

    /**
     *
     * @return a List of SpindleContourAnalyzer which contains the microtubule
     * contours and DNA contours
     */
    public List<SpindleContourAnalyzer> getSpindles() {
        return spindles;
    }

    private void pairMicrotubuleAndDNA(List<Polygon> mtPolygons, List<Polygon> dnaPolygons) {
        while (!mtPolygons.isEmpty()) {
            Polygon mt = mtPolygons.iterator().next();

            List<Polygon> mtComboList = new ArrayList<>();
            mtPolygons.remove(mt);
            mtComboList.add(mt);

            //get all DNA polygons overlapping with mt
            List<Polygon> dnaComboList = new ArrayList<>();
            for (Polygon pg : dnaPolygons) {
                if (isMicrotubuleOverlappingWithDNA(mt, pg)) {
                    dnaComboList.add(pg);
                }
            }

            if (!dnaComboList.isEmpty()) {
                //get rid of dnaComboSet polygons in dnaPolygons
                for (Polygon pg : dnaComboList) {
                    dnaPolygons.remove(pg);
                }

                //get all microtubule polygons overlapping with wholeDNA
                for (Polygon mtPg : mtPolygons) {
                    for (Polygon dnaPg : dnaComboList) {
                        if (isMicrotubuleOverlappingWithDNA(mtPg, dnaPg)) {
                            mtComboList.add(mtPg);
                        }
                    }
                }
                //get rid of mtComoboSet polygons in mtPolygons
                for (Polygon pg : mtComboList) {
                    mtPolygons.remove(pg);
                }

                spindles.add(new SpindleContourAnalyzer(mtComboList, dnaComboList, mtImp, dnaImp));

            }
        }

    }

    /**
     * First parameter has to be spindle polygon, and second parameter has to be
     * DNA polygon.
     *
     * @param spindle spindle polygon
     * @param dna DNA polygon
     * @return ture if spindle contains or overlapping with DNA polygon,
     * otherwise false.
     */
    private boolean isMicrotubuleOverlappingWithDNA(Polygon spindle, Polygon dna) {
        int[] dnax = dna.xpoints;
        int[] dnay = dna.ypoints;
        for (int i = 0; i < dna.npoints; i++) {
            if (spindle.contains(dnax[i], dnay[i])) {
                return true;
            }
        }
        return false;
    }

}
