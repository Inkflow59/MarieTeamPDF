package fr.marieteam.pdf.marieteampdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
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
    private void afficherInformationsBateau() {
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
    }
    
    @FXML
    private void genererPDF() {
        if (selectedBateau == null) return;
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName(selectedBateau + "_rapport.pdf");
        
        File file = fileChooser.showSaveDialog(btnGenererPDF.getScene().getWindow());
        if (file == null) return;
        
        try {
            Map<String, Object> boatInfo = api.getBoatInfoByName(selectedBateau);
            
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // En-tête
            Paragraph title = new Paragraph("Rapport du bateau " + selectedBateau)
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
            
            // Liste des traversées
            document.add(new Paragraph("\nListe des traversées:").setFontSize(16));
            Table traverseeTable = new Table(5);
            traverseeTable.setWidth(UnitValue.createPercentValue(100));
            
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
                for (Map<String, Object> traversee : traversees) {
                    addTableRow(traverseeTable, 
                        (String) traversee.get("date"),
                        (String) traversee.get("heure"),
                        (String) traversee.get("portDepart"),
                        (String) traversee.get("portArrivee"),
                        (String) traversee.get("secteur"));
                }
            }
            
            document.add(traverseeTable);
            document.close();
            
            showInfo("Succès", "Le PDF a été généré avec succès!");
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showError("Erreur lors de la génération du PDF", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la récupération des données", e.getMessage());
        }
    }
    
    private void addTableRow(Table table, String... cells) {
        for (String cell : cells) {
            String cellValue = (cell != null) ? cell : "";
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(cellValue)));
        }
    }
    
    @FXML
    private void retourAccueil() {
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
} 