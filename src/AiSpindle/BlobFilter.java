/*
 * GPLv3
 * use ij-blob
 * for documents of ij-blob see:
 * http://www.atetric.com/atetric/javadoc/de.biomedical-imaging.ij/ij_blob/1.4.8/
 */
package AiSpindle;

import ij.ImagePlus;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Xiao Zhou
 */
public class BlobFilter {

    protected final List<BlobFilterParam> filterParams;
    protected int perceptualThreshold;
    protected final ImagePlus imp;
    protected final List<Polygon> polygons;
    

    /**
     *
     * @param imagePlus ImagePlus Object that need to be processed for
     * extracting Blobs. The background must be black.
     */
    public BlobFilter(ImagePlus imagePlus) {
        imp = imagePlus.duplicate();
        imp.getProcessor().convertToByteProcessor();
        AutoThresholder.autoTheshold(imp);
        //imp.show();

        filterParams = new ArrayList<>();
        polygons=new ArrayList<>();

    }

    /**
     * Filter and Smooth the blobs extracted from the input ImagePlus
     */
    protected final void filterAndSmoothBlobs() {
        ManyBlobs blobs = new ManyBlobs(imp);
        blobs.setBackground(0);
        blobs.findConnectedComponents();        
        for (BlobFilterParam param : filterParams) {
            blobs = blobs.filterBlobs(param.Min, param.Max, param.FilterMethod);
        }
        
        for (Blob b : blobs) {
            polygons.add(new FullPolygon(b.getOuterContour()).getSmoothedContour(perceptualThreshold));
        }
    }

    /**
     *
     * @return the outer contour polygons of the final filtered and smoothed
     * polygons.
     */
    public List<Polygon> getOuterContour() {
        return polygons;
    }

}
