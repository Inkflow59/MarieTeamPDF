package fr.marieteam.pdf.marieteampdf;

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
        // On vérifie que le dossier images est accessible
        java.io.File imagesDir = new java.io.File("images");
        if (!imagesDir.exists()) {
            System.out.println("Dossier images non trouvé à : " + imagesDir.getAbsolutePath());
            // Essayer de créer le dossier si besoin
            imagesDir.mkdirs();
            // Afficher le répertoire de travail actuel pour le débogage
            System.out.println("Répertoire de travail actuel : " + System.getProperty("user.dir"));
        } else {
            System.out.println("Dossier images trouvé à : " + imagesDir.getAbsolutePath());
            // Lister les fichiers du dossier images
            java.io.File[] files = imagesDir.listFiles();
            if (files != null) {
                System.out.println("Fichiers dans le dossier images :");
                for (java.io.File file : files) {
                    System.out.println(" - " + file.getName());
                }
            }
        }
        
        // Lancer l'application principale
        MainApp.main(args);
    }
}
