import raytracer.*;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.Thread.sleep;

public class CentreCalcule implements ServiceCentreCalcule{
    List<FabriquateurScene> fabriquateurScenes;
    int tailleParti=10;

    public CentreCalcule(){
        fabriquateurScenes = new ArrayList<>();
    }

    @Override
    public void addCalculeur(FabriquateurScene fabriquateurScene) throws RemoteException {
        fabriquateurScenes.add(fabriquateurScene);
        System.out.println("Ajouter un calculeur: "+fabriquateurScene.toString());
    }

    public void calculer(ServiceDisp disp, Scene scene) throws RemoteException, InterruptedException {
        List<ServiceScene> scenes = new ArrayList<>();
        Iterator<FabriquateurScene> fabriquateurSceneIterator = fabriquateurScenes.iterator();
        while (fabriquateurSceneIterator.hasNext()){
            FabriquateurScene fabriquateurScene = fabriquateurSceneIterator.next();
            try {
                scenes.add(fabriquateurScene.convertirService(scene));
            }catch (ConnectException connectException){
                fabriquateurSceneIterator.remove();
                System.out.println("Enlever un calculeur: "+ fabriquateurScene);
            }
        }

        if (scenes.size()==0){
            System.out.println("Pas de calculeur disponible");
            throw new ArithmeticException();
        }

        /*distribuer les taches de calcul*/
        int nbLigne=scene.getHeight()/tailleParti;
        int nbColone=scene.getWidth()/tailleParti;
        for (int i=0;i<nbColone;i++){
            for (int j=0;j<nbLigne;j++){
                ServiceScene serviceScene = scenes.get((i+j) % scenes.size());
                try {
                    Image image = serviceScene.compute(i*tailleParti, j*tailleParti, tailleParti, tailleParti);
                    disp.setImage(image, i*tailleParti, j*tailleParti);
                    sleep(10);
                }catch (ConnectException connectException) {
                    scenes.remove(serviceScene);
                    System.out.println("Enlever un service de calcul: " + scene);
                    j--;
                }
            }
        }

        /*si la taille de l'image n'est pas un multiple de la taille des parties, completer l'image*/
        if(scene.getHeight()>nbLigne*tailleParti){
            for (int i=0;i<nbColone;i++){
                try {
                    Image image = scenes.get(i % scenes.size()).compute(i*tailleParti, nbLigne*tailleParti, tailleParti, scene.getHeight()-nbLigne*tailleParti);
                    disp.setImage(image, i*tailleParti, nbLigne*tailleParti);
                }catch (ConnectException connectException) {
                    scenes.remove(scenes.get(i % scenes.size()));
                    System.out.println("Enlever un service de calcul: " + scene);
                    i--;
                }
            }
        }
        if(scene.getWidth()>nbColone*tailleParti){
            for (int j=0;j<nbLigne;j++){
                try {
                    Image image = scenes.get(j % fabriquateurScenes.size()).compute(nbColone*tailleParti, j*tailleParti, scene.getWidth()-nbColone*tailleParti, tailleParti);
                    disp.setImage(image, nbColone*tailleParti, j*tailleParti);
                }catch (ConnectException connectException) {
                    scenes.remove(scenes.get(j % scenes.size()));
                    System.out.println("Enlever un service de calcul: " + scene);
                    j--;
                }
            }
        }
        if(scene.getHeight()>nbLigne*tailleParti && scene.getWidth()>nbColone*tailleParti){
            boolean complete = false;
            while (!complete) {
                try {
                    Image image = scenes.get(0).compute(nbColone * tailleParti, nbLigne * tailleParti, scene.getWidth() - nbColone * tailleParti, scene.getHeight() - nbLigne * tailleParti);
                    disp.setImage(image, nbColone * tailleParti, nbLigne * tailleParti);
                    complete = true;
                } catch (ConnectException connectException) {
                    scenes.remove(scenes.get(0));
                    System.out.println("Enlever un service de calcul: " + scene);
                }
            }
        }

        /* Arreter les services de calcul*/
        fabriquateurSceneIterator = fabriquateurScenes.iterator();
        while (fabriquateurSceneIterator.hasNext()){
            FabriquateurScene fabriquateurScene = fabriquateurSceneIterator.next();
            try {
                fabriquateurScene.stop();
            }catch (ConnectException connectException){
                fabriquateurSceneIterator.remove();
                System.out.println("Enlever un calculeur: "+ fabriquateurScene);
            }
        }
    }

    /*private void drow(List<ServiceScene> scenes,int nbLigne,int nbCilone,ServiceDisp disp,int i,int j) throws InterruptedException, RemoteException {
        while (i<nbCilone){
            while (j<nbLigne){
                ServiceScene scene= scenes.get((i*nbLigne+j) % scenes.size());
                try {
                    Image image = scene.compute(i*tailleParti, j*tailleParti, tailleParti, tailleParti);
                    disp.setImage(image, i*tailleParti, j*tailleParti);
                    sleep(10);
                    j++;
                }catch (ConnectException connectException) {
                    scenes.remove(scene);
                    System.out.println("Enlever un service de calcul: "+scene);
                    drow(scenes, nbLigne, nbCilone, disp, i, j);
                }
            }
            j=0;
            i++;
        }
    }

    private void drowConpleterLigne(List<ServiceScene> scenes,int nbLigne,int nbColone,ServiceDisp disp) throws RemoteException {
        if(disp.getHauteur()>nbLigne*tailleParti){
            for (int i=0;i<nbColone;i++){
                Image image = scenes.get(i % scenes.size()).compute(i*tailleParti, nbLigne*tailleParti, tailleParti, disp.getHauteur()-nbLigne*tailleParti);
                disp.setImage(image, i*tailleParti, nbLigne*tailleParti);
            }
        }
    }*/
}
