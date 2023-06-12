import raytracer.Disp;
import raytracer.Image;
import raytracer.Scene;
import raytracer.ServiceScene;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
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
        ServiceListFabriquateurScene fabriquateurScenes=(ServiceListFabriquateurScene) reg.lookup("FabriquateurScenes");
        Scene scene=new Scene("simple.txt",largeur,hauteur);
        List<ServiceScene> scenes=new ArrayList<>();
        for (int i=0;i< fabriquateurScenes.size();i++){
            scenes.add(fabriquateurScenes.get(i).convertirService(scene));
        }

        /*distribuer les taches de calcul*/
        int tailleParti=10;
        int nbLigne=hauteur/tailleParti;
        int nbCilone=largeur/tailleParti;
        Disp disp = new Disp("Raytracer", largeur, hauteur);
        for (int i=0;i<nbCilone;i++){
            for (int j=0;j<nbLigne;j++){
                Image image = scenes.get((i*nbLigne+j) % scenes.size()).compute(i*tailleParti, j*tailleParti, tailleParti, tailleParti);
                disp.setImage(image, i*tailleParti, j*tailleParti);
                sleep(10);
            }
        }

        /*si la taille de l'image n'est pas un multiple de la taille des parties, completer l'image*/
        if(hauteur>nbLigne*tailleParti){
            for (int i=0;i<nbCilone;i++){
                Image image = scenes.get(i % scenes.size()).compute(i*tailleParti, nbLigne*tailleParti, tailleParti, hauteur-nbLigne*tailleParti);
                disp.setImage(image, i*tailleParti, nbLigne*tailleParti);
            }
        }
        if(largeur>nbCilone*tailleParti){
            for (int j=0;j<nbLigne;j++){
                Image image = scenes.get(j % fabriquateurScenes.size()).compute(nbCilone*tailleParti, j*tailleParti, largeur-nbCilone*tailleParti, tailleParti);
                disp.setImage(image, nbCilone*tailleParti, j*tailleParti);
            }
        }
        if(hauteur>nbLigne*tailleParti && largeur>nbCilone*tailleParti){
            Image image = scenes.get(0).compute(nbCilone*tailleParti, nbLigne*tailleParti, largeur-nbCilone*tailleParti, hauteur-nbLigne*tailleParti);
            disp.setImage(image, nbCilone*tailleParti, nbLigne*tailleParti);
        }

        /* arrêter les services */
        //System.out.println(fabriquateurScenes.size());
        for (int i=0;i< fabriquateurScenes.size();i++){
            fabriquateurScenes.get(i).stop();
        }
    }
}
