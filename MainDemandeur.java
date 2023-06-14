import raytracer.*;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import static java.lang.Thread.sleep;

public class MainDemandeur {
    public static void main(String[] args) throws RemoteException, NotBoundException, InterruptedException {
        /* hauteur et largeur par défaut de l'image à construire */
        int hauteur=200;
        int largeur=200;

        /* adresse du service distant et port depuis la ligne de commande */
        String serveur="localhost";    // par défaut le serveur est sur la même machine
        int port=1099;                      // le port de la rmiregistry par défaut

        if(args.length > 0)
            hauteur=Integer.parseInt(args[0]);
        if(args.length > 1)
            largeur=Integer.parseInt(args[1]);
        if(args.length > 2)
            serveur=args[2];
        if(args.length > 3)
            port=Integer.parseInt(args[3]);

        /* récupération du service FabriquateurScene */
        Registry reg = LocateRegistry.getRegistry(serveur, port);
        ServiceCentreCalcule centreCalcule=(ServiceCentreCalcule) reg.lookup("CentreCalcule");
        Scene scene=new Scene("simple.txt",largeur,hauteur);
        Disp disp=new Disp("Raytracer", largeur, hauteur);
        try {
            Instant debut = Instant.now();
            centreCalcule.calculer((ServiceDisp) UnicastRemoteObject.exportObject(disp,0), scene);
            Instant fin = Instant.now();

            long duree = Duration.between(debut,fin).toMillis();
            System.out.println(duree);

        }catch (ArithmeticException arithmeticException){
            UnicastRemoteObject.unexportObject(disp,true);
            disp.close();
            System.out.println("Pas de calculeur disponible");
        }
    }
}
