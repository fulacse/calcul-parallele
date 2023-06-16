import raytracer.*;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.Thread.sleep;

public class CentreCalcule implements ServiceCentreCalcule {
    List<FabricateurScene> fabricateurScenes;
    List<ServiceScene> scenes;
    int tailleParti = 10;

    public CentreCalcule() {
        scenes = new ArrayList<>();
        fabricateurScenes = new ArrayList<>();
    }

    @Override
    public synchronized void addCalculeur(FabricateurScene fabricateurScene) throws RemoteException {
        fabricateurScenes.add(fabricateurScene);
        System.out.println("Ajouter un calculeur: " + fabricateurScene.toString());
    }

    public synchronized void calculer(ServiceDisp disp, Scene scene) throws RemoteException, InterruptedException {

        /* Arreter les services de calcul*/
        Iterator<FabricateurScene> fabriquateurSceneIterator = fabricateurScenes.iterator();
        List<Thread> threads = new ArrayList<>();
        while (fabriquateurSceneIterator.hasNext()) {
            FabricateurScene fabricateurScene = fabriquateurSceneIterator.next();
            Thread thread = new Thread(() -> {
                try {
                    if (fabricateurScene !=null) fabricateurScene.stop();
                } catch (ConnectException connectException) {
                    fabricateurScenes.remove(fabricateurScene);
                    System.out.println("Enlever un calculeur: " + fabricateurScene);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            threads.add(thread);
        }
        for (Thread thread : threads) {
            thread.join();
        }

        /* Convertir les services de calcul*/
        scenes = new ArrayList<>();
        fabriquateurSceneIterator = fabricateurScenes.iterator();
        threads = new ArrayList<>();
        while (fabriquateurSceneIterator.hasNext()) {
            FabricateurScene fabricateurScene = fabriquateurSceneIterator.next();
            Thread thread = new Thread(() -> {
                try {
                    if (fabricateurScene != null)scenes.add(fabricateurScene.convertirService(scene));
                } catch (ConnectException connectException) {
                    fabricateurScenes.remove(fabricateurScene);
                    System.out.println("Enlever un calculeur: " + fabricateurScene);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            threads.add(thread);
        }
        for (Thread thread : threads) {
            thread.join();
        }

        if (scenes.size() == 0) {
            System.out.println("Pas de calculeur disponible");
            throw new ArithmeticException();
        }

        /*distribuer les taches de calcul*/
        int nbLigne = scene.getHeight() / tailleParti;
        int nbColone = scene.getWidth() / tailleParti;
        List<int[]> tachesOmettre = new ArrayList<>();
        threads = new ArrayList<>();
        for (int i = 0; i < nbColone; i++) {
            for (int j = 0; j < nbLigne; j++) {
                ServiceScene serviceScene = scenes.get((i + j) % scenes.size());
                int finalI = i;
                int finalJ = j;
                Thread thread = toThread(serviceScene, finalI, finalJ,tailleParti,tailleParti, disp, tachesOmettre);
                thread.start();
                threads.add(thread);
            }
        }
        for (Thread thread : threads) {
            thread.join();
        }
        while (tachesOmettre.size()>0) {
            threads = new ArrayList<>();
            while (tachesOmettre.size() > 0) {
                int[] tache = tachesOmettre.get(0);
                tachesOmettre.remove(0);
                ServiceScene serviceScene = scenes.get((tache[0] + tache[1]) % scenes.size());
                int finalI = tache[0];
                int finalJ = tache[1];
                Thread thread = toThread(serviceScene, finalI, finalJ,tailleParti,tailleParti, disp, tachesOmettre);
                thread.start();
                threads.add(thread);
            }
            for (Thread thread : threads) {
                thread.join();
            }
        }

        /*si la taille de l'image n'est pas un multiple de la taille des parties, completer l'image*/
        threads = new ArrayList<>();
        if(scene.getHeight()>nbLigne*tailleParti){
            for (int i=0;i<nbColone;i++){
                ServiceScene sceneI = scenes.get(i % scenes.size());
                int finalI = i;
                List<int[]> finalTachesOmettre = tachesOmettre;
                Thread thread=toThread(sceneI, finalI, nbLigne,tailleParti,scene.getHeight()%tailleParti, disp, finalTachesOmettre);
                thread.start();
                threads.add(thread);
            }
        }
        for (Thread thread : threads) {
            thread.join();
        }
        while (tachesOmettre.size()>0) {
            threads = new ArrayList<>();
            while (tachesOmettre.size() > 0) {
                int[] tache = tachesOmettre.get(0);
                tachesOmettre.remove(0);
                ServiceScene sceneI = scenes.get(tache[0] % scenes.size());
                Thread thread=toThread(sceneI, tache[0], tache[1],tailleParti,scene.getHeight()%tailleParti, disp, tachesOmettre);
                thread.start();
                threads.add(thread);
            }
            for (Thread thread : threads) {
                thread.join();
            }
        }
        threads = new ArrayList<>();
        if(scene.getWidth()>nbColone*tailleParti){
            for (int j = 0; j <nbLigne; j++){
                int finalJ = j;
                ServiceScene sceneI = scenes.get(finalJ % scenes.size());
                Thread thread=toThread(sceneI, nbColone, finalJ,scene.getWidth()%tailleParti,tailleParti, disp, tachesOmettre);
                thread.start();
                threads.add(thread);
            }
        }
        for (Thread thread : threads) {
            thread.join();
        }
        while (tachesOmettre.size()>0) {
            threads = new ArrayList<>();
            while (tachesOmettre.size() > 0) {
                int[] tache = tachesOmettre.get(0);
                tachesOmettre.remove(0);
                ServiceScene sceneI = scenes.get(tache[0] % scenes.size());
                Thread thread=toThread(sceneI, tache[0], tache[1],scene.getWidth()%tailleParti,tailleParti, disp, tachesOmettre);
                thread.start();
                threads.add(thread);
            }
            for (Thread thread : threads) {
                thread.join();
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
                    System.out.println("Enlever un service de calcul: " + scenes.get(0));
                }
            }
        }
    }

    private Thread toThread(ServiceScene serviceScene,int finalI,int finalJ,int tailleX,int tailleY,ServiceDisp disp,List<int[]> tachesOmettre){
        return new Thread(() -> {
            Image image;
            try {
                image = serviceScene.compute(finalI * tailleParti, finalJ * tailleParti, tailleX, tailleY);
                disp.setImage(image, finalI * tailleParti, finalJ * tailleParti);
            } catch (RemoteException e) {
                synchronized (scenes) {
                    if (scenes.contains(serviceScene)) {
                        scenes.remove(serviceScene);
                        System.out.println("Enlever un service de calcul: " + serviceScene);
                    }
                }
                synchronized (tachesOmettre) {
                    tachesOmettre.add(new int[]{finalI, finalJ});
                }
            }
        });
    }
}

