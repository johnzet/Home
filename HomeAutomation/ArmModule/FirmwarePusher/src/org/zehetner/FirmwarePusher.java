package org.zehetner;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FirmwarePusher {

    private int[] remoteIpAddress;
    private DatagramSocket outUdpSocket;
    private byte ACK = 0x79;
    private byte NACK = 0x1F;

    PrintWriter out;
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

        udpReader = new UdpReader();
        udpReader.start();

    }

    private void sendRemoteAtCommandTcp(String atCommand, char[] arguments) throws InterruptedException, IOException {
        sendRemoteAtCommandByProtocol(atCommand, arguments, false);
    }

    private void sendRemoteAtCommand(String atCommand, char[] arguments) throws InterruptedException, IOException {
        sendRemoteAtCommandByProtocol(atCommand, arguments, true);
    }

    private void sendRemoteAtCommandByProtocol(String atCommand, char[] arguments, boolean byUdp) throws InterruptedException, IOException {
        char[] remoteCommand = new char[arguments.length + 12];
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
        remoteCommand[10] = atCommand.charAt(0);
        remoteCommand[11] = atCommand.charAt(1);
        for (int i=0; i<arguments.length; i++) {
            remoteCommand[i+12] = arguments[i];
        }
        if (byUdp) {
            sendUdpPacket(remoteCommand);
        } else {
            sendTcpPacket(remoteCommand);
        }
    }

    private void sendRemoteData(char[] data) throws IOException {
        char[] sendData = new char[data.length + 8];
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
        char[] charLocalIpAddress = new char[4];
        charLocalIpAddress[0] = (char)(localIpAddress[0] & 0xFF);
        charLocalIpAddress[1] = (char)(localIpAddress[1] & 0xFF);
        charLocalIpAddress[2] = (char)(localIpAddress[2] & 0xFF);
        charLocalIpAddress[3] = (char)(localIpAddress[3] & 0xFF);
        sendRemoteAtCommandTcp("DL", charLocalIpAddress);
    }

    private void setBaudRate() throws IOException, InterruptedException {
        sendRemoteAtCommand("BD", new char[]{0x08});
    }

    private void setEvenParity() throws IOException, InterruptedException {
        sendRemoteAtCommand("NB", new char[]{0x01});
    }

    private void setNoParity() throws IOException, InterruptedException {
        sendRemoteAtCommand("NB", new char[]{0x00});
    }

    private void setTransparentMode() throws IOException, InterruptedException {
        sendRemoteAtCommand("AP", new char[]{0x00});
    }

    private void setApiMode() throws InterruptedException, IOException {
        sendRemoteAtCommand("AP", new char[]{0x02});
    }
    private void setRemoteUdpMode() throws IOException, InterruptedException {
        sendRemoteAtCommandTcp("IP", new char[]{0x00});
    }

    private void setRemoteTcpMode() throws IOException, InterruptedException {
        sendRemoteAtCommand("IP", new char[]{0x01});
    }

    private void setPort(char[] port) throws IOException, InterruptedException {
        sendRemoteAtCommand("C0", port);
    }

    private void remoteDisableRtsAndCts() throws InterruptedException, IOException {
        sendRemoteAtCommand("D6", new char[]{0x00});
        sendRemoteAtCommand("D7", new char[]{0x00});
    }

    private void remoteEnableRtsAndCts() throws InterruptedException, IOException {
        sendRemoteAtCommand("D6", new char[]{0x01});
        sendRemoteAtCommand("D7", new char[]{0x01});
    }

    private void applyChangesTcp() throws InterruptedException, IOException {
        sendRemoteAtCommandTcp("AC", new char[]{});
    }

    private void applyChanges() throws InterruptedException, IOException {
        sendRemoteAtCommand("AC", new char[]{});
    }

    private void writeChanges() throws InterruptedException, IOException {
        sendRemoteAtCommand("WR", new char[]{});
    }

    private void remoteReset() throws IOException, InterruptedException {
        // D0 = N-type gate - High connects WiFi reset
        // D1 = P-type gate - Low connects WiFi reset
        // D2 = board reset line
        // Normal:  D0-D2 = HLH (board and WiFi resets connected, reset high)
        // Reset:   D0-D2 = LHL (reset lines disconnected, reset low)
        // Boot0:   D3 (L = Normal, H = bootloader)
        sendRemoteAtCommand("D0", new char[]{0x05}); // 0x05 = digital out, pulled high
        sendRemoteAtCommand("D1", new char[]{0x04}); // 0x04 = digital out, pulled low
        sendRemoteAtCommand("D2", new char[]{0x05});
        sendRemoteAtCommand("D3", new char[]{0x04});
        sendRemoteAtCommand("OM", new char[]{0x0F});
        sendRemoteAtCommand("IO", new char[]{0b00000110}); // D0-D2 = LHH, Boot0 = L (resets disconnected, reset high)
        applyChanges();
        sendRemoteAtCommand("IO", new char[]{0b00001010}); // D0-D2 = LHL, Boot0 = H (reset low, bootloader selected)
        Thread.sleep(1);
        sendRemoteAtCommand("IO", new char[]{0b00001110}); // D0-D2 = LHH, Boot0 = H (reset high)

        Thread.sleep(1000);

    }

    private void resetAgain() throws IOException, InterruptedException {
        System.out.println("Resetting...");
        sendRemoteAtCommand("IO", new char[]{0b00001010}); // D0-D2 = LHL, Boot0 = H (reset low, bootloader selected)
        Thread.sleep(1);
        sendRemoteAtCommand("IO", new char[] {0b00001110}); // D0-D2 = LHH, Boot0 = H (reset high)
        Thread.sleep(1000);

        bootLoaderNegotiate();
    }

    private void resetWithFlashSelected() throws IOException, InterruptedException {
        System.out.println("Resetting with Flash selected...");
        sendRemoteAtCommand("IO", new char[]{0b00000110}); // D0-D2 = HHL, Boot0 = L (reset high, flash selected)
        Thread.sleep(1);
        sendRemoteAtCommand("IO", new char[]{0b00000010}); // D0-D2 = LHL, Boot0 = L (reset low, flash selected)
        Thread.sleep(1);
        sendRemoteAtCommand("IO", new char[]{0b00000110}); // D0-D2 = LHH, Boot0 = L (reset high)
        Thread.sleep(1000);
    }

    private void restoreResetPins() throws IOException, InterruptedException {
        sendRemoteAtCommand("IO", new char[]{0b00000101}); // resets connected, reset high, flash selected
        sendRemoteAtCommand("OM", new char[]{0x07, 0xFF});
    }

    private void remoteSoftwareReset() throws InterruptedException, IOException {
        sendRemoteAtCommand("FR", new char[]{});
    }

    private boolean bootLoaderNegotiate() throws IOException, InterruptedException {
        System.out.println("Negotiating....");
        sendRemoteData(new char[]{0x7F});
        return readAck();
    }

    private int[] readMemory(int address, int length) throws IOException, InterruptedException {
        boolean success;
        int count = 0;
        int[] memoryBytes = null;
        do {
            success = true;
            try {
                sendRemoteData(new char[]{0x11, 0xEE});
                if (!readAck()) {
                    success = false;
                } else {

                    sendRemoteData(new char[]{
                            (char)((address & 0xFF000000) >> 24),
                            (char)((address & 0x00FF0000) >> 16),
                            (char)((address & 0x0000FF00) >> 8),
                            (char)((address & 0x000000FF)),
                            (char)((address & 0xFF000000) >> 24 ^ (address & 0x00FF0000) >> 16 ^ (address & 0x0000FF00) >> 8 ^ (address & 0x000000FF))
                    });
                    if (!readAck()) {
                        success = false;
                    } else {

                        sendRemoteData(new char[]{(char) (length & 0xFF), (char) (length ^ 0xFF)});
                        // if (!readAck()) return null;  //Ack is in the next packet

                        int[] memoryResponse = readUdpPacket(5000);
                        if (memoryResponse == null || memoryResponse.length<3) {
                            success = false;
                        } else {
                            memoryBytes = new int[memoryResponse.length - 2];
                            if (memoryBytes.length != length) {
                                success = false;
                            } else {
                                for (int i=1; i<(memoryResponse.length-1); i++) {
                                    memoryBytes[i-1] = memoryResponse[i];
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
            return memoryBytes;
        } else {
            return null;
        }
    }

    private boolean writeMemory(int[] bytes, int address, int length) throws IOException, InterruptedException {
        boolean success;
        int count = 0;
        do {
            success = false;
            try {
                sendRemoteData(new char[]{0x31, 0xCE});
                if (readAck()) {

                    sendRemoteData(new char[]{
                            (char)((address & 0xFF000000) >> 24),
                            (char)((address & 0x00FF0000) >> 16),
                            (char)((address & 0x0000FF00) >> 8),
                            (char)((address & 0x000000FF)),
                            (char)((address & 0xFF000000) >> 24 ^ (address & 0x00FF0000) >> 16 ^ (address & 0x0000FF00) >> 8 ^ (address & 0x000000FF))
                    });
                    if (readAck()) {
                        char cksum;
                        length += length % 4;
                        char[] memory = new char[length+2];
                        memory[0] = (char)((length-1) & 0xFF);
                        cksum = memory[0];

                        for (int i=0; i<length; i++) {
                            memory[i+1] = (char)(i>=length? 0xFF : bytes[i]);
                            cksum ^= memory[i+1];
                        }
                        memory[memory.length-1] = cksum;
                        sendRemoteData(memory);  // send byte count, data, cksum.  Byte count must be a multiple of 4;
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

    private boolean eraseFirmware() throws Throwable {
        System.out.println("Erasing sectors 0-5 (0x0800 0000 - 0x0803 FFFF)");

        int count = 0;
        boolean success;
        do {
            this.udpReader.clearData();
            success = true;
            sendRemoteData(new char[]{0x44, 0xBB});
            if (!readAck()) {
                success = false;
            } else {
                sendRemoteData(new char[] {0x00, 0x06, 0x00, 0x00, 0x00, 0x01, 0x00, 0x02, 0x00, 0x03, 0x00, 0x04, 0x00, 0x05, 0x00, 0x07, 0x00});
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
//            int chksum = Integer.parseInt(line.substring(3+(byteCount*2), 3+(byteCount*2)+2), 16);

            if (recordType == 2 || recordType == 4) {
                if (lowAddress >= 0 && bufferLength > 0) {
                    if (!writeChunk(buffer, bufferLength, highAddress, lowAddress)) return false;
                    lowAddress = -1; bufferLength = 0;
                }
                highAddress = Integer.parseInt(line.substring(9, 13), 16);
            } else if (recordType == 0) {
                int byteCount = Integer.parseInt(line.substring(1, 3), 16);
                if (lowAddress < 0) lowAddress = Integer.parseInt(line.substring(3, 7), 16);

                if (bufferLength + byteCount > 240) {
                    if (!writeChunk(buffer, bufferLength, highAddress, lowAddress)) return false;
                    lowAddress = Integer.parseInt(line.substring(3, 7), 16); bufferLength = 0;
                }

                for (int i=0; i<byteCount; i++) {
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

    private boolean writeChunk(final int[] bytes, final int byteCount, final int highAddress, final int lowAddress) throws IOException, InterruptedException {
        System.out.print("  Writing 0x" + Integer.toHexString((highAddress << 16) + lowAddress)
                + "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");

        this.udpReader.clearData();
        boolean success = writeMemory(bytes, (highAddress << 16) + lowAddress, byteCount);

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
//            int chksum = Integer.parseInt(line.substring(3+(byteCount*2), 3+(byteCount*2)+2), 16);

            if (recordType == 2 || recordType == 4) {
                if (lowAddress >= 0 && bufferLength > 0) {
                    if (!verifyChunk(buffer, bufferLength, highAddress, lowAddress)) return false;
                    lowAddress = -1; bufferLength = 0;
                }
                highAddress = Integer.parseInt(line.substring(9, 13), 16);
            } else if (recordType == 0) {
                int byteCount = Integer.parseInt(line.substring(1, 3), 16);
                if (lowAddress < 0) lowAddress = Integer.parseInt(line.substring(3, 7), 16);

                if (bufferLength + byteCount > 240) {
                    if (!verifyChunk(buffer, bufferLength, highAddress, lowAddress)) return false;
                    lowAddress = Integer.parseInt(line.substring(3, 7), 16); bufferLength = 0;
                }

                for (int i=0; i<byteCount; i++) {
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
        System.out.print("  Verifying 0x" + Integer.toHexString((highAddress << 16) + lowAddress)
                + "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
        success = true;
        try {
            this.udpReader.clearData();
            int[] memoryBytes = readMemory((highAddress << 16) + lowAddress, bufferLength);

            if (memoryBytes == null || memoryBytes.length != bufferLength) {
                System.out.println("Retrieved the wrong number of bytes");
                success = false;
            } else {

                for (int i=0; i<bufferLength; i++) {
                    if (buffer[i] != memoryBytes[i]) {
                        System.out.println("\nVerify failed at 0x" + (Integer.toHexString((highAddress << 16) + lowAddress) + "  Offset: 0x" + Integer.toHexString(i)));
                        System.out.println("Expected = 0x" + Integer.toHexString(buffer[i]) + "  Actual = 0x" + Integer.toHexString(memoryBytes[i]));
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

            this.udpReader.init();

            this.outUdpSocket = new DatagramSocket();
            this.outUdpSocket.setSoTimeout(10000);

            setPort(new char[]{0x0B, 0xEE});
            applyChanges();
            writeChanges();



            String hostName = remoteIpAddress[0] + "." + remoteIpAddress[1] + "." + remoteIpAddress[2] + "." + remoteIpAddress[3];
            Socket tcpSocket = new Socket(hostName, 0xBEE);
            tcpSocket.setTcpNoDelay(true);
            tcpSocket.setSoTimeout(10000);
            out = new PrintWriter(tcpSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

            setRemoteDestinationAddressesTcp();
            setRemoteUdpMode();
            applyChangesTcp();
            Thread.sleep(1000);

            out.close();
            in.close();
            tcpSocket.close();


            setApiMode();
            sendRemoteAtCommand("DO", new char[]{0x14});
            remoteDisableRtsAndCts();
            applyChanges();

            remoteReset();

            setBaudRate();
            setEvenParity();
            setTransparentMode();
            applyChanges();

            Thread.sleep(1000);

            if (!sendFirmware(fileName)) {
                System.out.println("Firmware upload was NOT successful");
            } else {
                System.out.println("Firmware upload was SUCCESSFUL");
            }

//            this.outUdpSocket.close();
//            this.udpReader.close();

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
//            this.outUdpSocket = new DatagramSocket();
//            this.outUdpSocket.setSoTimeout(10000);
//            this.udpReader.init();

            setApiMode();
            setNoParity();
            applyChanges();
            writeChanges();

            resetWithFlashSelected();  // resets the board, not the WiFi
            remoteEnableRtsAndCts();
            restoreResetPins();

            setPort(new char[]{80});
            setRemoteTcpMode();
            applyChanges();
            writeChanges();
            remoteSoftwareReset();  // takes at least 2 seconds, but does cause a join notification

            this.outUdpSocket.close();
            this.udpReader.close();

            System.out.println("Restore Done.");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void sendTcpPacket(char[] buffer) {
        this.out.write(buffer);
        this.out.flush();
    }

    private void sendUdpPacket(char[] buffer) throws IOException {
        byte[] bytes = new byte[buffer.length];
        for (int i=0; i<buffer.length; i++) bytes[i] = (byte)buffer[i];
        byte[] byteAddr = new byte[] {(byte)this.remoteIpAddress[0], (byte)this.remoteIpAddress[1], (byte)this.remoteIpAddress[2], (byte)this.remoteIpAddress[3]};
        Inet4Address addr = (Inet4Address)Inet4Address.getByAddress(byteAddr);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, addr, 0xBEE);

        this.outUdpSocket.send(packet);
    }

    private int[] readUdpPacket(int timeoutMs) throws IOException {
        int count = 0;
        int[] data;
        do {
            try {Thread.sleep(5);} catch (InterruptedException ie) {/* do nothing */}
            data = this.udpReader.getData();
        } while (count++ < timeoutMs/10 && (data == null || data.length == 0));
        return data;
    }

    public static void main ( String[] args )
    {
//        System.out.println("args length: " + args.length);
//        System.out.println("arg 0 = " + args[0]);

        try
        {
            if ("-h".equals(args[0]) || "-help".equals(args[0]) || args.length != 2) {
                System.out.println("Usage:\tFirmwarePusher -h\r\n" +
                                       "\t\tFirmwarePusher -help\r\n" +
                                       "\t\tFirmwarePusher <Remote IP Address> -r (restore WiFI config)\r\n" +
                                       "\t\tFirmwarePusher <Remote IP Address> <firmware filename>\r\n");
                System.exit(1);
            }


            FirmwarePusher pusher = new FirmwarePusher(args[0].trim(), args[1].trim());

            if ("-r".equals(args[1])) {
                pusher.udpReader.init();

                pusher.outUdpSocket = new DatagramSocket();
                pusher.outUdpSocket.setSoTimeout(10000);

                pusher.restoreConfig();
            } else {
                pusher.pushFirmware(args[1].trim());
                pusher.restoreConfig();
            }

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

        public void init() {
            try {
                this.inUdpSocket = new DatagramSocket(0x2616);
                this.inUdpSocket.setSoTimeout(1000);
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
                int[] returnBytes = new int[packetLength];
                for (int i=0; i<packetLength; i++) {
                    returnBytes[i] = packetData.get(i);
                }
                clearData();
                return returnBytes;
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
                            byte[] data = packet.getData();
                            synchronized(packetData) {
                                packetData.clear();
                                packetLength = packet.getLength();
                                for (int i=0; i<packetLength; i++) {
                                    packetData.add(data[i] & 0xFF);
                                }
                            }
                        } catch (SocketTimeoutException e) {
                            // ignore
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