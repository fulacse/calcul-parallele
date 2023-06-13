import raytracer.*;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

public class CentreCalcule implements ServiceCentreCalcule {
    List<FabriquateurScene> fabriquateurScenes;
    List<ServiceScene> scenes;
    int tailleParti = 10;

    public CentreCalcule() {
        scenes = new ArrayList<>();
        fabriquateurScenes = new ArrayList<>();
    }

    @Override
    public synchronized void addCalculeur(FabriquateurScene fabriquateurScene) throws RemoteException {
        fabriquateurScenes.add(fabriquateurScene);
        System.out.println("Ajouter un calculeur: " + fabriquateurScene.toString());
    }

    public synchronized void calculer(ServiceDisp disp, Scene scene) throws RemoteException, InterruptedException {

        /* Arreter les services de calcul*/
        Iterator<FabriquateurScene> fabriquateurSceneIterator = fabriquateurScenes.iterator();
        List<Thread> threads = new ArrayList<>();
        while (fabriquateurSceneIterator.hasNext()) {
            FabriquateurScene fabriquateurScene = fabriquateurSceneIterator.next();
            Thread thread = new Thread(() -> {
                try {
                    fabriquateurScene.stop();
                } catch (ConnectException connectException) {
                    fabriquateurScenes.remove(fabriquateurScene);
                    System.out.println("Enlever un calculeur: " + fabriquateurScene);
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
        fabriquateurSceneIterator = fabriquateurScenes.iterator();
        threads = new ArrayList<>();
        while (fabriquateurSceneIterator.hasNext()) {
            FabriquateurScene fabriquateurScene = fabriquateurSceneIterator.next();
            Thread thread = new Thread(() -> {
                try {
                    scenes.add(fabriquateurScene.convertirService(scene));
                } catch (ConnectException connectException) {
                    fabriquateurScenes.remove(fabriquateurScene);
                    System.out.println("Enlever un calculeur: " + fabriquateurScene);
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
                Thread thread = new Thread(() -> {
                    Image image;
                    try {
                        image = serviceScene.compute(finalI * tailleParti, finalJ * tailleParti, tailleParti, tailleParti);
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
                thread.start();
                threads.add(thread);
            }
        }
        for (Thread thread : threads) {
            thread.join();
        }
        while (tachesOmettre.size() > 0) {
            int[] tache = tachesOmettre.get(0);
            tachesOmettre.remove(0);
            ServiceScene serviceScene = scenes.get((tache[0] + tache[1]) % scenes.size());
            int finalI = tache[0];
            int finalJ = tache[1];
            try {
                Image image = serviceScene.compute(finalI * tailleParti, finalJ * tailleParti, tailleParti, tailleParti);
                disp.setImage(image, finalI * tailleParti, finalJ * tailleParti);
            } catch (RemoteException e) {
                scenes.remove(serviceScene);
                System.out.println("Enlever un service de calcul: " + serviceScene);
                tachesOmettre.add(new int[]{finalI, finalJ});
            }
        }

        /*si la taille de l'image n'est pas un multiple de la taille des parties, completer l'image*/
        threads = new ArrayList<>();
        if(scene.getHeight()>nbLigne*tailleParti){
            for (int i=0;i<nbColone;i++){
                ServiceScene sceneI = scenes.get(i % scenes.size());
                int finalI = i;
                List<int[]> finalTachesOmettre = tachesOmettre;
                Thread thread=new Thread(()->{
                    try {
                        Image image = sceneI.compute(finalI *tailleParti, nbLigne*tailleParti, tailleParti, scene.getHeight()-nbLigne*tailleParti);
                        disp.setImage(image, finalI *tailleParti, nbLigne*tailleParti);
                    }catch (RemoteException e) {
                        synchronized (scenes) {
                            if (scenes.contains(sceneI)) {
                                scenes.remove(sceneI);
                                System.out.println("Enlever un service de calcul: " + sceneI);
                            }
                        }
                        synchronized (finalTachesOmettre) {
                            finalTachesOmettre.add(new int[]{finalI, nbLigne});
                        }
                    }
                });
                thread.start();
                threads.add(thread);
            }
        }
        for (Thread thread : threads) {
            thread.join();
        }
        while (tachesOmettre.size()>0){
            int[] tache = tachesOmettre.get(0);
            tachesOmettre.remove(0);
            ServiceScene sceneI = scenes.get(tache[0] % scenes.size());
            try {
                Image image = sceneI.compute(tache[0] *tailleParti, tache[1] *tailleParti, tailleParti, scene.getHeight()-nbLigne*tailleParti);
                disp.setImage(image, tache[0] *tailleParti, tache[1] *tailleParti);
            }catch (RemoteException e) {
                scenes.remove(sceneI);
                System.out.println("Enlever un service de calcul: " + sceneI);
                tachesOmettre.add(new int[]{tache[0], tache[1]});
            }
        }
        threads = new ArrayList<>();
        if(scene.getWidth()>nbColone*tailleParti){
            for (int j = 0; j <nbLigne; j++){
                int finalJ = j;
                ServiceScene sceneI = scenes.get(finalJ % scenes.size());
                Thread thread=new Thread(()->{
                    try {
                        Image image = sceneI.compute(nbColone*tailleParti, finalJ *tailleParti, scene.getWidth()-nbColone*tailleParti, tailleParti);
                        disp.setImage(image, nbColone*tailleParti, finalJ *tailleParti);
                    }catch (RemoteException e) {
                        synchronized (scenes) {
                            if (scenes.contains(sceneI)) {
                                scenes.remove(sceneI);
                                System.out.println("Enlever un service de calcul: " + sceneI);
                            }
                        }
                        synchronized (tachesOmettre) {
                            tachesOmettre.add(new int[]{nbColone, finalJ});
                        }
                    }
                });
                thread.start();
                threads.add(thread);
            }
        }
        for (Thread thread : threads) {
            thread.join();
        }
        while (tachesOmettre.size()>0){
            int[] tache = tachesOmettre.get(0);
            tachesOmettre.remove(0);
            ServiceScene sceneI = scenes.get(tache[0] % scenes.size());
            try {
                Image image = sceneI.compute(tache[0] *tailleParti, tache[1] *tailleParti, scene.getWidth()-nbColone*tailleParti, tailleParti);
                disp.setImage(image, tache[0] *tailleParti, tache[1] *tailleParti);
            }catch (RemoteException e) {
                scenes.remove(sceneI);
                System.out.println("Enlever un service de calcul: " + sceneI);
                tachesOmettre.add(new int[]{tache[0], tache[1]});
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

