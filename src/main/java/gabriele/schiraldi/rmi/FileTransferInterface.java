package gabriele.schiraldi.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileTransferInterface extends Remote {
    void uploadFile(byte[] data, String fileName) throws RemoteException;
    void chunkUploadFile(byte[] data, String fileName, int chunkNumber) throws RemoteException;
    byte[] downloadFile(String fileName) throws RemoteException;
    String[] listFiles() throws RemoteException;
    long getFileSize(String fileName) throws RemoteException;
    byte[] downloadFileChunk(String fileName, int chunkNumber) throws RemoteException;
}