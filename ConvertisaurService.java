import raytracer.Scene;
import raytracer.ServiceScene;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ConvertisaurService implements FabriquateurScene{

    private Scene scene;

    @Override
    public ServiceScene fabriquer(int largeur, int hauteur) throws RemoteException {
        return null;
    }

    @Override
    public ServiceScene convertirService(Scene scene) throws RemoteException {
        this.scene = scene;
        return (ServiceScene) UnicastRemoteObject.exportObject(this.scene,0);
    }

    @Override
    public void stop() throws RemoteException {
        UnicastRemoteObject.unexportObject(this.scene,true);
    }
}
