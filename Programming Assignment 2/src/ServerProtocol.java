import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class ServerProtocol {
    private static byte[] nonce = new byte[32];
    private static byte[] encryptedNonce = new byte[128];
    private static byte[] certificate;
    private static InputStream server;
    private static CertificateFactory cf = null;
    private static KeyFactory kf = null;
    private static X509Certificate ServerCert;
    private static PublicKey serverKey;
    private static PrivateKey privateKey;
    private static Cipher cipher;
    private static Cipher fdcipher;

    private static String privatePath = "privateSe.der";

    public ServerProtocol(String server) throws IOException {
        this.server = new FileInputStream(server);

        try {
            cf = CertificateFactory.getInstance("X.509");

            // Get signed server certificate
            ServerCert =(X509Certificate)cf.generateCertificate(this.server);
            certificate = ServerCert.getEncoded();

            // Get server public key
            serverKey = ServerCert.getPublicKey();

            // Get server private key
            privateKey = getPrivateKey(privatePath);

        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        this.server.close();
    }

    // Get private key from file
    private PrivateKey getPrivateKey(String filePath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Path privateKeyPath = Paths.get(filePath);
        byte[] privateKeyBytes = Files.readAllBytes(privateKeyPath);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        kf = KeyFactory.getInstance("RSA");

        return kf.generatePrivate(keySpec);
    }

    // Encrypted nonce
    public void encryptNonce() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE,privateKey);
        encryptedNonce = cipher.doFinal(nonce);
    }

    public byte[] getNonce(){return nonce;}

    public byte[] getEncryptedNonce(){return encryptedNonce;}

    public byte[] getCertificate() {
        return certificate;
    }

    // CP-1 decryption using private key
    public byte[] decryptFile(byte[] fileByte) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        fdcipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        fdcipher.init(Cipher.DECRYPT_MODE,privateKey);
        return fdcipher.doFinal(fileByte);
    }
}
