package CP1;

import java.io.*;
import java.net.Socket;

public class ClientCP1 {
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
            }else{
                System.out.println("Server verification faile");
                System.out.println("Closing all connections...");
                toServer.close();
                fromServer.close();
                clientSocket.close();
            }

            System.out.println("AP completes. Sending file...");

            // Open the file
            fileInputStream = new FileInputStream(filename);
            bufferedFileInputStream = new BufferedInputStream(fileInputStream);

            // Send file size
            int fileSize = fileInputStream.available();
            toServer.writeInt(fileSize);
            toServer.flush();

            // Send the filename
            toServer.writeInt(0);
            toServer.writeInt(filename.getBytes().length);
            toServer.write(filename.getBytes());
            toServer.flush();

            byte [] fromFileBuffer = new byte[117];

            int count = 0;
            // Send the encrypted file
            for (boolean fileEnded = false; !fileEnded;) {

                // Read 117 bytes
                numBytes = bufferedFileInputStream.read(fromFileBuffer);

                // Encrypt 117 bytes
                byte[] encryptedfromFileBuffer = clientProtocol.encryptFile(fromFileBuffer);
                count++;
                fileEnded = numBytes < fromFileBuffer.length;
                System.out.println("Write " + numBytes + " for " + count + " times" );
                int encryptedNumBytes = encryptedfromFileBuffer.length;

                toServer.writeInt(1);
                toServer.writeInt(encryptedNumBytes);
                toServer.writeInt(numBytes);
                toServer.write(encryptedfromFileBuffer);
                toServer.flush();
            }

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

