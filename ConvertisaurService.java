import raytracer.Scene;
import raytracer.ServiceScene;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Semaphore;

public class ConvertisaurService implements FabriquateurScene{

    private Scene scene;
    private Semaphore semaphore = new Semaphore(1);

    @Override
    public ServiceScene fabriquer(int largeur, int hauteur) throws RemoteException {
        return null;
    }

    @Override
    public ServiceScene convertirService(Scene scene) throws RemoteException {
        try {
            semaphore.acquire();
            this.scene = scene;
            return (ServiceScene) UnicastRemoteObject.exportObject(this.scene, 0);
        } catch (InterruptedException e) {
            throw new RemoteException("Erreur lors de l'acquisition du verrou.", e);
        }
    }

    @Override
    public void stop() throws RemoteException {
        UnicastRemoteObject.unexportObject(this.scene, true);
        this.scene = null;
        semaphore.release();
    }
}
