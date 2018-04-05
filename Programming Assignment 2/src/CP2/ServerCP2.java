package CP2;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ServerCP2 {
    public static void main(String[] args) throws IOException {

        ServerSocket welcomeSocket = null;
        Socket connectionSocket = null;
        DataOutputStream toClient = null;
        DataInputStream fromClient = null;

        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedFileOutputStream = null;

        BufferedReader stdIn = null;
        BufferedReader inputReader = null;

        PrintWriter out = null;

        try {
            welcomeSocket = new ServerSocket(4321);
            connectionSocket = welcomeSocket.accept();

            fromClient = new DataInputStream(connectionSocket.getInputStream());
            toClient = new DataOutputStream(connectionSocket.getOutputStream());

            stdIn = new BufferedReader(new InputStreamReader(System.in));
            inputReader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            out = new PrintWriter(connectionSocket.getOutputStream(), true);

            while (true){
                String request = inputReader.readLine();
                if (request.equals("Requesting authentication...")){
                    System.out.println("Client: " + request);
                    break;
                }
                else
                    System.out.println("Request failed...");
            }

            // Set up protocol
            ServerProtocol serverProtocol = new ServerProtocol("server.crt");

            // Get nonce from client
            System.out.println("Getting nonce from client...");
            fromClient.read(serverProtocol.getNonce());
            System.out.println("Nonce received");

            // Encrypt nonce
            System.out.println("Encrypting nonce...");
            serverProtocol.encryptNonce();

            // Send nonce to client
            System.out.println("Sending encrypted nonce to client...");
            toClient.write(serverProtocol.getEncryptedNonce());
            toClient.flush();

            // Receive certificate request from client
            while (true) {
                String request = inputReader.readLine();
                if (request.equals("Request certificate...")){
                    System.out.println("Client: " + request);

                    // Send certificate to client
                    System.out.println("Sending certificate to client...");
                    toClient.write(serverProtocol.getCertificate());
                    toClient.flush();
                    break;
                }
                else
                    System.out.println("Request failed...");
            }

            // Waiting for client to finish verification
            System.out.println("Client: " + inputReader.readLine());

            // wait for client to send over the signal to accept the encrypted key
            while (fromClient.readInt() != -1);

            // Get the encrypted session key and decrypt using private key
            int encryptedSessionKeySize = fromClient.readInt();
            byte[] encryptedSessionKey = new byte[encryptedSessionKeySize];
            int size = 0;
            while (size < encryptedSessionKeySize) {
                encryptedSessionKey[size] = (byte)fromClient.read();
                size++;
            }


            System.out.println("Received encrypted session key of size: " + encryptedSessionKey.length);
            System.out.println("Encrypted session key: " + DatatypeConverter.printBase64Binary(encryptedSessionKey));

            // Starts file transfer
            System.out.println("Attempting to receive file...");



            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[117];
            while (buffer.size() < 39546) {
                fromClient.read(data);
                System.out.println("Writing to buffer: " + Arrays.toString(data));
                buffer.write(data);
                data = new byte[117];
                System.out.println(buffer.size());
            }

            byte[] encryptedFileBytes = buffer.toByteArray();
            System.out.println(Arrays.toString(encryptedFileBytes));
            System.out.println(encryptedFileBytes.length);

            System.out.println("Decrypting file with session key...");

            byte[] result = serverProtocol.decryptBytesWithSessionKey(encryptedFileBytes, encryptedSessionKey);
            System.out.println(Arrays.toString(result));

            // Indicate end of transfer to client
            System.out.println("Transfer finished");
            out.println("Ending transfer...");

            // Close connection
            System.out.println("Closing connection...");
            bufferedFileOutputStream.close();
            fileOutputStream.close();

            fromClient.close();
            toClient.close();
            connectionSocket.close();

        } catch (Exception e) {e.printStackTrace();}

    }
}
