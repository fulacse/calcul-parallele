import raytracer.Scene;
import raytracer.ServiceScene;

public interface FabriquateurScene extends java.rmi.Remote, java.io.Serializable{
    public ServiceScene fabriquer(int largeur, int hauteur) throws java.rmi.RemoteException;
    public ServiceScene convertirService(Scene scene) throws java.rmi.RemoteException;
}
