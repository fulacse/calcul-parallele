import raytracer.Scene;
import raytracer.ServiceScene;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ConvertisaurService implements FabriquateurScene{
    @Override
    public ServiceScene fabriquer(int largeur, int hauteur) throws RemoteException {
        return null;
    }

    @Override
    public ServiceScene convertirService(Scene scene) throws RemoteException {
        return (ServiceScene) UnicastRemoteObject.exportObject(scene,0);
    }
}
