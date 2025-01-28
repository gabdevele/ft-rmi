package gabriele.schiraldi.rmi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;
import me.tongfei.progressbar.ProgressBar;

public class Client {

    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private static final int PORT = 1100;
    private static final int UPLOAD_CHUNK_SIZE_MB = 80;
    private static final int DOWNLOAD_CHUNK_SIZE_MB = 80;
    private FileTransferInterface obj;
    private final Scanner scanner;

    public Client() {
        try {
            Registry registry = LocateRegistry.getRegistry(PORT);
            obj = (FileTransferInterface) registry.lookup("ft");
        } catch (Exception e) {
            LOGGER.severe("Client error: " + e);
        } finally {
            scanner = new Scanner(System.in);
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public void run() {
        //please run the client from the terminal not from the IDE
        //otherwise it doesn't clear the console
        while (true) {
            System.out.print("\033\143");
            showMenu();
            int choice = scanner.nextInt();
            scanner.nextLine();
            System.out.print("\033\143");
            switch (choice) {
                case 1:
                    uploadFile();
                    break;
                case 2:
                    listFiles();
                    downloadFile();
                    break;
                case 3:
                    listFiles();
                    break;
                case 4:
                    generateTestFile();
                    break;
                case 5:
                    System.out.println("Goodbye...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option.");
            }
            scanner.nextLine();
        }
    }

    private void showMenu() {
        System.out.println("""
        \u001B[33m
        +================================================================================+
        |                                                                                |
        |   _____ _ _        _____                     __             ____  __  __ ___   |
        |  |  ___(_) | ___  |_   _| __ __ _ _ __  ___ / _| ___ _ __  |  _ \\|  \\/  |_ _|  |
        |  | |_  | | |/ _ \\   | || '__/ _` | '_ \\/ __| |_ / _ \\ '__| | |_) | |\\/| || |   |
        |  |  _| | | |  __/   | || | | (_| | | | \\__ \\  _|  __/ |    |  _ <| |  | || |   |
        |  |_|   |_|_|\\___|   |_||_|  \\__,_|_| |_|___/_|  \\___|_|    |_| \\_\\_|  |_|___|  |
        |                                                                                |
        |                             developed by @gabdevele                            |
        +================================================================================+\u001B[0m""");
        System.out.println("""
        \033[0;35m\tThis program allows file transfer via RMI in Java.\033[0m
        Select an option:
        1) Upload file
        2) Download file
        3) List files
        4) Generate test file
        5) Exit""");
        System.out.print("> ");
    }

    private void uploadFile() {
        try {
            System.out.println("Enter the (relative/absolute) path of the file you want to upload.");
            System.out.println("Up to " + UPLOAD_CHUNK_SIZE_MB + " MB will be uploaded entirely, otherwise it will be uploaded in chunks.");
            System.out.print("> ");
            String uploadPath = scanner.nextLine();
            File uploadFile = new File(uploadPath);
            //I don't always chunk because depending on the size of the file or the performance
            //of the PC it could be slower than reading all at once
            if (uploadFile.exists()) {
                if (uploadFile.length() > UPLOAD_CHUNK_SIZE_MB * 1024L * 1024L) {
                    uploadFileInChunks(uploadFile);
                } else {
                    uploadEntireFile(uploadFile);
                }
                System.out.println("File uploaded successfully.");
            } else {
                System.out.println("File not found.");
            }
        } catch (Exception e) {
            LOGGER.severe("File upload error: " + e);
        }
    }

    private void uploadFileInChunks(File uploadFile) throws IOException {
        //memory management needs to be improved, the garbage collector doesn't help
        try (ProgressBar pb = new ProgressBar("Uploading file", uploadFile.length())) {
            byte[] uploadData = new byte[UPLOAD_CHUNK_SIZE_MB * 1024 * 1024];
            int bytesRead;
            int chunkNumber = 0;
            try (InputStream inputStream = Files.newInputStream(uploadFile.toPath())) {
                while ((bytesRead = inputStream.read(uploadData)) != -1) {
                    obj.chunkUploadFile(Arrays.copyOf(uploadData, bytesRead), uploadFile.getName(), chunkNumber++);
                    pb.stepBy(bytesRead);
                }
            }
        }
    }

    private void uploadEntireFile(File uploadFile) throws IOException {
        try (ProgressBar pb = new ProgressBar("Uploading file", 2)) {
            pb.setExtraMessage("Reading file.");
            byte[] uploadData = Files.readAllBytes(uploadFile.toPath());
            pb.step();
            pb.setExtraMessage("Uploading file.");
            obj.uploadFile(uploadData, uploadFile.getName());
            pb.step();
        }
    }

    private void downloadFile() {
        try {
            System.out.println("Enter the name of the file you want to download.");
            System.out.println("Up to " + DOWNLOAD_CHUNK_SIZE_MB + " MB will be downloaded entirely, otherwise it will be downloaded in chunks.");
            System.out.print("> ");
            String downloadFileName = scanner.nextLine();
            File downloadFile = new File(downloadFileName);
            long fileSize = obj.getFileSize(downloadFileName);
            if(fileSize == -1) {
                System.out.println("File not found.");
                return;
            }
            try (ProgressBar pb = new ProgressBar("Downloading file", fileSize)) {
                if (fileSize > DOWNLOAD_CHUNK_SIZE_MB * 1024L * 1024L) {
                    downloadFileInChunks(downloadFileName, downloadFile, pb);
                } else {
                    downloadEntireFile(downloadFileName, downloadFile, pb);
                }
            }
            System.out.println("File downloaded successfully.");
        } catch (Exception e) {
            LOGGER.severe("File download error: " + e);
        }
    }

    private void downloadFileInChunks(String downloadFileName, File downloadFile, ProgressBar pb) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(downloadFile)) {
            int chunkNumber = 0;
            byte[] chunk;
            while ((chunk = obj.downloadFileChunk(downloadFileName, chunkNumber++)) != null) {
                fos.write(chunk);
                pb.stepBy(chunk.length);
            }
        }
    }

    private void downloadEntireFile(String downloadFileName, File downloadFile, ProgressBar pb) throws IOException {
        byte[] downloadData = obj.downloadFile(downloadFileName);
        if (downloadData != null) {
            Files.write(downloadFile.toPath(), downloadData);
            pb.stepBy(downloadData.length);
        } else {
            System.out.println("Error downloading file.");
        }
    }

    private void listFiles() {
        try {
            String[] files = obj.listFiles();
            System.out.println("Available files: ");
            for (String file : files) {
                System.out.print("- " + file);
                long fileSize = obj.getFileSize(file);
                if(fileSize != -1) {
                    System.out.println(" (" + fileSize + " bytes)");
                } else {
                    System.out.println();
                }
            }
        } catch (Exception e) {
            LOGGER.severe("File list error: " + e);
        }
    }

    private void generateTestFile() {
        try {
            System.out.print("Enter the name of the test file: ");
            String fileName = scanner.nextLine();
            System.out.print("Enter the size of the test file (in MB): ");
            int fileSizeMB = scanner.nextInt();
            scanner.nextLine();
            TestFileGenerator.generateTestFile(fileName, fileSizeMB);
            System.out.println("Test file generated successfully.");
        } catch (Exception e) {
            LOGGER.severe("Test file generation error: " + e);
        }
    }
}