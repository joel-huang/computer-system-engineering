import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
        BufferedReader inputReader = null;
        PrintWriter out = null;

        try {
            welcomeSocket = new ServerSocket(4321);

            // Prints IP
            System.out.println("Server IP: " + welcomeSocket.getInetAddress().getLocalHost().getHostAddress());

            connectionSocket = welcomeSocket.accept();

            fromClient = new DataInputStream(connectionSocket.getInputStream());
            toClient = new DataOutputStream(connectionSocket.getOutputStream());

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

            // Hold some variables here.
            byte[] encryptedSessionKey;
            String filename = "";
            Cipher sessionCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            // wait for client to send over the signal to accept the encrypted key
            while (!connectionSocket.isClosed()) {

                int command = fromClient.readInt();
                BufferedInputStream inputStream = new BufferedInputStream(connectionSocket.getInputStream());

                if (command == 0) {
                    // Get the encrypted session key and decrypt using private key
                    int encryptedSessionKeySize = fromClient.readInt();
                    encryptedSessionKey = new byte[encryptedSessionKeySize];
                    fromClient.readFully(encryptedSessionKey);

                    System.out.println("Received encrypted session key of size: " + encryptedSessionKey.length);
                    System.out.println("Encrypted session key: " + DatatypeConverter.printBase64Binary(encryptedSessionKey));
                    System.out.println("Decrypting session key...");
                    byte[] sessionKeyBytes = serverProtocol.decryptFile(encryptedSessionKey);
                    SecretKey sessionKey = new SecretKeySpec(sessionKeyBytes, 0, sessionKeyBytes.length, "AES");
                    sessionCipher.init(Cipher.DECRYPT_MODE, sessionKey);
                }
                else if (command == 1) {
                    // set the filename
                    int nameLength = fromClient.readInt();
                    byte[] nameBytes = new byte[nameLength];
                    fromClient.readFully(nameBytes);
                    filename = new String(nameBytes);

                } else if (command == 2) {
                    // Starts file transfer
                    System.out.println("Attempting to receive file...");
                    System.out.println("Getting file size...");

                    int encryptedFileSize = fromClient.readInt();
                    System.out.println("File size: " + encryptedFileSize);

                    byte[] encryptedFileBytes = new byte[encryptedFileSize];
                    fromClient.readFully(encryptedFileBytes, 0, encryptedFileSize);
                    System.out.println(Arrays.toString(encryptedFileBytes));
                    System.out.println(encryptedFileBytes.length);

                    System.out.println("Decrypting file with session key...");

                    byte[] result = sessionCipher.doFinal(encryptedFileBytes);
                    //byte[] result = serverProtocol.decryptBytesWithSessionKey(sessionCipher, encryptedFileBytes);

                    FileOutputStream file = new FileOutputStream("recv/CP2_" + filename); //creating output file
                    file.write(result);
                    file.close();

                    // Indicate end of transfer to client
                    System.out.println("Transfer finished");
                    out.println("Ending transfer...");

                    // Close connection
                    System.out.println("Closing connection...");
                    fromClient.close();
                    toClient.close();
                    connectionSocket.close();
                }
            }
        } catch (Exception e) {e.printStackTrace();}

    }
}
