package org.zehetner.homeautomation.xbee;

import com.rapplogic.xbee.api.XBeeException;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 1/11/12
 * Time: 5:50 PM
 */
public class HexUploaderTest extends TestCase {

    public void testParse() throws IOException, XBeeException, InterruptedException {
        final String inFileName = "./testFile.hex";
        final String outFileName = "./testFile.out";
        final String expFileName = "./testFile.exp";

        HexUploader hexUploader = new HexUploader();
        hexUploader.junitSetOutputFileName(outFileName);

        // Create and clear the file
        FileOutputStream stream = new FileOutputStream(outFileName);
        stream.close();

        hexUploader.parseHexFileAndUpload("anyComPortName", new int[] {1,2,3,4}, inFileName);

        File actual = new File(outFileName);
        File expected = new File(expFileName);
//        assertEquals(FileUtils.readFileToString(expected), FileUtils.readFileToString(actual));
        assertEquals(FileUtils.readFileToString(expected).replaceAll("\\s", ""), FileUtils.readFileToString(actual).replaceAll("\\s", ""));
    }
}
