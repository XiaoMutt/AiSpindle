package XLineScan;

import AiSpindle.PointPair;
import AiSpindle.SpindleContourAnalyzer;
import AiSpindle.SpindleIdentifier;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.StackWindow;
import ij.gui.Toolbar;
import java.util.List;

/**
 *
 * @author Xiao Zhou
 */
public class XRoiPickUpWindow extends StackWindow {

    private final int mtChannel;
    private final int dnaChannel;
    private final RoiListManager roiList;
    private final XLineScanWorker lineScanWorker;

    public XRoiPickUpWindow(String fileName, XLineScanWorker lineScanWorker, boolean autoDetection, int mtChannel, int dnaChannel) {
        super(IJ.openImage(fileName));
        this.mtChannel = mtChannel;
        this.dnaChannel = dnaChannel;
        this.lineScanWorker = lineScanWorker;

        if (autoDetection) {
            Overlay overlay = autoDetection(imp);
            imp.setOverlay(overlay);
        }

        IJ.setTool(Toolbar.LINE);

        roiList = new RoiListManager(this);

    }

    @Override
    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
        lineScanWorker.resumeWorker(roiList.getRois());        
        roiList.dispose();
        this.close();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        roiList.setLocation(this.getLocation().x + this.getWidth(), this.getLocation().y);
        roiList.setVisible(true);
    }

    private Overlay autoDetection(ImagePlus imp) {
        SpindleIdentifier si = new SpindleIdentifier(imp, mtChannel, dnaChannel);
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

    void canel() {
        roiList.dispose();
        this.close();
        lineScanWorker.resumeWorker(null);
        lineScanWorker.cancel(true);
    }

}
