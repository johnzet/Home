package org.zehetner;


import java.io.*;
import java.net.*;
import java.util.*;

public class FirmwarePusher {

    private static boolean debug = false;
    private int[] remoteIpAddress;
    private DatagramSocket outUdpSocket;
    private int ACK = 0x79;
    private int NACK = 0x1F;

    OutputStream out;
    BufferedReader in;
    UdpReader udpReader;

    public FirmwarePusher() {
    }

    public FirmwarePusher(String remoteIpAddressStr, String filename) {
        Scanner scanner = new Scanner(remoteIpAddressStr);
        scanner.useDelimiter("\\.");
        this.remoteIpAddress = new int[4];
        this.remoteIpAddress[0] = scanner.nextInt();
        this.remoteIpAddress[1] = scanner.nextInt();
        this.remoteIpAddress[2] = scanner.nextInt();
        this.remoteIpAddress[3] = scanner.nextInt();
    }

    void init() throws SocketException, UnknownHostException {
        udpReader = new UdpReader();

        outUdpSocket = new DatagramSocket();
        outUdpSocket.setSoTimeout(10000);

        udpReader.init(this.outUdpSocket);
        udpReader.start();
    }

    private void sendRemoteAtCommandTcp(String atCommand, int[] arguments) throws InterruptedException, IOException {
        sendRemoteAtCommandByProtocol(atCommand, arguments, false);
    }

    private void sendRemoteAtCommand(String atCommand, int[] arguments) throws InterruptedException, IOException {
        sendRemoteAtCommandByProtocol(atCommand, arguments, true);
    }

    private void sendRemoteAtCommandByProtocol(String atCommand, int[] arguments, boolean byUdp) throws InterruptedException, IOException {
        int[] remoteCommand = new int[arguments.length + 12];
        remoteCommand[0] = 0x42;
        remoteCommand[1] = 0x42;
        remoteCommand[2] = 0x00;
        remoteCommand[3] = 0x00;

        remoteCommand[4] = 0x00;
        remoteCommand[5] = 0x00;
        remoteCommand[6] = /* command id*/0x02;
        remoteCommand[7] = /* cmd options */ 0x00;
        remoteCommand[8] = 0x01;
        remoteCommand[9] = 0x02;
        remoteCommand[10] = atCommand.getBytes()[0];
        remoteCommand[11] = atCommand.getBytes()[1];
        for (int i=0; i<arguments.length; i++) {
            remoteCommand[i+12] = arguments[i];
        }
        if (byUdp) {
            sendUdpPacket(remoteCommand);
        } else {
            sendTcpPacket(remoteCommand);
        }
    }

    private void sendRemoteData(int[] data) throws IOException, InterruptedException {
        int[] sendData = new int[data.length + 8];
        sendData[0] = 0x42;
        sendData[1] = 0x42;
        sendData[2] = 0x00;
        sendData[3] = 0x00;

        sendData[4] = 0x00;
        sendData[5] = 0x00;
        sendData[6] = /* command id*/0x00;
        sendData[7] = /* cmd options */ 0x00;
        for (int i=0; i<data.length; i++) {
            sendData[i+8] = data[i];
        }

        sendUdpPacket(sendData);
    }

    private void setRemoteDestinationAddressesTcp() throws IOException, InterruptedException {
        byte[] localIpAddress = Inet4Address.getLocalHost().getAddress();
        int[] charLocalIpAddress = new int[4];
        charLocalIpAddress[0] = (int)(localIpAddress[0] & 0xFF);
        charLocalIpAddress[1] = (int)(localIpAddress[1] & 0xFF);
        charLocalIpAddress[2] = (int)(localIpAddress[2] & 0xFF);
        charLocalIpAddress[3] = (int)(localIpAddress[3] & 0xFF);
        sendRemoteAtCommandTcp("DL", charLocalIpAddress);
    }

    private void setNormalBaudRate() throws IOException, InterruptedException {
        sendRemoteAtCommand("BD", new int[]{0x08});
    }

    private void setSlowBaudRate() throws IOException, InterruptedException {
        sendRemoteAtCommand("BD", new int[]{0x05});
    }

    private void setEvenParity() throws IOException, InterruptedException {
        sendRemoteAtCommand("NB", new int[]{0x01});
    }

    private void setNoParity() throws IOException, InterruptedException {
        sendRemoteAtCommand("NB", new int[]{0x00});
    }

    private void setTransparentMode() throws IOException, InterruptedException {
        sendRemoteAtCommand("AP", new int[]{0x00});
    }

    private void setApiMode() throws InterruptedException, IOException {
        sendRemoteAtCommand("AP", new int[]{0x02});
    }
    private void setRemoteUdpModeTcp() throws IOException, InterruptedException {
        sendRemoteAtCommandTcp("IP", new int[]{0x00});
    }

    private void setRemoteTcpMode() throws IOException, InterruptedException {
        sendRemoteAtCommand("IP", new int[]{0x01});
    }

    private void setPort(int[] port, boolean byUdp) throws IOException, InterruptedException {
        sendRemoteAtCommandByProtocol("C0", port, byUdp);
    }

    private void setDestPort(int[] port, boolean byUdp) throws IOException, InterruptedException {
        sendRemoteAtCommandByProtocol("DE", port, byUdp);
    }

    private void remoteDisableRtsAndCts() throws InterruptedException, IOException {
        sendRemoteAtCommand("D6", new int[]{0x00});
        sendRemoteAtCommand("D7", new int[]{0x00});
    }

    private void remoteEnableRtsAndCts() throws InterruptedException, IOException {
        sendRemoteAtCommand("D6", new int[]{0x01});
        sendRemoteAtCommand("D7", new int[]{0x01});
    }

    private void applyChanges(boolean byUdp) throws InterruptedException, IOException {
        sendRemoteAtCommandByProtocol("AC", new int[]{}, byUdp);
    }

    private void writeChanges(boolean byUdp) throws InterruptedException, IOException {
        sendRemoteAtCommandByProtocol("WR", new int[]{}, byUdp);
    }

    private void remoteReset() throws IOException, InterruptedException {
        // D0 = N-type gate - High connects WiFi reset
        // D1 = P-type gate - Low connects WiFi reset
        // D2 = board reset line
        // Normal:  D0-D2 = HLH (board and WiFi resets connected, reset high)
        // Reset:   D0-D2 = LHL (reset lines disconnected, reset low)
        // Boot0:   D3 (L = Normal, H = bootloader)
        sendRemoteAtCommand("D0", new int[]{0x05}); // 0x05 = digital out, pulled high
        sendRemoteAtCommand("D1", new int[]{0x04}); // 0x04 = digital out, pulled low
        sendRemoteAtCommand("D2", new int[]{0x05});
        sendRemoteAtCommand("D3", new int[]{0x04});
        sendRemoteAtCommand("OM", new int[]{0x0F});
        sendRemoteAtCommand("IO", new int[]{0b00000110}); // D0-D2 = LHH, Boot0 = L (resets disconnected, reset high)
        applyChanges(true);
        writeChanges(true);
        sendRemoteAtCommand("IO", new int[]{0b00001010}); // D0-D2 = LHL, Boot0 = H (reset low, bootloader selected)
        Thread.sleep(1);
        sendRemoteAtCommand("IO", new int[]{0b00001110}); // D0-D2 = LHH, Boot0 = H (reset high)

        Thread.sleep(1000);

    }

    private void resetAgain() throws IOException, InterruptedException {
        System.out.println("Resetting...");
        sendRemoteAtCommand("IO", new int[]{0b00001010}); // D0-D2 = LHL, Boot0 = H (reset low, bootloader selected)
        Thread.sleep(1);
        sendRemoteAtCommand("IO", new int[] {0b00001110}); // D0-D2 = LHH, Boot0 = H (reset high)
        Thread.sleep(1000);

        bootLoaderNegotiate();
    }

    private void resetWithFlashSelected() throws IOException, InterruptedException {
        System.out.println("Resetting with Flash selected...");
        sendRemoteAtCommand("IO", new int[]{0b00000110}); // D0-D2 = HHL, Boot0 = L (reset high, flash selected)
        Thread.sleep(1);
        sendRemoteAtCommand("IO", new int[]{0b00000010}); // D0-D2 = LHL, Boot0 = L (reset low, flash selected)
        Thread.sleep(1);
        sendRemoteAtCommand("IO", new int[]{0b00000110}); // D0-D2 = LHH, Boot0 = L (reset high)
        Thread.sleep(1000);
    }

    private void restoreResetPins() throws IOException, InterruptedException {
        sendRemoteAtCommand("IO", new int[]{0b00000101}); // resets connected, reset high, flash selected
        sendRemoteAtCommand("OM", new int[]{0x07, (int)0xFF});
    }

    private void remoteSoftwareReset() throws InterruptedException, IOException {
        sendRemoteAtCommand("FR", new int[]{});
    }

    private boolean bootLoaderNegotiate() throws IOException, InterruptedException {
        System.out.println("Negotiating....");
        this.udpReader.clearData();
        sendRemoteData(new int[]{0x7F});
        return readAck();
    }

    private int[] readMemory(int address, int length) throws IOException, InterruptedException {
        boolean success;
        int count = 0;
        int[] memoryints = null;
        do {
            success = true;
            try {
                sendRemoteData(new int[]{0x11, (int)0xEE});
                if (!readAck()) {
                    success = false;
                } else {

                    sendRemoteData(new int[]{
                            (int)((address & 0xFF000000) >> 24),
                            (int)((address & 0x00FF0000) >> 16),
                            (int)((address & 0x0000FF00) >> 8),
                            (int)((address & 0x000000FF)),
                            (int)((address & 0xFF000000) >> 24 ^ (address & 0x00FF0000) >> 16 ^ (address & 0x0000FF00) >> 8 ^ (address & 0x000000FF))
                    });
                    if (!readAck()) {
                        success = false;
                    } else {

                        sendRemoteData(new int[]{(int) (length & 0xFF), (int) (length ^ 0xFF)});
                        // if (!readAck()) return null;  //Ack is in the next packet

                        int[] memoryResponse = readUdpPacket(5000);
                        if (memoryResponse == null || memoryResponse.length<3) {
                            success = false;
                        } else {
                            memoryints = new int[memoryResponse.length - 2];
                            if (memoryints.length != length) {
                                success = false;
                            } else {
                                for (int i=1; i<(memoryResponse.length-1); i++) {
                                    memoryints[i-1] = memoryResponse[i];
                                }
                            }
                        }
                    }
                }
            }
            catch (Throwable t) {
                t.printStackTrace();
                success = false;
            }
            if (!success) resetAgain();
        } while (!success && count++ < 4);

        if (success) {
            return memoryints;
        } else {
            return null;
        }
    }

    private boolean writeMemory(int[] ints, int address, int length) throws IOException, InterruptedException {
        boolean success;
        int count = 0;
        do {
            success = false;
            try {
                sendRemoteData(new int[]{0x31, (int)0xCE});
                if (readAck()) {

                    sendRemoteData(new int[]{
                            (int)((address & 0xFF000000) >> 24),
                            (int)((address & 0x00FF0000) >> 16),
                            (int)((address & 0x0000FF00) >> 8),
                            (int)((address & 0x000000FF)),
                            (int)((address & 0xFF000000) >> 24 ^ (address & 0x00FF0000) >> 16 ^ (address & 0x0000FF00) >> 8 ^ (address & 0x000000FF))
                    });
                    if (readAck()) {
                        int cksum;
                        length += length % 4;
                        int[] memory = new int[length+2];
                        memory[0] = (int)((length-1) & 0xFF);
                        cksum = memory[0];

                        for (int i=0; i<length; i++) {
                            memory[i+1] = (int)(i>=length? 0xFF : ints[i]);
                            cksum ^= memory[i+1];
                        }
                        memory[memory.length-1] = cksum;
                        sendRemoteData(memory);  // send int count, data, cksum.  int count must be a multiple of 4;
                        if (readAck()) success = true;
                    }

                }
            }
            catch (Throwable t) {
                t.printStackTrace();
                success = false;
            }
            if (!success) resetAgain();
        } while (!success && count++ < 4);

        return success;
    }

    private boolean readAck() throws IOException {
        return readAck(5000);
    }

    private boolean readAck(int timeoutMs) throws IOException {
        int[] ack = readUdpPacket(timeoutMs);
        if (ack == null || ack.length <= 0) {
            System.out.println("Ack is null - may have timed out");
        } else if (ack[0] == NACK) {
            System.out.println("Received NACK");
        } else if (ack[0] == ACK) {
            return true;
        } else {
            System.out.println("Expecting ACK - Got 0x" + Integer.toHexString(ack[0]));
        }
        return false;
    }

    private boolean sendFirmware(String fileName) throws Throwable {
        //***********************************************************************************************
        if (!bootLoaderNegotiate()) return false;

        getBootloaderType();

        if (!eraseFirmware()) {
            return false;
        }

//        // verify erase
//        if (!verifyFirmware(fileName, true)) {
//            return false;
//        }


        if (!writeFirmware(fileName)) {
            return false;
        }

        if (!verifyFirmware(fileName, false)) {
            return false;
        }

        return true;
        //************************************************************************************************
    }

    private void getBootloaderType() throws IOException, InterruptedException {
        System.out.println("Getting bootloader type");

        this.udpReader.clearData();
        sendRemoteData(new int[]{0x02, 0xFD});
        readAck();

        Thread.sleep(1000);

    }

    private boolean eraseFirmware() throws Throwable {
        System.out.println("Erasing sectors 0-5 (0x0800 0000 - 0x0803 FFFF) - HARDCODED RANGE");

        int count = 0;
        boolean success;
        do {
            this.udpReader.clearData();
            success = true;
            sendRemoteData(new int[]{0x44, 0xBB});
            if (!readAck()) {
                success = false;
            } else {
                sendRemoteData(new int[] {0x00, 0x06, 0x00, 0x00, 0x00, 0x01, 0x00, 0x02, 0x00, 0x03, 0x00, 0x04, 0x00, 0x05, 0x00, 0x07, 0x00});
                if (!readAck(20000)) success = false;
            }
            if (!success) resetAgain();
        } while (!success && count++ < 3);

        if (success) {
            System.out.println("Erase SUCCESSFUL");
        } else {
            System.out.println("Erase FAILED");
        }

        return success;
    }

    private boolean writeFirmware(String fileName) throws Throwable {
        System.out.println("Writing....");

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        int recordType;
        int highAddress = 0;
        int lowAddress = -1;
        int[] buffer = new int[256];
        int bufferLength = 0;
        do {
            line = reader.readLine();
            if (!line.startsWith(":")) throw new IllegalArgumentException("Bad hex file format");
            recordType = Integer.parseInt(line.substring(7, 9), 16);
//            int chksum = Integer.parseInt(line.substring(3+(intCount*2), 3+(intCount*2)+2), 16);

            if (recordType == 2 || recordType == 4) {
                if (lowAddress >= 0 && bufferLength > 0) {
                    if (!writeChunk(buffer, bufferLength, highAddress, lowAddress)) return false;
                    lowAddress = -1; bufferLength = 0;
                }
                highAddress = Integer.parseInt(line.substring(9, 13), 16);
            } else if (recordType == 0) {
                int intCount = Integer.parseInt(line.substring(1, 3), 16);
                if (lowAddress < 0) lowAddress = Integer.parseInt(line.substring(3, 7), 16);

                if (bufferLength + intCount > 240) {
                    if (!writeChunk(buffer, bufferLength, highAddress, lowAddress)) return false;
                    lowAddress = Integer.parseInt(line.substring(3, 7), 16); bufferLength = 0;
                }

                for (int i=0; i<intCount; i++) {
                    buffer[bufferLength++] = (Integer.parseInt(line.substring(9 + 2*i, 11 + 2*i), 16) & 0xFF);
                }

            }  else {
                if (lowAddress >= 0 && bufferLength > 0) {
                    if (!writeChunk(buffer, bufferLength, highAddress, lowAddress)) return false;
                    lowAddress = -1; bufferLength = 0;
                }
            }
        } while (recordType != 1 /* EOF */);
        System.out.println("Write SUCCESSFUL");

        return true;
    }

    private boolean writeChunk(final int[] ints, final int intCount, final int highAddress, final int lowAddress) throws IOException, InterruptedException {
//        System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
        System.out.println("  Writing 0x" + Integer.toHexString((highAddress << 16) + lowAddress));

        this.udpReader.clearData();
        boolean success = writeMemory(ints, (highAddress << 16) + lowAddress, intCount);

        if (! success) {
            System.out.println("Write failed - Subject to possible retry");
        }

        return success;
    }

    private boolean verifyFirmware(String fileName, boolean verifyErase) throws Throwable {
        System.out.println("Verifying....");

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        int recordType;
        int highAddress = 0;
        int lowAddress = -1;
        int[] buffer = new int[256];
        int bufferLength = 0;
        do {
            line = reader.readLine();
            if (!line.startsWith(":")) throw new IllegalArgumentException("Bad hex file format");
            recordType = Integer.parseInt(line.substring(7, 9), 16);
//            int chksum = Integer.parseInt(line.substring(3+(intCount*2), 3+(intCount*2)+2), 16);

            if (recordType == 2 || recordType == 4) {
                if (lowAddress >= 0 && bufferLength > 0) {
                    if (!verifyChunk(buffer, bufferLength, highAddress, lowAddress)) return false;
                    lowAddress = -1; bufferLength = 0;
                }
                highAddress = Integer.parseInt(line.substring(9, 13), 16);
            } else if (recordType == 0) {
                int intCount = Integer.parseInt(line.substring(1, 3), 16);
                if (lowAddress < 0) lowAddress = Integer.parseInt(line.substring(3, 7), 16);

                if (bufferLength + intCount > 240) {
                    if (!verifyChunk(buffer, bufferLength, highAddress, lowAddress)) return false;
                    lowAddress = Integer.parseInt(line.substring(3, 7), 16); bufferLength = 0;
                }

                for (int i=0; i<intCount; i++) {
                    if (verifyErase) {
                        buffer[bufferLength++] = 0xFF;
                    } else {
                        buffer[bufferLength++] = (Integer.parseInt(line.substring(9 + 2*i, 11 + 2*i), 16) & 0xFF);
                    }
                }

            }  else {
                if (lowAddress >= 0 && bufferLength > 0) {
                    if (!verifyChunk(buffer, bufferLength, highAddress, lowAddress)) return false;
                    lowAddress = -1; bufferLength = 0;
                }
            }
        } while (recordType != 1 /* EOF */);
        System.out.println("\nVerify SUCCESSFUL");

        return true;
    }

    private boolean verifyChunk(int[] buffer, int bufferLength, final int highAddress, final int lowAddress) {
        boolean success;
        System.out.println("  Verifying 0x" + Integer.toHexString((highAddress << 16) + lowAddress));
        success = true;
        try {
            this.udpReader.clearData();
            int[] memoryints = readMemory((highAddress << 16) + lowAddress, bufferLength);

            if (memoryints == null || memoryints.length != bufferLength) {
                System.out.println("Retrieved the wrong number of ints");
                success = false;
            } else {

                for (int i=0; i<bufferLength; i++) {
                    if (buffer[i] != memoryints[i]) {
                        System.out.println("\nVerify failed at 0x" + (Integer.toHexString((highAddress << 16) + lowAddress) + "  Offset: 0x" + Integer.toHexString(i)));
                        System.out.println("Expected = 0x" + Integer.toHexString(buffer[i]) + "  Actual = 0x" + Integer.toHexString(memoryints[i]));
                        success = false;
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            success = false;
        }
        return success;
    }

    private boolean pushFirmware(String fileName) throws IOException, InterruptedException {

        try {

            setPort(new int[]{0x0B, (int)0xEE}, true);
            applyChanges(true);
            writeChanges(true);

            String hostName = remoteIpAddress[0] + "." + remoteIpAddress[1] + "." + remoteIpAddress[2] + "." + remoteIpAddress[3];
            Socket tcpSocket = new Socket(hostName, 0x0BEE);
            tcpSocket.setTcpNoDelay(true);
            tcpSocket.setSoTimeout(10000);
            out = tcpSocket.getOutputStream();
            in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

            setRemoteDestinationAddressesTcp();
            setRemoteUdpModeTcp();
            applyChanges(false);
            writeChanges(false);

            setApiMode();
            sendRemoteAtCommand("DO", new int[]{0x14});  // device options
            applyChanges(true);
            writeChanges(true);

            remoteReset();

            remoteDisableRtsAndCts();
            setSlowBaudRate();
            setDestPort(new int[] {(this.outUdpSocket.getLocalPort() & 0xff00) >> 8, this.outUdpSocket.getLocalPort() & 0x00ff}, true);
            setEvenParity();
            setTransparentMode();
            applyChanges(true);
            writeChanges(true);


            if (!sendFirmware(fileName)) {
                System.out.println("Firmware upload was NOT successful");
            } else {
                System.out.println("Firmware upload was SUCCESSFUL");
            }

            out.close();
            in.close();
            tcpSocket.close();

            System.out.println("Done.");
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
        return true;
    }

    public void restoreConfig() {
        System.out.println("Restoring...");
        try {
            setNormalBaudRate();
            applyChanges(true);
            writeChanges(true);

            setApiMode();
            setNoParity();
            applyChanges(true);
            writeChanges(true);

            resetWithFlashSelected();  // resets the board, not the WiFi
            remoteEnableRtsAndCts();
            restoreResetPins();

            setPort(new int[]{80}, true);
            setDestPort(new int[]{0x0B, 0xEE}, true);
            setRemoteTcpMode();
            applyChanges(true);
            writeChanges(true);
            remoteSoftwareReset();  // takes at least 2 seconds, but does cause a join notification

            this.outUdpSocket.close();
            this.udpReader.close();

            System.out.println("Restore Done.");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void sendTcpPacket(int[] buffer) throws IOException {
        this.out.write(convertIntArrayToByteArray(buffer));
        this.out.flush();

        if (debug) System.out.println("   ---   TCP sent packet:     " + hexToString(buffer));
    }

    private void sendUdpPacket(int[] buffer) throws IOException, InterruptedException {
        byte[] bytes = new byte[buffer.length];
        for (int i=0; i<buffer.length; i++) bytes[i] = (byte)buffer[i];
        byte[] intAddr = new byte[] {(byte)this.remoteIpAddress[0], (byte)this.remoteIpAddress[1], (byte)this.remoteIpAddress[2], (byte)this.remoteIpAddress[3]};
        Inet4Address addr = (Inet4Address)Inet4Address.getByAddress(intAddr);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, addr, 0x0BEE);

        this.outUdpSocket.send(packet);

        if (debug) System.out.println("   ---   UDP sent packet:     " + hexToString((int[])buffer));


        Thread.sleep(10);
    }

    private int[] convertByteArrayToIntArray(byte[] byteArray) {
        int[] intArray = new int[byteArray.length];
        for (int i=0; i<byteArray.length; i++) {
            if (byteArray[i] >= 0) {
                intArray[i] = byteArray[i];
            } else {
                intArray[i] = byteArray[i] + 256;
            }
        }
        return intArray;
    }

    private byte[] convertIntArrayToByteArray(int[] intArray) {
        byte[] byteArray = new byte[intArray.length];
        for (int i=0; i<intArray.length; i++) {
            byteArray[i] = (byte)intArray[i];
        }
        return byteArray;
    }

    private char[] convertIntArrayToCharArray(int[] intArray) {
        char[] charArray = new char[intArray.length];
        for (int i=0; i<intArray.length; i++) {

            //charArray[i] = new String(new char[] {(char)(intArray[i])}).codePointAt(0);

            char c = (char)intArray[i];
            Character cc = new Character(c);

            //charArray[i] = new String(convertIntArrayToByteArray(intArray));
        }
        return charArray;
    }

    String hexToString(int[] buffer) {
        String retStr = "";
        for (int i=0; i<buffer.length; i++) {
            retStr += " ";
            int intint = buffer[i];
            if (intint < 0) intint = buffer[i] + 256;
            retStr += Integer.toHexString(intint);
        }
        return retStr;
    }

    private int[] readUdpPacket(int timeoutMs) throws IOException {
        int count = 0;
        int[] data;
        do {
            data = this.udpReader.getData();
            if (data == null || data.length == 0) {
                try {Thread.sleep(100);} catch (InterruptedException ie) {/* do nothing */}
            }
        } while (count++ < timeoutMs/100 && (data == null || data.length == 0));
        return data;
    }

    public static void main ( String[] args )
    {
//        System.out.println("args length: " + args.length);
//        System.out.println("arg 0 = " + args[0]);

        try
        {
            if (args.length == 0 || "-h".equals(args[0]) || "-help".equals(args[0])) {
                System.out.println("Usage:\tFirmwarePusher -h\r\n" +
                                       "\t\tFirmwarePusher -help\r\n" +
                                       "\t\tFirmwarePusher <Remote IP Address> -r (restore WiFI config) [-d] \r\n" +
                                       "\t\tFirmwarePusher <Remote IP Address> <firmware filename> [-d] \r\n");
                System.exit(1);
            }


            FirmwarePusher pusher = new FirmwarePusher(args[0].trim(), args[1].trim());
            pusher.init();
            Thread.sleep(100); // wait for udp reader to start

            if (args.length >= 3 && "-d".equals(args[2])) debug = true;
            if ("-r".equals(args[1])) {
                pusher.restoreConfig();
            } else {
                pusher.pushFirmware(args[1].trim());
                pusher.restoreConfig();
            }

            pusher.outUdpSocket.close();
            pusher.udpReader.close();


            pusher.udpReader.kill();
            pusher.udpReader.join();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    private class UdpReader extends Thread {
        private final List<Integer> packetData = new ArrayList<>();
        private int packetLength = 0;
        private DatagramSocket inUdpSocket;
        private boolean stop = false;
        private boolean scheduleClose = false;

        public UdpReader() {
            setName("UdpReader");
        }

        public void init(DatagramSocket socket) {
            try {
                this.inUdpSocket = socket;
                this.inUdpSocket.setSoTimeout(10000);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        public void clearData() {
            synchronized (packetData) {
                this.packetData.clear();
                this.packetLength = 0;
            }
        }

        public int[] getData() {
            synchronized (packetData) {
                int[] returnints = new int[packetLength];
                for (int i=0; i<packetLength; i++) {
                    returnints[i] = packetData.get(i);
                }
                clearData();
                return returnints;
            }
        }

        public synchronized void close() {
            this.scheduleClose = true;
        }

        public void kill() {
            this.inUdpSocket.disconnect();
            this.scheduleClose = true;
            this.stop = true;
        }

        @Override
        public void run() {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            do {
                try {
                    if (this.inUdpSocket != null && !this.inUdpSocket.isClosed()) {
                        try {
                            this.inUdpSocket.receive(packet);
                            synchronized (packetData) {
                                packetData.clear();
                                for (int i=0; i<packet.getLength(); i++) {
                                    int packetByte = packet.getData()[i];
                                    if (packetByte < 0) packetByte = (packetByte + 256) & 0xFF;
                                    packetData.add(packetByte);
                                }
                                packetLength = packet.getLength();
                                if (debug) System.out.println("   ---   UDP received packet: " + hexToString(packetData.stream().mapToInt(i->i).toArray()));
                            }
                        } catch (SocketException se) {
                            if (this.inUdpSocket.isClosed()) {
                                this.stop = true;
                            } else {
                                throw se;
                            }
                        } catch (SocketTimeoutException e) {
//                            System.out.println("UDP read timeout");
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                if (this.scheduleClose) {
                    if (this.inUdpSocket != null && !this.inUdpSocket.isClosed()) this.inUdpSocket.close();
                    this.scheduleClose = false;
                }
                Thread.yield();
            } while (!this.stop);
        }
    }
}