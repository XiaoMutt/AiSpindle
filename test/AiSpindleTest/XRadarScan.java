package AiSpindleTest;
import AiSpindle.FullPolygon;
import AiSpindle.PointPair;
import AiSpindle.SpindleContourAnalyzer;
import AiSpindle.SpindleIdentifier;
import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.io.OpenDialog;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;

/**
 *
 * @author Xiao Zhou
 */
public class XRadarScan {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        java.awt.EventQueue.invokeLater(() -> {
            ImageJ imj = new ImageJ(ImageJ.STANDALONE);
            imj.setVisible(true);
            imj.exitWhenQuitting(true);

        });

        OpenDialog od = new OpenDialog("Choose a Image File");
        String file_name = od.getPath();
        CompositeImage imp = new CompositeImage(new ImagePlus(file_name));

        SpindleIdentifier si = new SpindleIdentifier(imp, 2, 1);
        Overlay overlay = new Overlay();
        for (SpindleContourAnalyzer b : si.getSpindles()) {
           // Point[] points = b.getSpindleLongAxisPoints();
            //overlay.add(new Line(points[0].x, points[0].y, points[1].x, points[1].y), "line");
            for(FullPolygon mt: b.mtFullPolygons){
                overlay.add(new PolygonRoi(mt.getPolygon(), Roi.POLYGON), "Spindle");
            }
            for(PointPair p: b.mtAxis){
                overlay.add(new Line(p.A.x, p.A.y, p.B.x, p.B.y), "Axis");
            }            
            for(FullPolygon dna: b.dnaFullPolygons){
                overlay.add(new PolygonRoi(dna.getPolygon(), Roi.POLYGON), "DNA");
            }
            for(PointPair p: b.dnaAxis){
                overlay.add(new Line(p.A.x, p.A.y, p.B.x, p.B.y), "Axis");
            }             
        }

        /*
         //MicrotubuleBlobs mb=new MicrotubuleBlobs(mtImp);
         DNABlobs mb=new DNABlobs(dnaImp);
         Overlay overlay=new Overlay();
         for(Polygon pg: mb.getOuterContourPolygons()){
         overlay.add(new PolygonRoi(pg, Roi.POLYGON));
         }
         */
        
        imp.setOverlay(overlay);
        imp.updateAndDraw();
        imp.setDisplayMode(IJ.COMPOSITE);
        imp.show();


        /*
         Binner br = new Binner();
         ImageProcessor impbinned = br.shrink(ip, 2, 2, Binner.MEDIAN);
         */
    }

}
