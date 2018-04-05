import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.FileReader;

public class DesText {

    public static void main(String[] args) throws Exception {
        processFile("smallSize.txt");
        processFile("largeSize.txt");
    }

    public static void processFile(String fileName) throws Exception {

        StringBuilder data = new StringBuilder();
        String nextLine;
        BufferedReader reader = new BufferedReader(new FileReader("res/" + fileName));
        while((nextLine = reader.readLine()) != null) {
            data.append(nextLine).append("\n");
        }

        // Generate new symmetric key
        SecretKey secretKey = KeyGenerator.getInstance("DES").generateKey();

        // New cipher object, set it to encrypt using secret key
        Cipher encrypt = Cipher.getInstance("DES");
        encrypt.init(Cipher.ENCRYPT_MODE, secretKey);

        // Carry out the encryption
        byte[] encrypted = encrypt.doFinal(data.toString().getBytes());
        String base64encrypted = DatatypeConverter.printBase64Binary(encrypted);
        System.out.println("Ciphertext: " + base64encrypted);
        System.out.println("Length: " + base64encrypted.length());

        // New cipher object, set it to decrypt using secret key
        Cipher decrypt = Cipher.getInstance("DES");
        decrypt.init(Cipher.DECRYPT_MODE, secretKey);

        // Carry out the decryption
        byte[] decrypted = decrypt.doFinal(encrypted);
        //System.out.println("Decrypted: " + new String(decrypted));
    }
}
