package CP2;

import javax.crypto.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class ClientProtocol {
    private static InputStream CA;
    private static CertificateFactory cf = null;
    private static X509Certificate CAcert;
    private static X509Certificate ServerCert;
    private static PublicKey CAkey;
    private static PublicKey serverKey;

    private static byte[] nonce = new byte[32];
    private static byte[] encryptedNonce = new byte[128];

    private static Cipher dcipher;
    private static Cipher fcipher;

    public ClientProtocol(String CA) throws IOException {
        this.CA = new FileInputStream(CA);

        try {
            cf = CertificateFactory.getInstance("X.509");

            // Get public key from CA certificate
            CAcert =(X509Certificate)cf.generateCertificate(this.CA);
            CAkey = CAcert.getPublicKey();

        } catch (CertificateException e) {
            e.printStackTrace();
        }

        this.CA.close();
    }

    public void getCertificate(InputStream certificate) throws CertificateException {
        // Get signed server certificate
        ServerCert =(X509Certificate)cf.generateCertificate(certificate);
    }

    public void getPublicKey() {
        // Get server public key from certificate
        serverKey = ServerCert.getPublicKey();
    }

    // Verify signed certificate using CA's public key
    public void verifyCert(){
        try {
            ServerCert.checkValidity();
            ServerCert.verify(CAkey);

        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
    }

    // Generate nonce
    public void generateNonce(){
        SecureRandom random = new SecureRandom();
        random.nextBytes(nonce);
    }

    // Decrypt encrypted nonce with public key
    public byte[] decryptNonce(byte[] encryptedNonce) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        dcipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        dcipher.init(Cipher.DECRYPT_MODE,serverKey);
        return dcipher.doFinal(encryptedNonce);
    }

    // Checks that decrypted nonce equals to original nonce
    public boolean validateNonce(byte[] decryptedNonce){
        return Arrays.equals(nonce,decryptedNonce);
    }

    public byte[] getEncryptedNonce(){
        return encryptedNonce;
    }

    public byte[] getNonce(){return nonce;}

    // CP-1 encryption using public key
    public byte[] encryptFile(byte[] fileByte) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        fcipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        fcipher.init(Cipher.ENCRYPT_MODE,serverKey);
        return fcipher.doFinal(fileByte);
    }

    // atomic method to encrypt file with session key, then encrypt
    // session key with RSA public key.
    // postcondition: AES session key encrypted with RSA public key and file
    // encrypted with session key
    public byte[] encryptSessionKeyAndFile(byte[] fileByte) throws Exception {
        SecretKey sessionKey = KeyGenerator.getInstance("AES").generateKey();
        // encrypt file with session key
        Cipher sessionCipher = Cipher.getInstance("AES");
        sessionCipher.init(Cipher.ENCRYPT_MODE, sessionKey);
        byte[] sessionEncryptedFile = sessionCipher.doFinal(fileByte);
        FileOutputStream fos = new FileOutputStream("sessionEncryptedFile.txt");
        fos.write(sessionEncryptedFile);
        // encrypt session key with RSA public key
        return encryptFile(sessionKey.getEncoded());
    }

}
