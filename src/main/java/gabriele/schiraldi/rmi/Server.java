package gabriele.schiraldi.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static final int PORT = 1100;

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(PORT);
            registry.rebind("ft", new FileTransfer());
            LOGGER.info("Server ready");
        } catch (Exception e) {
            LOGGER.severe("Server error: " + e);
        }
    }

}