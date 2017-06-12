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
public class DNABlobs extends BlobFilter {

    public DNABlobs(ImagePlus imagePlus) {
        super(imagePlus);
        perceptualThreshold = 5;
        int area = imp.getProcessor().getWidth() * imp.getProcessor().getHeight();

        //DNA channel filter params;
        filterParams.add(new BlobFilterParam(Blob.GETCIRCULARITY, 4 * Math.PI, 600));
        filterParams.add(new BlobFilterParam(Blob.GETENCLOSEDAREA, Math.max(3e-4 * area, 100), Math.max(0.5 * area, 15)));
        filterAndSmoothBlobs();
    }

}
