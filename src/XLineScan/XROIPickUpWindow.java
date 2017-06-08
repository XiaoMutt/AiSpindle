package XLineScan;

import AiSpindle.PointPair;
import AiSpindle.SpindleContourAnalyzer;
import AiSpindle.SpindleIdentifier;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.gui.Toolbar;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Xiao Zhou
 */
public class XROIPickUpWindow implements Runnable {

    private final String FileName;
    private final ArrayList<Roi> PickedROIs;
    private final XLineScanWorker RunningLineScanWorker;

    private StackWindow imageWd;
    private RoiListManager roiList;

    @Override
    public void run() {
        if (!RunningLineScanWorker.isCancelled()) {
            showWindow();
        }
    }

    public XROIPickUpWindow(String fileName, XLineScanWorker runningLineScanWorker) {
        FileName = fileName;
        RunningLineScanWorker = runningLineScanWorker;
        PickedROIs = new ArrayList();
    }

    public Roi[] obtainPickedROIs() {
        if (PickedROIs.isEmpty()) {
            return null;
        } else {
            return PickedROIs.toArray(new Roi[PickedROIs.size()]);
        }
    }

    private void showWindow() {
        imageWd = new StackWindow(IJ.openImage(FileName));
        ImagePlus imp = imageWd.getImagePlus();
        imageWd.setVisible(true);

        if (RunningLineScanWorker.getUseAutoDetection()) {
            Overlay overlay = autoDetection(imp);
            imp.setOverlay(overlay);
        }

        roiList = new RoiListManager(imp, imageWd);
        imageWd.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                Overlay overlay = imp.getOverlay();
                if (overlay != null) {
                    for (Roi roi : overlay.toArray()) {
                        PickedROIs.add(roi);
                    }
                }
                roiList.dispose();
                imageWd.close();
                RunningLineScanWorker.resumeWorker();
            }
        });
        IJ.setTool(Toolbar.LINE);
        roiList.setLocation(imageWd.getLocation().x + imageWd.getWidth(), imageWd.getLocation().y);
        roiList.setVisible(true);

    }

    private Overlay autoDetection(ImagePlus imp) {
        ImagePlus imp2=imp.duplicate();
        SpindleIdentifier si = new SpindleIdentifier(imp, RunningLineScanWorker.getMtchannel(), RunningLineScanWorker.getDnachannel());
        Overlay overlay = new Overlay();
        List<SpindleContourAnalyzer> spindles = si.getSpindles();
        for (SpindleContourAnalyzer s : spindles) {
            if (s.getSpindleType() == SpindleContourAnalyzer.SINGLE_MT_SINGLE_DNA) {
                PointPair pp = s.getMainMtAxis();
                overlay.add(new Line(pp.A.x, pp.A.y, pp.B.x, pp.B.y));
            }
        }

        return overlay;
    }

    public void close() {
        if(imageWd!=null)imageWd.close();
        if(roiList!=null)roiList.dispose();
        RunningLineScanWorker.resumeWorker();
        RunningLineScanWorker.cancel(true);

    }

}
