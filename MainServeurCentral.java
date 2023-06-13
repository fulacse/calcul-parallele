import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MainServeurCentral {
    public static void main(String[] args) throws RemoteException {
        /*initialisation du service central*/
        Registry reg = LocateRegistry.createRegistry(1099);
        reg.rebind("CentreCalcule", UnicastRemoteObject.exportObject(new CentreCalcule(),0));
    }
}
