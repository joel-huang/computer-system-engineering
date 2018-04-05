import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

public class DesImage {

    private static final String file1 = "SUTD.bmp";
    private static final String file2 = "triangle.bmp";

    public static void main(String[] args) throws Exception {
        System.out.println("Doing ECB...");
        processImage(file1, "ECB", "regular");
        processImage(file2, "ECB", "regular");
        System.out.println("\nDoing CBC...");
        processImage(file1, "CBC", "regular");
        processImage(file2, "CBC", "regular");
        System.out.println("Doing ECB reversed...");
        processImage(file1, "ECB", "reversed");
        processImage(file2, "ECB", "reversed");
        System.out.println("\nDoing CBC reversed...");
        processImage(file1, "CBC", "reversed");
        processImage(file2, "CBC", "reversed");
    }

    private static void processImage(String fileName, String mode, String option) throws Exception {
        // initialize an int[][] with the RGB values of the bitmap
        BufferedImage bufferedImage = ImageIO.read(new File("res/" + fileName));
        int imageHeight = bufferedImage.getHeight();
        int imageWidth = bufferedImage.getWidth();
        // buffer for the encrypted image in RGB
        BufferedImage encryptedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);

        int[][] imageArray = new int[imageWidth][imageHeight];

        for (int i = 0; i < imageWidth; i++) {
            for (int j = 0; j < imageHeight; j++) {
                imageArray[i][j] = bufferedImage.getRGB(i, j);
            }
        }

        // create cipher, which operates on byte[]
        Cipher encrypt = Cipher.getInstance("DES/" + mode + "/PKCS5Padding");
        SecretKey secretKey = KeyGenerator.getInstance("DES").generateKey();
        encrypt.init(Cipher.ENCRYPT_MODE, secretKey);

        // encrypt each column of int[][] using cipher
        // imageHeight is the number of ints (32bit/4byte blocks) in a column

        // for every column
        for (int i = 0; i < imageWidth; i++) {
            byte[] column = new byte[4*imageHeight];
            // for every row
            if (option.equalsIgnoreCase("reversed")) {
                for (int j = imageHeight - 1; j > 0; j--) {
                    // make new byte buffer with size 4 bytes
                    ByteBuffer buffer = ByteBuffer.allocate(4);
                    // put the next int vertically below into the buffer
                    buffer.putInt(imageArray[i][imageHeight - j]);
                    // convert the buffer into a byte array
                    byte[] bytes = buffer.array();
                    // concat the byte array to the column
                    System.arraycopy(bytes, 0, column, 4 * j, 4);
                }
            } else {
                for (int j = 0; j < imageHeight; j++) {
                    // make new byte buffer with size 4 bytes
                    ByteBuffer buffer = ByteBuffer.allocate(4);
                    // put the next int vertically below into the buffer
                    buffer.putInt(imageArray[i][j]);
                    // convert the buffer into a byte array
                    byte[] bytes = buffer.array();
                    // concat the byte array to the column
                    System.arraycopy(bytes, 0, column, 4 * j, 4);
                }
            }

            // encrypt the entire column
            byte[] encryptedBytes = encrypt.doFinal(column);

            // write a new encrypted pixel for every pixel in the column
            // make a new byte buffer to store the next encrypted pixel
            byte[] pixel = new byte[4];
            for (int j = 0; j < imageHeight; j++) {
                // copy the encrypted byte at position 4*j to the pixel buffer
                System.arraycopy(encryptedBytes, 4*j, pixel, 0, 4);
                // make new byte buffer with size 4 bytes
                ByteBuffer buffer = ByteBuffer.wrap(pixel);
                // get the int from the buffer
                int newRGB = buffer.getInt();
                encryptedImage.setRGB(i, j, newRGB);
            }
        }

        String newFileName;

        if ("reversed".equalsIgnoreCase(option)) {
            newFileName = fileName + "_" + mode + "_encrypted_reversed.bmp";
        } else {
            newFileName = fileName + "_" + mode + "_encrypted.bmp";
        }
        ImageIO.write(encryptedImage, "BMP",new File("output/" + newFileName));
        System.out.println(mode + " mode on " + fileName + " Complete. File saved as " + newFileName);
    }
}
