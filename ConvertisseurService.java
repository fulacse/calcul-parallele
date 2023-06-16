import raytracer.Scene;
import raytracer.ServiceScene;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ConvertisseurService implements FabricateurScene {

    private Scene scene;

    @Override
    public synchronized ServiceScene convertirService(Scene scene) throws RemoteException {
        this.scene = scene;
        System.out.println("Créer un service de calcul: "+this.scene.toString());
        return (ServiceScene) UnicastRemoteObject.exportObject(scene, 0);
    }

    @Override
    public synchronized void stop() throws RemoteException {
        if(this.scene==null)return;
        UnicastRemoteObject.unexportObject(this.scene, true);
        System.out.println("Arrêter un service de calcul: "+this.scene.toString());
        this.scene = null;
    }
}
