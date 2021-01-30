package org.zehetner.homeautomation.xbee;


import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import com.rapplogic.xbee.util.ByteUtils;
import gnu.io.CommPortIdentifier;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/14/11
 * Time: 9:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class HexUploader {
    private final static Logger LOG = Logger.getLogger(HexUploader.class);
    private String junitSetOutputFileName;


    /**
     * Intel HEX formatted file
     * Lines begin with a colon,
     *      then 8-bit data length,
     *      then 16-bit address,
     *      then 8-bit type - 00 for normal data, 01 for end of file,
     *      then the data,
     *      then an 8-bit checksum
     *
     * @param comPort
     * @param serLow Low 4 bytes of XBee serial number
     * @param fileName
     * @return
     * @throws IOException
     */
    public void parseHexFileAndUpload(final String comPort, final int[] serLow, final String fileName) throws IOException, XBeeException, InterruptedException {

        XBeeTransceiver transceiver = new XBeeTransceiver();
        // replace with end device's 64-bit address (SH + SL)
        XBeeAddress64 addr64 = new XBeeAddress64(0x00, 0x13, 0xa2, 0x00, serLow[0], serLow[1], serLow[2], serLow[3]);

        prepareTarget(comPort, transceiver, addr64);
        resetTarget(transceiver, addr64);

        ArrayList<Integer> fileContents = new ArrayList<Integer>();
        readFile(fileName, fileContents);

        int[] outBytes = new int[68];
        int address = 0;
        int count;
        Iterator<Integer> iter = fileContents.iterator ();
        while(iter.hasNext()) {

            int[] addressBytes = ByteUtils.convertInttoMultiByte(address);
            outBytes[0] = 'P';
            outBytes[1] = 'R';
            outBytes[2] = (addressBytes.length > 1)? addressBytes[1] : addressBytes[0]; // lsb
            outBytes[3] = (addressBytes.length > 1)? addressBytes[0] : 0x00; // msb

            count = 0;
            for (int i=0; i<64 && iter.hasNext(); i++) {
                outBytes[4 + i] = iter.next();
                count++;
                address++;
            }


            logIntArray(outBytes);
            System.out.print(String.format("Sending: %02d%%", ((address * 100) / fileContents.size())));
            System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b");
            boolean success;
            do {
                success = uploadPacket(transceiver, addr64, outBytes, count + 4);
            } while (!success);
        }


        LOG.info("sending end packet");
        uploadPacket(transceiver, addr64, new int[]{'E', 'N'}, 2);

        System.out.print("\n");
        resetTarget(transceiver, addr64);

        if (this.junitSetOutputFileName == null) {
            transceiver.close();
        }
        System.out.println("\nDone");
        LOG.info("Done.");
    }

    private void prepareTarget(String comPort, XBeeTransceiver transceiver, XBeeAddress64 addr64) throws XBeeException, InterruptedException {

        if (this.junitSetOutputFileName != null) {
            return;
        }

        transceiver.open(comPort);
    }

    private void resetTarget(XBeeTransceiver transceiver, XBeeAddress64 addr64) throws XBeeException, InterruptedException {
        if (this.junitSetOutputFileName != null) {
            return;
        }

        try {
            transceiver.resetTarget(addr64);
        }
        catch (XBeeTimeoutException e) {
            LOG.info("reset timed out");
            System.err.println("Reset Timed-Out");
        }
    }

    private boolean uploadPacket(XBeeTransceiver transceiver, XBeeAddress64 addr64, int[] outBytes, int length) throws XBeeException, IOException {

        if (this.junitSetOutputFileName != null) {
            PrintWriter writer = new PrintWriter(new FileOutputStream(this.junitSetOutputFileName, true /*append*/));
            for (int i=0; i<2; i++) {
                writer.print((char)outBytes[i]);
            }
            if (length > 2) {
                writer.print(" ");
                for (int i=3; i>=2; i--) {
                    writer.print(String.format("%02X", outBytes[i]));
                }
                writer.print(" ");
                for (int i=4; i<length; i++) {
                    writer.print(String.format(" 0x%02X", outBytes[i]));
                }
            }
            writer.println();
            writer.close();
        } else {
            int[] payload = new int[length];
            System.arraycopy(outBytes, 0, payload, 0, length);
            transceiver.sendRequest(addr64, payload);
            return transceiver.waitAckResponse();
        }
        return true;
    }

    private static int byteToInt(byte b) {
        if (b >= 0) return (int)b;
        return 256 + ((int)b);
    }

    private static void readFile(String fileName, ArrayList<Integer> fileContents) throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

        HexBinaryAdapter adapter = new HexBinaryAdapter();
        String lineStr;
        while ((lineStr = reader.readLine()) != null)   {
            byte[]  inBytes = adapter.unmarshal(lineStr.substring(1));
            if (inBytes[3] == 0x01) {
                // end-marker line
                break;
            }
            for (int i=4; i<(inBytes.length-1); i++) {
                fileContents.add(byteToInt(inBytes[i]));
            }
        }
        reader.close();
    }

    private static void logIntArray(int[] outBytes) {
        StringBuffer logBuffer = new StringBuffer("sending: ");
        for (int b : outBytes)
        {
            logBuffer.append(String.format("0x%02X", b));
            logBuffer.append(" ");
        }
        LOG.info(logBuffer.toString());
    }

    /** Ask the Java Communications API * what ports it thinks it has. */
    public static void listComPorts() {
        // get list of ports available on this particular computer,
        // by calling static method in CommPortIdentifier.
        Enumeration pList = CommPortIdentifier.getPortIdentifiers();

        // Process the list.
        while (pList.hasMoreElements()) {
            CommPortIdentifier cpi = (CommPortIdentifier) pList.nextElement();
            StringBuffer sb = new StringBuffer();
            sb.append("Port ").append(cpi.getName()).append(" ");
            if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                sb.append("is a Serial Port.");
            } else if (cpi.getPortType() == CommPortIdentifier.PORT_PARALLEL) {
                sb.append("is a Parallel Port.");
            } else {
                sb.append("is an Unknown Port: ").append(cpi);
            }
            System.out.println(sb.toString());
            LOG.info(sb.toString());
        }
    }

    public static void main(String[] args) throws XBeeException, InterruptedException, IOException {
        try {
            PropertyConfigurator.configure("log4j.properties");
            if (args.length < 1) {
                System.err.println(HexUploader.class.getName() + "   List COM ports:  <-c>");
                System.err.println(HexUploader.class.getName() + "   List XBees:      <-n> <com port>");
                System.err.println(HexUploader.class.getName() + "   Upload firmware: <com port> <SL> <filename>");
                System.err.println(HexUploader.class.getName() + "   Read sensors:    <-r> <com port>");
                return;
            }
            if ("-c".equals(args[0])) {
                HexUploader.listComPorts();
                System.exit(0);
            }
            if ("-n".equals(args[0])) {
                String comPort = args[1];
                XBeeTransceiver transceiver = new XBeeTransceiver();
                transceiver.open(comPort);
                transceiver.printNodeIdentifiers();
                System.exit(0);
            }
            if ("-r".equals(args[0])) {
                String comPort = args[1];
                XBeeTransceiver transceiver = new XBeeTransceiver();
                transceiver.open(comPort);
                while(true) {
                    System.out.println(transceiver.receiveResponseText());
                    // wait for the user to CTRL-C
                }
            }
            if (args.length != 3) {
                System.err.println("3 arguments required");
                System.exit(1);
            }
            String comPort = args[0];
            int serLow[] = ByteUtils.convertInttoMultiByte(Integer.parseInt(args[1], 16));
            String fileName = args[2];
            if (serLow.length != 4) {
                System.err.println("Couldn't parse serial number low 4 bytes.");
                System.exit(1);
            }
            new HexUploader().parseHexFileAndUpload(comPort, serLow, fileName);
        }
        catch (Exception e) {
            LOG.error("Caught an exception", e);
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    public void junitSetOutputFileName(String outFileName) {
        this.junitSetOutputFileName = outFileName;
    }
}
