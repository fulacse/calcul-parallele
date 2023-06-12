public interface ServiceListFabriquateurScene extends java.rmi.Remote, java.io.Serializable{
    public void add(FabriquateurScene fabriquateurScene) throws java.rmi.RemoteException;
    public FabriquateurScene get(int i) throws java.rmi.RemoteException;
    public int size() throws java.rmi.RemoteException;
    public void remove(int i) throws java.rmi.RemoteException;
    public void remove(FabriquateurScene fabriquateurScene) throws java.rmi.RemoteException;
}
