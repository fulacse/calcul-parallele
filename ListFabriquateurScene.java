import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ListFabriquateurScene implements ServiceListFabriquateurScene{

    private List<FabriquateurScene> fabriquateurScenes;

    public ListFabriquateurScene() {
        this.fabriquateurScenes = new ArrayList<>();
    }

    @Override
    public void add(FabriquateurScene fabriquateurScene) throws RemoteException {
        this.fabriquateurScenes.add(fabriquateurScene);
        System.out.println("Add: "+fabriquateurScene);
    }

    @Override
    public FabriquateurScene get(int i) throws RemoteException {
        return this.fabriquateurScenes.get(i);
    }

    @Override
    public int size() throws RemoteException {
        return this.fabriquateurScenes.size();
    }

    @Override
    public void remove(int i) throws RemoteException {
        this.fabriquateurScenes.remove(i);
    }

    @Override
    public void remove(FabriquateurScene fabriquateurScene) throws RemoteException {
        this.fabriquateurScenes.remove(fabriquateurScene);
    }
}
