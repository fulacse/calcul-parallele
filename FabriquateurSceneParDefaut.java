import raytracer.Scene;
import raytracer.ServiceScene;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class FabriquateurSceneParDefaut implements FabriquateurScene{
    @Override
    public ServiceScene fabriquer(int largeur, int hauteur) throws RemoteException {
        System.out.println("fabriquer une scene par defaut");
        return (ServiceScene) UnicastRemoteObject.exportObject(new Scene("simple.txt",largeur, hauteur),0);
    }

    @Override
    public ServiceScene convertirService(Scene scene) throws RemoteException {
        return null;
    }
}
