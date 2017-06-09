/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package XLineScan;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Iterates the folder (and its sub folders) specified by the path parameter in
 * the constructor, returns the full paths of the files found in the folder or
 * null if no files have been found (use the nextFilePath method).
 *
 * @author Xiao Zhou
 */
public class XFileIterator {

    private final Iterator<File> FileIterator;
    private XFileIterator DaughterFileIterator;

    /**
     *
     * @param path full path of the target folder
     */
    public XFileIterator(String path) {
        File working_folder = new File(path);
        ArrayList<File> file_list = new ArrayList<>(Arrays.asList(working_folder.listFiles()));
        FileIterator = file_list.iterator();
        DaughterFileIterator = null;
    }

    /**
     * private constructor used by the obtainFileName method to iterate sub
     * folders
     *
     * @param path full path of the target folder
     */
    private XFileIterator(File currentFile) {
        ArrayList<File> file_list = new ArrayList<>(Arrays.asList(currentFile.listFiles()));
        FileIterator = file_list.iterator();
        DaughterFileIterator = null;
    }

    /**
     *
     * @return true if has another file or folder; otherwise return false
     */
    private boolean hasNext() {
        boolean result = false;
        //check DaughterFileIterator first
        if (DaughterFileIterator != null) {
            result = DaughterFileIterator.hasNext();
        }
        return result || FileIterator.hasNext();

    }

    /**
     * Returns the full path of a file in the folder (including its sub folders)
     * or return null if no files has been found. Keep calling this method until
     * it returns null will iterate all the files in the folder (including its
     * sub folders).
     *
     * @return the full path of a file in the folder (or its sub folders) or
     * null if no files are found
     */
    public String nextFilePath() {
        File current_file;
        String result = null;
        if (DaughterFileIterator != null) {
            result = DaughterFileIterator.nextFilePath();
            if (result == null) {
                DaughterFileIterator = null;//release the daugther object for garbage collection;
            }
        }

        if (DaughterFileIterator == null) {
            while (hasNext()) {
                current_file = FileIterator.next();
                if (current_file.isDirectory()) {
                    DaughterFileIterator = new XFileIterator(current_file);
                    result = DaughterFileIterator.nextFilePath();
                    if (result == null) {
                        DaughterFileIterator = null;//release the daugther object for garbage collection;
                    } else {
                        break;
                    }
                } else {
                    result = current_file.getAbsolutePath();
                    break;
                }
            }
        }
        return result;
    }

}
