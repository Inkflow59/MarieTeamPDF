package fr.marieteam.pdf.marieteampdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import fr.marieteam.pdf.marieteampdf.api.MarieTeamAPI;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class GenererPDFController {

    @FXML
    private ComboBox<String> comboBoxBateaux;
    
    @FXML
    private Button btnAfficherInfo;
    
    @FXML
    private Button btnGenererPDF;
    
    @FXML
    private Button btnRetour;
    
    @FXML
    private VBox bateauInfoBox;
    
    @FXML
    private VBox traverseesInfoBox;
    
    private String selectedBateau;
    private ObservableList<String> bateauxList = FXCollections.observableArrayList();
    private MarieTeamAPI api;
    
    @FXML
    public void initialize() {
        api = new MarieTeamAPI();
        // Charger la liste des bateaux depuis l'API
        loadBateaux();
        
        // Configurer le ComboBox
        comboBoxBateaux.setItems(bateauxList);
        comboBoxBateaux.setOnAction(e -> {
            selectedBateau = comboBoxBateaux.getValue();
            btnAfficherInfo.setDisable(selectedBateau == null);
        });
        
        // Désactiver le bouton d'affichage initialement
        btnAfficherInfo.setDisable(true);
    }
    
    private void loadBateaux() {
        try {
            ArrayList<String> boats = api.getAllBoats();
            bateauxList.clear();
            bateauxList.addAll(boats);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des bateaux", e.getMessage());
        }
    }
      @FXML
    public void afficherInformationsBateau() { // changé en public pour être appelable par FXML
        if (selectedBateau == null) {
            showError("Erreur", "Aucun bateau sélectionné");
            return;
        }
        
        try {
            // Extraire le nom simple du bateau (avant le premier " - ")
            String simpleName = selectedBateau.split(" - ")[0];
            System.out.println("Récupération des informations pour le bateau: " + simpleName);
            
            Map<String, Object> boatInfo = api.getBoatInfoByName(simpleName);
            
            if (boatInfo == null || boatInfo.isEmpty()) {
                showError("Erreur", "Aucune information trouvée pour le bateau: " + simpleName);
                return;
            }
            
            // Afficher les informations du bateau
            bateauInfoBox.getChildren().clear();
            bateauInfoBox.getChildren().add(new Label("Nom: " + boatInfo.get("nom")));
            bateauInfoBox.getChildren().add(new Label("Catégorie: " + boatInfo.get("categorieLibelle")));
            bateauInfoBox.getChildren().add(new Label("Capacité: " + boatInfo.get("capaciteMax")));
            
            // Afficher les traversées
            Object traverseesObj = boatInfo.get("traversees");
            if (traverseesObj instanceof ArrayList<?>) {
                ArrayList<Map<String, Object>> traversees = new ArrayList<>();
                for (Object obj : (ArrayList<?>) traverseesObj) {
                    if (obj instanceof Map<?, ?>) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) obj;
                        traversees.add(map);
                    }
                }
                traverseesInfoBox.getChildren().clear();
                
                if (traversees.isEmpty()) {
                    traverseesInfoBox.getChildren().add(new Label("Aucune traversée trouvée pour ce bateau"));
                } else {
                    for (Map<String, Object> traversee : traversees) {
                        VBox traverseeBox = new VBox(5);
                        traverseeBox.getChildren().add(new Label("Date: " + traversee.get("date")));
                        traverseeBox.getChildren().add(new Label("Heure: " + traversee.get("heure")));
                        traverseeBox.getChildren().add(new Label("Port de départ: " + traversee.get("portDepart")));
                        traverseeBox.getChildren().add(new Label("Port d'arrivée: " + traversee.get("portArrivee")));
                        traverseeBox.getChildren().add(new Label("Secteur: " + traversee.get("secteur")));
                        traverseesInfoBox.getChildren().add(traverseeBox);
                    }
                }
                
                btnGenererPDF.setDisable(false);
            } else {
                traverseesInfoBox.getChildren().clear();
                traverseesInfoBox.getChildren().add(new Label("Format de données de traversées invalide"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la récupération des informations", "Détails: " + e.getMessage());
        }
    }    @FXML
    public void genererPDF() { // changé en public pour être appelable par FXML
        if (selectedBateau == null) return;
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName(selectedBateau.split(" - ")[0] + "_rapport.pdf");
        
        File file = fileChooser.showSaveDialog(btnGenererPDF.getScene().getWindow());
        if (file == null) return;
        
        try {
            // Extraire le nom simple du bateau (avant le premier " - ")
            String simpleName = selectedBateau.split(" - ")[0];
            Map<String, Object> boatInfo = api.getBoatInfoByName(simpleName);
              // Utiliser try-with-resources pour fermer automatiquement les ressources
            try (PdfWriter writer = new PdfWriter(file);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf)) {
            
            // En-tête
            Paragraph title = new Paragraph("Rapport du bateau " + simpleName)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20);
            document.add(title);
            
            // Informations du bateau
            document.add(new Paragraph("\nInformations du bateau:").setFontSize(16));
            Table bateauTable = new Table(2);
            bateauTable.setWidth(UnitValue.createPercentValue(100));
            
            addTableRow(bateauTable, "Nom", (String) boatInfo.get("nom"));
            addTableRow(bateauTable, "Catégorie", (String) boatInfo.get("categorieLibelle"));
            addTableRow(bateauTable, "Capacité", String.valueOf(boatInfo.get("capaciteMax")));
            
            document.add(bateauTable);
            
            // Ajouter les informations des traversées s'il y en a (limité aux 10 premières)
            Object traverseesObj = boatInfo.get("traversees");
            if (traverseesObj instanceof ArrayList<?> && !((ArrayList<?>) traverseesObj).isEmpty()) {
                document.add(new Paragraph("\nTraversées:").setFontSize(16));
                Table traverseesTable = new Table(5);
                traverseesTable.setWidth(UnitValue.createPercentValue(100));
                
                // En-têtes
                addTableRow(traverseesTable, "Date", "Heure", "Départ", "Arrivée", "Secteur");
                
                // Données des traversées (limité aux 10 premières)
                ArrayList<?> traversees = (ArrayList<?>) traverseesObj;
                int count = 0;
                int maxTraversees = 10; // Limiter à 10 traversées
                
                for (Object obj : traversees) {
                    if (count >= maxTraversees) break; // Sortir après 10 traversées
                    
                    if (obj instanceof Map<?, ?>) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> traversee = (Map<String, Object>) obj;
                        addTableRow(traverseesTable, 
                            String.valueOf(traversee.get("date")),
                            String.valueOf(traversee.get("heure")),
                            String.valueOf(traversee.get("portDepart")),
                            String.valueOf(traversee.get("portArrivee")),
                            String.valueOf(traversee.get("secteur"))
                        );
                        count++;
                    }
                }
                
                document.add(traverseesTable);
                
                // Ajouter une note si plus de 10 traversées sont disponibles
                if (traversees.size() > maxTraversees) {
                    document.add(new Paragraph("Note: Seules les 10 premières traversées sont affichées sur un total de " + traversees.size())
                        .setFontSize(10)
                        .setItalic());
                }
            }
            
            // Ajouter l'image du bateau en utilisant la méthode robuste de recherche
            try {
                // Utiliser notre méthode améliorée pour trouver l'image
                File imageFile = findBoatImage(simpleName);
                
                // Si une image a été trouvée, l'ajouter au PDF
                if (imageFile != null && imageFile.exists() && imageFile.length() > 0) {
                    System.out.println("Utilisation de l'image: " + imageFile.getAbsolutePath());
                    System.out.println("Taille de l'image: " + imageFile.length() + " bytes");
                    
                    // Ajouter l'image au PDF
                    byte[] imageData = java.nio.file.Files.readAllBytes(imageFile.toPath());
                    Image image = new Image(ImageDataFactory.create(imageData));
                    image.setWidth(UnitValue.createPercentValue(60));
                    image.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
                    document.add(new Paragraph("\nImage du bateau:").setFontSize(16));
                    document.add(image);
                    System.out.println("Image ajoutée au PDF avec succès");
                } else {                System.err.println("Aucune image trouvée pour le bateau: " + simpleName);
                    document.add(new Paragraph("\nAucune image disponible pour ce bateau").setFontSize(14).setItalic());
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'ajout de l'image au PDF: " + e.getMessage());
                e.printStackTrace();
                document.add(new Paragraph("\nErreur lors du chargement de l'image").setFontSize(14).setItalic());
            }
            }
              // Le document se fermera automatiquement avec try-with-resources
            showInfo("Succès", "Le PDF a été généré avec succès!");
            
        } catch (FileNotFoundException e) {
            System.err.println("Fichier non trouvé: " + e.getMessage());
            showError("Erreur lors de la génération du PDF", "Impossible de créer le fichier PDF. " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erreur d'entrée/sortie: " + e.getMessage());
            showError("Erreur lors de la génération du PDF", "Erreur lors de l'écriture du PDF. " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de la récupération des données", "Une erreur est survenue. " + e.getMessage());
        }
    }
    
    private void addTableRow(Table table, String... cells) {
        for (String cell : cells) {
            String cellValue = (cell != null) ? cell : "";
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(cellValue)));
        }
    }
      @FXML
    public void retourAccueil() { // changé en public pour être appelable par FXML
        try {
            MainApp.setRoot("Welcome.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du retour à l'accueil", e.getMessage());
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Trouve un fichier image pour un bateau donné
     * Recherche dans plusieurs emplacements possibles pour être plus robuste
     */
    private File findBoatImage(String boatName) {
        System.out.println("Recherche d'une image pour le bateau: " + boatName);
        
        // Normaliser le nom pour la recherche de fichier
        String normalizedName = boatName.replaceAll("[^a-zA-Z0-9]", "_");
        System.out.println("Nom normalisé pour la recherche: " + normalizedName);
        
        // Liste des chemins possibles à vérifier
        String[] possiblePaths = {
            "images/" + normalizedName + ".jpg",
            "images/" + normalizedName + ".png",
            "./images/" + normalizedName + ".jpg",
            "./images/" + normalizedName + ".png",
            "../images/" + normalizedName + ".jpg",
            "../images/" + normalizedName + ".png"
        };
        
        // Afficher le répertoire de travail actuel pour le débogage
        System.out.println("Répertoire de travail actuel: " + System.getProperty("user.dir"));
        
        // Vérifier si le dossier images existe
        File imagesDir = new File("images");
        System.out.println("Le dossier 'images' existe-t-il? " + imagesDir.exists());
        System.out.println("Chemin absolu du dossier 'images': " + imagesDir.getAbsolutePath());
        
        // Liste tous les fichiers du dossier images pour le débogage
        if (imagesDir.exists() && imagesDir.isDirectory()) {
            System.out.println("Contenu du dossier 'images':");
            File[] allFiles = imagesDir.listFiles();
            if (allFiles != null) {
                for (File f : allFiles) {
                    System.out.println(" - " + f.getName());
                }
            } else {
                System.out.println("Impossible de lister les fichiers du dossier 'images'");
            }
        }
        
        // Essayer les chemins directs d'abord
        for (String path : possiblePaths) {
            File file = new File(path);
            System.out.println("Vérification du chemin: " + path + " (absolu: " + file.getAbsolutePath() + ")");
            if (file.exists()) {
                System.out.println("Image trouvée: " + file.getAbsolutePath());
                return file;
            }
        }
        
        // Si aucun chemin direct ne fonctionne, chercher dans le dossier images
        if (imagesDir.exists() && imagesDir.isDirectory()) {
            // D'abord, essayer de faire correspondre sur la base du nom normalisé
            File[] matchingFiles = imagesDir.listFiles((dir, name) -> 
                name.toLowerCase().contains(normalizedName.toLowerCase()) && 
                (name.endsWith(".jpg") || name.endsWith(".png"))
            );
            
            if (matchingFiles != null && matchingFiles.length > 0) {
                System.out.println("Image trouvée par correspondance de nom: " + matchingFiles[0].getName());
                return matchingFiles[0];
            }
            
            // Ensuite, essayer de faire correspondre sur des parties du nom original
            String[] searchTerms = boatName.split(" ");
            for (String term : searchTerms) {
                if (term.length() > 2) { // Ignorer les termes trop courts
                    String searchTerm = term.toLowerCase();
                    System.out.println("Recherche d'images contenant: " + searchTerm);
                    
                    matchingFiles = imagesDir.listFiles((dir, name) -> 
                        name.toLowerCase().contains(searchTerm) && 
                        (name.endsWith(".jpg") || name.endsWith(".png"))
                    );
                    
                    if (matchingFiles != null && matchingFiles.length > 0) {
                        System.out.println("Image trouvée par correspondance partielle: " + matchingFiles[0].getName());
                        return matchingFiles[0];
                    }
                }
            }
            
            // En dernier recours, prendre n'importe quelle image
            File[] anyImage = imagesDir.listFiles((dir, name) -> 
                name.endsWith(".jpg") || name.endsWith(".png")
            );
            
            if (anyImage != null && anyImage.length > 0) {
                System.out.println("Aucune correspondance trouvée, utilisation d'une image par défaut: " + anyImage[0].getName());
                return anyImage[0];
            }
        }
        
        System.out.println("Aucune image trouvée pour le bateau: " + boatName);
        return null;
    }
}