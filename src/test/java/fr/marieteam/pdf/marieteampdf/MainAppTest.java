package fr.marieteam.pdf.marieteampdf;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

@ExtendWith(ApplicationExtension.class)
public class MainAppTest {

    @Test
    void testSetRoot() {
        try {
            // Cette méthode est difficile à tester avec JUnit car elle implique JavaFX
            // On vérifie simplement qu'elle ne lance pas d'exception avec un FXML valide
            MainApp.setRoot("Welcome");
            // Si on arrive ici sans exception, le test est considéré comme réussi
            assertTrue(true, "SetRoot a fonctionné sans exception");
        } catch (IOException e) {
            fail("setRoot a échoué avec une IOException: " + e.getMessage());
        }
    }

    @Test
    void testMainAppInitialization() {
        // Test que la classe MainApp peut être instanciée sans erreur
        // C'est un test minimal, mais il vérifie les imports et la structure de base
        MainApp newApp = new MainApp();
        assertNotNull(newApp, "L'instance de MainApp ne devrait pas être null");
    }
}
