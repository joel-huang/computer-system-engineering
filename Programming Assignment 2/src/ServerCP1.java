import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerCP1 {

    public static void main(String[] args) {

        ServerSocket welcomeSocket = null;
        Socket connectionSocket = null;
        DataOutputStream toClient = null;
        DataInputStream fromClient = null;

        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedFileOutputStream = null;

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
            while (true){
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

            // Starts file transfer
            System.out.println("AP completes. Receiving file...");

            // Get file size from client
            int fileSize = fromClient.readInt();
            System.out.println(fileSize);
            int size = 0;

            int count = 0;
            while (size < fileSize) {

                int packetType = fromClient.readInt();

                // If the packet is for transferring the filename
                if (packetType == 0) {

                    System.out.println("Receiving file...");

                    int numBytes = fromClient.readInt();
                    byte [] filename = new byte[numBytes];
                    fromClient.read(filename);

                    fileOutputStream = new FileOutputStream("recv/CP1_" + new String(filename, 0, numBytes));
                    bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);

                    // If the packet is for transferring a chunk of the file
                } else if (packetType == 1) {
                    count++;
                    int numBytes = fromClient.readInt();
                    int decryptedNumBytes = fromClient.readInt();
                    size+=decryptedNumBytes;

                    byte [] block = new byte[numBytes];
                    fromClient.read(block);

                    // Decrypt each 128 bytes
                    byte[] decryptedBlock = serverProtocol.decryptFile(block);

                    if (numBytes > 0){
                        bufferedFileOutputStream.write(decryptedBlock, 0, decryptedNumBytes);
                        bufferedFileOutputStream.flush();
                    }
                }
            }

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
