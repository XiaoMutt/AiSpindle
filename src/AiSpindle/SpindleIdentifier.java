package AiSpindle;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.GaussianBlur;
import ij.process.ImageConverter;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Xiao zhou
 */
public class SpindleIdentifier {

    private final List<SpindleContourAnalyzer> spindles;
    private final ImagePlus mtImp;
    private final ImagePlus dnaImp;

    public SpindleIdentifier(ImagePlus imagePlus, int mtChannel, int dnaChannel) {
        spindles = new ArrayList<>();
        ImageStack imageStack = imagePlus.getImageStack();
        mtImp = new ImagePlus("mt", imageStack.getProcessor(mtChannel));
        dnaImp = new ImagePlus("dna", imageStack.getProcessor(dnaChannel));
        
        new ImageConverter(mtImp).convertToGray8();
        new GaussianBlur().blurGaussian(mtImp.getChannelProcessor(), 2);        
        new ImageConverter(dnaImp).convertToGray8();
        new GaussianBlur().blurGaussian(dnaImp.getChannelProcessor(), 2);

        MicrotubuleBlobs mb = new MicrotubuleBlobs(mtImp);
        DNABlobs db = new DNABlobs(dnaImp);

        pairMicrotubuleAndDNA(mb.getOuterContour(), db.getOuterContour());
    }

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
