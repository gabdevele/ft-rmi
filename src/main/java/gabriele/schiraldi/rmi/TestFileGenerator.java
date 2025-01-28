package gabriele.schiraldi.rmi;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class TestFileGenerator {

    public static void generateTestFile(String fileName, int fileSizeMB) {
        System.out.println(fileSizeMB);
        long fileSize = (long) 1024 * 1024 * fileSizeMB;
        byte[] buffer = new byte[1024 * 1024 * 50];
        Random random = new Random();

        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            for (long i = 0; i < fileSize / buffer.length; i++) {
                random.nextBytes(buffer);
                fos.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}