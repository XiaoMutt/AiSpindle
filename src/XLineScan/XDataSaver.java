/*
 * Here comes the text of your license
 * Each line should be prefixed with  *
 */
package XLineScan;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Mutt
 */
public class XDataSaver {

    private final FileOutputStream FileOut;
    private final XSSFWorkbook XWorkbook;
    private final ArrayList<XSSFSheet> XSheet;
    private final int NORMALIZED_DATASHEET_INDEX = 3;

    private int RowCount;

    public XDataSaver(String fileName) throws FileNotFoundException {
        FileOut = new FileOutputStream(fileName);
        XWorkbook = new XSSFWorkbook();
        XSheet = new ArrayList<>();
        XSheet.add(XWorkbook.createSheet("Numerator Raw Profiles"));
        XSheet.add(XWorkbook.createSheet("Denominator Raw Profiles"));
        XSheet.add(XWorkbook.createSheet("Raw Ratio Profiles"));
        XSheet.add(XWorkbook.createSheet("Normalized Numerator Profiles"));
        XSheet.add(XWorkbook.createSheet("Normalized Denominator Profiles"));
        XSheet.add(XWorkbook.createSheet("Normalized Ratio Profiles"));
        XSheet.add(XWorkbook.createSheet("Length"));
        RowCount = 0;

    }

    public int saveDataToExcel(double[][] dataSets) throws IOException {
        for (int i = 0; i < dataSets.length; i++) {
            saveData(XSheet.get(i), dataSets[i]);
        }
        RowCount++;
        return RowCount;
    }

    private void saveData(XSSFSheet sheet, double[] data) {
        XSSFRow row = sheet.createRow(RowCount);
        row.createCell(0).setCellValue("DataSet" + Integer.toString(RowCount + 1));
        for (int i = 0; i < data.length; i++) {
            row.createCell(i + 1).setCellValue(data[i]);
        }

    }

    public int getRowCount() {
        return RowCount;
    }

    public void saveNormalizedMeanAndSD(double[][] mean, double[][] sd) {
        for (int n = 0; n < 3; n++) {
            XSSFRow row = XSheet.get(n + NORMALIZED_DATASHEET_INDEX).createRow(RowCount);
            row.createCell(0).setCellValue("Mean");
            for (int i = 0; i < mean[n].length; i++) {
                row.createCell(i + 1).setCellValue(mean[n][i]);
            }
            row = XSheet.get(n + NORMALIZED_DATASHEET_INDEX).createRow(RowCount + 1);
            row.createCell(0).setCellValue("SD");
            for (int i = 0; i < mean[n].length; i++) {
                row.createCell(i + 1).setCellValue(sd[n][i]);
            }

        }
    }

    public void close() throws IOException {
        XWorkbook.write(FileOut);
        FileOut.close();
    }

}
