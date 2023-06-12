package raytracer;

public interface ServiceScene extends java.rmi.Remote, java.io.Serializable{
    public Image compute(int x0, int y0, int w, int h) throws java.rmi.RemoteException;
}
