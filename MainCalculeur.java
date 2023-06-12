import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MainCalculeur {
    public static void main(String[] args) throws RemoteException, NotBoundException {
        /* adresse du service distant et port depuis la ligne de commande */
        String serveur="localhost";    // par défaut le serveur est sur la même machine
        int port=1099;                      // le port de la rmiregistry par défaut

        if(args.length > 0)
            serveur=args[0];
        if(args.length > 1)
            port=Integer.parseInt(args[1]);

        /* récupération du service FabriquateurScene */
        Registry reg = LocateRegistry.getRegistry(serveur, port);
        ServiceListFabriquateurScene fabriquateurScenes=(ServiceListFabriquateurScene) reg.lookup("FabriquateurScenes");

        /*ajouter un fabriquateur de scene par defaut*/
        fabriquateurScenes.add((FabriquateurScene) UnicastRemoteObject.exportObject(new ConvertisaurService(),0));

    }
}
