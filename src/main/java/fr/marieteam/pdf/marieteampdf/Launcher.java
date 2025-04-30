package fr.marieteam.pdf.marieteampdf;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Classe Launcher qui sert de point d'entrée pour le programme packagé
 * Cette classe est nécessaire pour contourner certaines limitations de JavaFX
 * lors du packaging avec Maven
 */
public class Launcher {
    /**
     * Point d'entrée principal de l'application
     * Nécessaire pour le packaging en exécutable natif
     */
    public static void main(String[] args) {
        // Définir le chemin des images de manière relative
        String userDir = System.getProperty("user.dir");
        Path imagesPath = Paths.get(userDir, "images");
        File imagesDir = imagesPath.toFile();

        if (!imagesDir.exists()) {
            System.out.println("Création du dossier images à : " + imagesDir.getAbsolutePath());
            if (!imagesDir.mkdirs()) {
                System.err.println("Erreur : Impossible de créer le dossier images");
            }
        } else {
            System.out.println("Dossier images trouvé à : " + imagesDir.getAbsolutePath());
            File[] files = imagesDir.listFiles();
            if (files != null && files.length > 0) {
                System.out.println("Fichiers dans le dossier images :");
                for (File file : files) {
                    System.out.println(" - " + file.getName());
                }
            } else {
                System.out.println("Le dossier images est vide");
            }
        }
        
        // Lancer l'application principale
        MainApp.main(args);
    }
}
