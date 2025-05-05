package fr.marieteam.pdf.marieteampdf.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MarieTeamAPITest {

    private MarieTeamAPI api;
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        
        // Création d'un mock pour MarieTeamAPI avec une méthode pour injecter notre connexion mock
        api = new MarieTeamAPI() {
            @Override
            protected Connection getConnection() {
                return mockConnection;
            }
        };
        
        // Configuration de base des mocks
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    }
    @Test
    public void testGetAllBoats() throws SQLException, Exception {
        // Arrange
        when(mockResultSet.next()).thenReturn(true, true, false); // 2 bateaux
        when(mockResultSet.getString("Nom du Bateau")).thenReturn("Bateau 1", "Bateau 2");
        when(mockResultSet.getString("Catégories")).thenReturn("A - Standard", "B - Confort");
        when(mockResultSet.getString("Capacités")).thenReturn("200", "300");
        when(mockResultSet.getInt("Nombre de Traversées")).thenReturn(5, 10);
        when(mockResultSet.getString("Image")).thenReturn("image1.jpg", "image2.jpg");
        
        // Act
        ArrayList<String> boats = api.getAllBoats();
        
        // Assert
        assertNotNull(boats, "La liste de bateaux ne devrait pas être null");
        assertEquals(2, boats.size(), "La liste devrait contenir exactement 2 bateaux");
        assertTrue(boats.get(0).contains("Bateau 1"), "Le premier bateau devrait contenir 'Bateau 1'");
        assertTrue(boats.get(1).contains("Bateau 2"), "Le second bateau devrait contenir 'Bateau 2'");
        
        // Verify
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).executeQuery();
    }
    
    @Test
    public void testGetBoatInfoByName() throws SQLException, Exception {
        // Arrange
        when(mockResultSet.next()).thenReturn(true, false); // Un bateau trouvé
        when(mockResultSet.getInt("idBat")).thenReturn(1);
        when(mockResultSet.getString("nomBat")).thenReturn("Bateau Test");
        when(mockResultSet.getInt("capaciteMax")).thenReturn(200);
        when(mockResultSet.getString("Equipements")).thenReturn("Wifi,Bar");
        when(mockResultSet.getString("lienImage")).thenReturn("image.jpg");
        
        // Act
        Map<String, Object> boatInfo = api.getBoatInfoByName("Bateau Test");
        
        // Assert
        assertNotNull(boatInfo, "Les informations du bateau ne devraient pas être null");
        assertEquals(1, boatInfo.get("idBat"), "L'ID du bateau devrait être 1");
        assertEquals("Bateau Test", boatInfo.get("nomBat"), "Le nom du bateau devrait être 'Bateau Test'");
        assertEquals(200, boatInfo.get("capaciteMax"), "La capacité maximale devrait être 200");
        assertEquals("Wifi,Bar", boatInfo.get("Equipements"), "Les équipements devraient être 'Wifi,Bar'");
        assertEquals("image.jpg", boatInfo.get("lienImage"), "Le lien de l'image devrait être 'image.jpg'");
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT b.*, MAX(c.capaciteMax) AS capaciteMax FROM bateau b"));
        verify(mockPreparedStatement).setString(1, "Bateau Test");
        verify(mockPreparedStatement).executeQuery();
    }
    
    @Test
    public void testUpdateBoatInfo() throws SQLException, Exception {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 ligne affectée
        
        // Act
        boolean result = api.updateBoatInfo(1, "Nouveau Nom", 300, "Wifi,Restaurant,Cinéma");
        
        // Assert
        assertTrue(result, "La mise à jour devrait réussir");
        
        // Verify
        verify(mockConnection).prepareStatement(contains("UPDATE bateau"));
        verify(mockPreparedStatement).setString(1, "Nouveau Nom");
        verify(mockPreparedStatement).setString(2, "Wifi,Restaurant,Cinéma");
        verify(mockPreparedStatement).setInt(3, 1);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    public void testGetDetailedBoatsInfo() throws SQLException, Exception {
        // Arrange
        when(mockResultSet.next()).thenReturn(true, false); // Un bateau trouvé
        when(mockResultSet.getInt("idBat")).thenReturn(1);
        when(mockResultSet.getString("nomBat")).thenReturn("Bateau Info");
        when(mockResultSet.getInt("capaciteMax")).thenReturn(250);
        when(mockResultSet.getString("Equipements")).thenReturn("Bar,Restaurant");
        when(mockResultSet.getString("lienImage")).thenReturn("image.jpg");

        // Préparer un second ResultSet pour les traversées
        ResultSet mockTraverseesResultSet = mock(ResultSet.class);
        PreparedStatement mockTraverseesStatement = mock(PreparedStatement.class);
        
        when(mockConnection.prepareStatement(contains("SELECT t.*, l.code, p_dep.nomPort"))).thenReturn(mockTraverseesStatement);
        when(mockTraverseesStatement.executeQuery()).thenReturn(mockTraverseesResultSet);
        when(mockTraverseesResultSet.next()).thenReturn(false); // Pas de traversées pour simplifier
        
        // Act
        Map<Integer, Map<String, Object>> detailedInfo = api.getDetailedBoatsInfo();
        
        // Assert
        assertNotNull(detailedInfo, "Les informations détaillées ne devraient pas être null");
        assertEquals(1, detailedInfo.size(), "Il devrait y avoir exactement 1 bateau");
        
        Map<String, Object> boat = detailedInfo.get(1);
        assertNotNull(boat, "Les informations du bateau ne devraient pas être null");
        assertEquals("Bateau Info", boat.get("nomBat"), "Le nom du bateau devrait être 'Bateau Info'");
        assertEquals(250, boat.get("capaciteMax"), "La capacité maximale devrait être 250");
        assertEquals("Bar,Restaurant", boat.get("Equipements"), "Les équipements devraient être 'Bar,Restaurant'");
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT b.*, MAX(c.capaciteMax) AS capaciteMax FROM bateau b"));
        verify(mockPreparedStatement).executeQuery();
    }
    
    @Test
    public void testGetAllBoatsEmptyResult() throws SQLException, Exception {
        // Arrange
        when(mockResultSet.next()).thenReturn(false); // Aucun bateau trouvé
        
        // Act
        ArrayList<String> boats = api.getAllBoats();
        
        // Assert
        assertNotNull(boats, "La liste de bateaux ne devrait pas être null même si vide");
        assertTrue(boats.isEmpty(), "La liste devrait être vide");
        
        // Verify
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).executeQuery();
    }
}
