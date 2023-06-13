import raytracer.Scene;
import raytracer.ServiceDisp;

import java.io.Serializable;
import java.rmi.Remote;

public interface ServiceCentreCalcule extends Remote, Serializable {
    public void addCalculeur(FabriquateurScene fabriquateurScene) throws java.rmi.RemoteException;
    public void calculer(ServiceDisp disp, Scene secne) throws java.rmi.RemoteException, InterruptedException;
}
