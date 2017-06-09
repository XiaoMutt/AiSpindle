package AiSpindle;

import ij.process.ImageProcessor;
import java.util.Iterator;
import java.util.TreeSet;
/**
 * 
 * @author Xiao Zhou
 * Modified from online sources.
 * Get rid of background using rubber band method.
 * The rubber band is found by QuickHull algorithm.
 * 
 */

public class RubberBandBaseLineCorrecter {

    private static TreeSet<Integer> convexHull;
    
    /**
     * 
     * @param ip ImageProcesser object that need to be processed
     */
    public static void CorrectBaseline(ImageProcessor ip) {

        int w = ip.getWidth(), h = ip.getHeight();
        int[] intArray;
        
        intArray = new int[w];
        for (int y = 0; y < h; y++) {
            ip.getRow(0, y, intArray, w);
            convexHull = new TreeSet<>();
            bottomConvexHull(intArray);
            ip.putRow(0, y, calRubberBandArray(intArray), w);

        }
        /*column wise
        intArray = new int[h];
        for (int x = 0; x < w; x++) {
            ip.getColumn(x, 0, intArray, h);
            convexHull = new TreeSet();
            bottomConvexHull(intArray);
            ip.putColumn(x, 0, calRubberBandArray(intArray), h);
        }
        */
        convexHull = null;
    }

    private static void bottomConvexHull(int[] intArray) {
        convexHull.add(0);
        convexHull.add(intArray.length - 1);
        findHull(intArray, 0, intArray.length - 1);
    }

    private static void findHull(int[] intArray, int a, int b) {
        int minIndex = findFurthestPoint(intArray, a, b);
        if (a < minIndex && minIndex < b) {
            convexHull.add(minIndex);
            findHull(intArray, a, minIndex);
            findHull(intArray, minIndex, b);
        }
    }

    private static int  findFurthestPoint(int[] intArray, int a, int b) {
        int delta, distance = 0, furthestIndex = 0;

        for (int x = a + 1; x < b; x++) {
            delta = (intArray[b] - intArray[a]) * (x - a) + (intArray[a] - intArray[x]) * (b - a);//pseudo distance
            if (delta > distance) {
                furthestIndex = x;
                distance = delta;
            }
        }
        return furthestIndex;
    }

    private static  int[] calRubberBandArray(int[] intArray) {
        int[] result = new int[intArray.length];
        Iterator it = convexHull.iterator();
        int b, a = it.hasNext() ? (int) it.next() : 0;
        while (it.hasNext()) {
            b = (int) it.next();
            for (int x = a; x < b; x++) {
                result[x] = (int) (intArray[x] - (((float) intArray[b] - intArray[a]) / (b - a) * (x - a) + intArray[a]));
            }
            a = b;
        }
        return result;
    }

}
