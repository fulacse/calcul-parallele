import raytracer.Scene;
import raytracer.ServiceScene;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class FabriquateurSceneParDefaut implements FabriquateurScene{

private Scene scene;

    @Override
    public ServiceScene fabriquer(int largeur, int hauteur) throws RemoteException {
        System.out.println("fabriquer une scene par defaut");
        this.scene = new Scene("simple.txt",largeur, hauteur);
        return (ServiceScene) UnicastRemoteObject.exportObject(this.scene,0);
    }

    @Override
    public ServiceScene convertirService(Scene scene) throws RemoteException {
        return null;
    }

    @Override
    public void stop() throws RemoteException {
        UnicastRemoteObject.unexportObject(this.scene,true);
    }
}
