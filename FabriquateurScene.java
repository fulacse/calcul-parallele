import raytracer.Scene;
import raytracer.ServiceScene;

public interface FabriquateurScene extends java.rmi.Remote, java.io.Serializable{
    public ServiceScene convertirService(Scene scene) throws java.rmi.RemoteException;
    public void stop() throws java.rmi.RemoteException;
}
