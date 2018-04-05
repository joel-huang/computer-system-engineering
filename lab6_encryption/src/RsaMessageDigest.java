import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;

public class RsaMessageDigest {

    public static void main(String[] args) throws Exception {
        digest("smallSize.txt");
        digest("largeSize.txt");
    }

    public static void digest(String fileName) throws Exception {

        // read the file
        StringBuilder data = new StringBuilder();
        String newLine;
        BufferedReader reader = new BufferedReader(new FileReader("res/" + fileName));
        while ((newLine = reader.readLine()) != null) {
            data.append(newLine).append("\n");
        }

        // create the messagedigest
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(data.toString().getBytes());

        System.out.println("Digesting " + fileName + "...");
        System.out.println("The digest is: " + DatatypeConverter.printBase64Binary(digest));
        System.out.println("Digest length: " + digest.length);

        // generate RSA private key
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keys = keyPairGenerator.generateKeyPair();
        Key privateKey = keys.getPrivate();
        Key publicKey = keys.getPublic();

        // encrypt the digest
        System.out.println("Encrypting with private key...");
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] encryptedMessageDigest = cipher.doFinal(digest);

        System.out.println("Encrypted digest as: " + DatatypeConverter.printBase64Binary(encryptedMessageDigest));
        System.out.println("Signed digest length: " + encryptedMessageDigest.length);

        // decrypt the digest with the public key
        System.out.println("Decrypting with public key...");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] decryptedMessageDigest = cipher.doFinal(encryptedMessageDigest);

        System.out.println("Decrypted digest as: " + DatatypeConverter.printBase64Binary(decryptedMessageDigest));
    }
}
