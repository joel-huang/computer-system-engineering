package CP1;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.security.cert.*;
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

}
