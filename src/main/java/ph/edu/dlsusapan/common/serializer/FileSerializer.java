package ph.edu.dlsusapan.common.serializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author XC23 - Chael Sumilang & Arron Baranquil @ 2024
 */
public class FileSerializer {

    // Method to convert file to byte array
    public static byte[] fileToBytes(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] bytesArray = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(bytesArray); // read file into bytes[]
        fis.close();
        return bytesArray;
    }

    // Method to convert byte array to file
    public static File bytesToFile(byte[] bytes, String filePath) throws IOException {
        File file = new File(filePath);

        if (!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bytes);
        fos.close();

        return file;
    }

}
