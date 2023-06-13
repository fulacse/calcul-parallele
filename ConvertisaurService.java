import raytracer.Scene;
import raytracer.ServiceScene;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Semaphore;

public class ConvertisaurService implements FabriquateurScene{
    @Override
    public ServiceScene convertirService(Scene scene) throws RemoteException {
        System.out.println("Créer un service de calcul: ");
        return (ServiceScene) UnicastRemoteObject.exportObject(scene, 0);
    }
}
