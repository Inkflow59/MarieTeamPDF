package fr.marieteam.pdf.marieteampdf;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class WelcomeController {
    
    @FXML
    private Button btnGenererPDF;
    
    @FXML
    private void navigateToGenererPDF(ActionEvent event) {
        try {
            MainApp.setRoot("GenererPDF.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            // Afficher une alerte en cas d'erreur
            System.err.println("Erreur lors du chargement de l'écran de génération PDF: " + e.getMessage());
        }
    }
}