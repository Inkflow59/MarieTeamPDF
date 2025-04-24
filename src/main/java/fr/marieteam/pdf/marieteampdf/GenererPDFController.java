package fr.marieteam.pdf.marieteampdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

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
    
    @FXML
    public void initialize() {
        // Charger la liste des bateaux depuis la base de données
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
        try (Connection conn = getConnection()) {
            String query = "SELECT nom FROM bateau ORDER BY nom";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    bateauxList.add(rs.getString("nom"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des bateaux", e.getMessage());
        }
    }
    
    @FXML
    private void afficherInformationsBateau() {
        if (selectedBateau == null) return;
        
        try (Connection conn = getConnection()) {
            // Récupérer les informations du bateau
            String bateauQuery = "SELECT * FROM bateau WHERE nom = ?";
            try (PreparedStatement stmt = conn.prepareStatement(bateauQuery)) {
                stmt.setString(1, selectedBateau);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    // Afficher les informations du bateau
                    bateauInfoBox.getChildren().clear();
                    bateauInfoBox.getChildren().add(new Label("Nom: " + rs.getString("nom")));
                    bateauInfoBox.getChildren().add(new Label("Type: " + rs.getString("type")));
                    bateauInfoBox.getChildren().add(new Label("Capacité: " + rs.getInt("capacite")));
                    
                    // Récupérer les traversées
                    String traverseesQuery = "SELECT * FROM traversee WHERE id_bateau = ? ORDER BY date_depart DESC";
                    try (PreparedStatement traverseesStmt = conn.prepareStatement(traverseesQuery)) {
                        traverseesStmt.setInt(1, rs.getInt("id"));
                        ResultSet traverseesRs = traverseesStmt.executeQuery();
                        
                        traverseesInfoBox.getChildren().clear();
                        while (traverseesRs.next()) {
                            VBox traverseeBox = new VBox(5);
                            traverseeBox.getChildren().add(new Label("Date de départ: " + 
                                traverseesRs.getTimestamp("date_depart").toLocalDateTime().format(
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
                            traverseeBox.getChildren().add(new Label("Port de départ: " + 
                                traverseesRs.getString("port_depart")));
                            traverseeBox.getChildren().add(new Label("Port d'arrivée: " + 
                                traverseesRs.getString("port_arrivee")));
                            traverseesInfoBox.getChildren().add(traverseeBox);
                        }
                    }
                    
                    btnGenererPDF.setDisable(false);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la récupération des informations", e.getMessage());
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
            
            try (Connection conn = getConnection()) {
                String query = "SELECT * FROM bateau WHERE nom = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, selectedBateau);
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        addTableRow(bateauTable, "Nom", rs.getString("nom"));
                        addTableRow(bateauTable, "Type", rs.getString("type"));
                        addTableRow(bateauTable, "Capacité", String.valueOf(rs.getInt("capacite")));
                    }
                }
            }
            document.add(bateauTable);
            
            // Liste des traversées
            document.add(new Paragraph("\nListe des traversées:").setFontSize(16));
            Table traverseeTable = new Table(4);
            traverseeTable.setWidth(UnitValue.createPercentValue(100));
            
            try (Connection conn = getConnection()) {
                String query = "SELECT t.*, b.id FROM traversee t " +
                             "JOIN bateau b ON t.id_bateau = b.id " +
                             "WHERE b.nom = ? ORDER BY t.date_depart DESC";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, selectedBateau);
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        addTableRow(traverseeTable, 
                            rs.getTimestamp("date_depart").toLocalDateTime().format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                            rs.getString("port_depart"),
                            rs.getString("port_arrivee"),
                            String.valueOf(rs.getInt("nb_passagers")));
                    }
                }
            }
            document.add(traverseeTable);
            
            document.close();
            
            showInfo("Succès", "Le PDF a été généré avec succès!");
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showError("Erreur lors de la génération du PDF", e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la récupération des données", e.getMessage());
        }
    }
    
    private void addTableRow(Table table, String... cells) {
        for (String cell : cells) {
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(cell)));
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
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/marieteam",
            "root",
            "root"
        );
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