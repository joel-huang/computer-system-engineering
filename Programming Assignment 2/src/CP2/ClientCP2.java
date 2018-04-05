package CP2;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class ClientCP2 {
    public static void main(String[] args) {

        String filename = "test3.txt";

        int numBytes = 0;

        Socket clientSocket = null;

        DataOutputStream toServer = null;
        DataInputStream fromServer = null;

        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedFileInputStream = null;

        PrintWriter out = null;
        BufferedReader in = null;

        long timeStarted = System.nanoTime();

        try {

            System.out.println("Establishing connection to server...");

            // Connect to server and get the input and output streams
            clientSocket = new Socket("127.0.0.1", 4321);

            toServer = new DataOutputStream(clientSocket.getOutputStream());
            fromServer = new DataInputStream(clientSocket.getInputStream());

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Set up protocol
            ClientProtocol clientProtocol = new ClientProtocol("CA.crt");

            out.println("Requesting authentication...");
            System.out.println("Requesting authentication...");

            // Generate nonce
            System.out.println("Generating nonce...");
            clientProtocol.generateNonce();

            // Send nonce to sever
            System.out.println("Sending nonce to server...");
            toServer.write(clientProtocol.getNonce());

            // Retrieve encrypted nonce from server
            fromServer.read(clientProtocol.getEncryptedNonce());
            System.out.println("Retrieved encrypted nonce from server...");

            // Send certificate request to server
            System.out.println("Requesting certificate from server...");
            out.println("Request certificate...");


            clientProtocol.getCertificate(fromServer);
            System.out.println("Validating certificate...");
            clientProtocol.verifyCert();
            System.out.println("Certificate validated");


            System.out.println("Verifying server...");
            // Get public key
            clientProtocol.getPublicKey();

            // Decrypt encrypted nonce
            byte[] decryptedNonce = clientProtocol.decryptNonce(clientProtocol.getEncryptedNonce());

            if (clientProtocol.validateNonce(decryptedNonce)){
                System.out.println("Server verified");
                out.println("Server verified");
            } else{
                System.out.println("Server verification faile");
                System.out.println("Closing all connections...");
                toServer.close();
                fromServer.close();
                clientSocket.close();
            }

            System.out.println("Starting CP-2 session...");

            // encrypt the file with session key and return the encrypted session key
            byte[] encryptedSessionKey = clientProtocol.encryptSessionKeyAndFile(Files.readAllBytes(Paths.get(filename)));
            System.out.println(DatatypeConverter.printBase64Binary(encryptedSessionKey));

            // setup buffer and notify server that the session key is coming
            byte[] sessionKeyBuffer = new byte[117];
            ByteArrayInputStream sessionKeyStream = new ByteArrayInputStream(encryptedSessionKey);
            toServer.writeInt(-1);
            toServer.writeInt(encryptedSessionKey.length);

            // write into a buffer of size 117
            while (sessionKeyStream.available() != 0) {
                sessionKeyStream.read(sessionKeyBuffer);
                if (sessionKeyBuffer.length == 117) {
                    toServer.write(sessionKeyBuffer);
                    sessionKeyBuffer = new byte[117];
                }
            }
            toServer.flush();

            System.out.println("Sent encrypted session key");

            // Open the file
            fileInputStream = new FileInputStream("sessionEncryptedFile.txt");
            bufferedFileInputStream = new BufferedInputStream(fileInputStream);

            toServer.writeInt(-1);

            byte[] buffer = new byte[117];
            while (bufferedFileInputStream.available() != 0) {
                System.out.println("Reading from stream: " + bufferedFileInputStream.read(buffer));
                if (buffer.length == 117) {
                    toServer.write(buffer);
                    buffer = new byte[117];
                }
            }
            toServer.flush();



//            int count = 0;
//            // Send the encrypted file
//            for (boolean fileEnded = false; !fileEnded;) {
//
//                // Read 117 bytes
//                numBytes = bufferedFileInputStream.read(fromFileBuffer);
//                count++;
//                fileEnded = numBytes < fromFileBuffer.length;
//                System.out.println("Write " + numBytes + " for " + count + " times" );
//
//                toServer.write(fromFileBuffer);
//                toServer.flush();
//            }

            // Receives end signal from server
            while (true){
                String end = in.readLine();
                if (end.equals("Ending transfer...")){
                    System.out.println("Server: " + end);
                    break;
                }
                else
                    System.out.println("End request failed...");
            }

            System.out.println("Closing connection...");
            bufferedFileInputStream.close();
            fileInputStream.close();

        } catch (Exception e) {e.printStackTrace();}

        long timeTaken = System.nanoTime() - timeStarted;
        System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run");
    }
}

