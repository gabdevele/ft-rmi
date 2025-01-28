package gabriele.schiraldi.rmi;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

public class FileTransfer extends UnicastRemoteObject implements FileTransferInterface {

    private static final Logger LOGGER = Logger.getLogger(FileTransfer.class.getName());
    private final String workingDir = System.getProperty("user.dir") + "/files";
    private static final int CHUNK_SIZE_MB = 50;
    private static final long CHUNK_SIZE_BYTES = CHUNK_SIZE_MB * 1024L * 1024L;

    protected FileTransfer() throws RemoteException {
        File dir = new File(workingDir);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                LOGGER.severe("Error creating directory");
            }
        }
    }

    private String sanitizeFileName(String fileName) {
        //all this mess to keep the file extension
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return fileName.replaceAll("[^a-zA-Z0-9]", "");
        }
        String name = fileName.substring(0, dotIndex).replaceAll("[^a-zA-Z0-9]", "");
        String esten = fileName.substring(dotIndex);
        return name + esten;
    }

    @Override
    public void uploadFile(byte[] data, String fileName) throws RemoteException {
        fileName = sanitizeFileName(fileName);
        try {
            File file = new File(workingDir + "/" + fileName);
            if (!file.createNewFile()) {
                fileName = fileName + System.currentTimeMillis();
                file = new File(workingDir + "/" + fileName);
            }
            Files.write(file.toPath(), data);
        } catch (IOException e) {
            LOGGER.severe("Error writing file: " + e);
        }
    }

    @Override
    public void chunkUploadFile(byte[] data, String fileName, int chunkNumber) throws RemoteException {
        fileName = sanitizeFileName(fileName);
        try {
            File file = new File(workingDir + "/" + fileName);
            if (chunkNumber == 0 && file.exists()) {
                file.delete();
            }
            Files.write(file.toPath(), data, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.severe("Error writing file chunk: " + e);
        }
    }


    @Override
    public byte[] downloadFile(String fileName) throws RemoteException {
        fileName = sanitizeFileName(fileName);
        try {
            File file = new File(workingDir + "/" + fileName);
            if (file.exists()) {
                return Files.readAllBytes(file.toPath());
            } else {
                LOGGER.severe(String.format("File not found: %s", fileName));
            }
        } catch (IOException e) {
            LOGGER.severe("Error reading file: " + e);
        }
        return null; //I wanted to avoid using null but exceptions didn't work
    }

    @Override
    public long getFileSize(String fileName) throws RemoteException {
        fileName = sanitizeFileName(fileName);
        File file = new File(workingDir + "/" + fileName);
        if (file.exists()) {
            return file.length();
        } else {
            LOGGER.severe("File not found: " + fileName);
            return -1;
        }
    }

    @Override
    public byte[] downloadFileChunk(String fileName, int chunkNumber) throws RemoteException {
        fileName = sanitizeFileName(fileName);
        File file = new File(workingDir + "/" + fileName);
        if (file.exists()) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                long offset = chunkNumber * CHUNK_SIZE_BYTES;
                if (offset >= file.length()) {
                    return null;
                }
                long remaining = file.length() - offset;
                int chunkSize = (int) Math.min(CHUNK_SIZE_BYTES, remaining);
                byte[] chunk = new byte[chunkSize];
                raf.seek(offset);
                raf.readFully(chunk);
                return chunk;
            } catch (IOException e) {
                LOGGER.severe("Error reading file chunk: " + e);
            }
        } else {
            LOGGER.severe("File not found: " + fileName);
        }
        return null;
    }

    @Override
    public String[] listFiles() throws RemoteException {
        File dir = new File(workingDir);
        String[] files = dir.list();
        if (files != null && files.length == 0) {
            return new String[]{"No files present"};
        }
        return files;
    }
}