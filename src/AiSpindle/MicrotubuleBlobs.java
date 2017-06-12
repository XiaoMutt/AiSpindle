/*
 * GPLv3
 */
package AiSpindle;

import ij.ImagePlus;
import ij.blob.Blob;

/**
 *
 * @author Xiao Zhou
 */
public class MicrotubuleBlobs extends BlobFilter {

    public MicrotubuleBlobs(ImagePlus imagePlus) {
        super(imagePlus);
        perceptualThreshold = 2;
        int area = imp.getProcessor().getWidth() * imp.getProcessor().getHeight();
        //microtubule channel filter params;
        filterParams.add(new BlobFilterParam(Blob.GETCIRCULARITY, 4 * Math.PI, 100));
        filterParams.add(new BlobFilterParam(Blob.GETENCLOSEDAREA, Math.max(0.0005 * area, 10), Math.max(0.9 * area, 20)));
        filterAndSmoothBlobs();
    }

}
