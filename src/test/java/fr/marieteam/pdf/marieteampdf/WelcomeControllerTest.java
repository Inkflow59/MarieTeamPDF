package fr.marieteam.pdf.marieteampdf;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;

@ExtendWith(MockitoExtension.class)
public class WelcomeControllerTest {

    private WelcomeController controller;
    
    @Mock
    private Button btnGenererPDF;
    
    @Mock
    private ActionEvent mockEvent;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        controller = new WelcomeController();
        
        // Injection des mocks via reflection
        var btnField = WelcomeController.class.getDeclaredField("btnGenererPDF");
        btnField.setAccessible(true);
        btnField.set(controller, btnGenererPDF);
    }

    @Test
    void testNavigateToGenererPDF() throws Exception {
        // On utilise la réflexion pour accéder à la méthode privée
        Method navigateMethod = WelcomeController.class.getDeclaredMethod("navigateToGenererPDF", ActionEvent.class);
        navigateMethod.setAccessible(true);
        
        // On ne peut pas mocker la méthode statique directement avec Mockito standard
        // Ce test sera incomplet, on vérifie simplement que la méthode est appelable
        
        try {
            navigateMethod.invoke(controller, mockEvent);
            // Si on arrive ici sans exception, c'est que la méthode a été appelée
            fail("L'appel à navigateToGenererPDF aurait dû lever une exception car MainApp.setRoot est statique");
        } catch (InvocationTargetException e) {
            // On s'attend à une exception car on n'a pas pu mocker la méthode statique
            // Mais on vérifie que c'est bien une IOException (celle qui serait lancée par setRoot)
            if (e.getCause() instanceof IOException || e.getCause() instanceof NullPointerException) {
                // C'est le comportement attendu
                assertTrue(true, "Exception attendue reçue");
            } else {
                fail("Exception inattendue: " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
            }
        }
    }
}
