package raytracer;

public interface ServiceDisp extends java.rmi.Remote, java.io.Serializable{
    public void setImage(Image i, int x0, int y0) throws java.rmi.RemoteException;
    public int getHauteur() throws java.rmi.RemoteException;
    public int getLargeur() throws java.rmi.RemoteException;
}
