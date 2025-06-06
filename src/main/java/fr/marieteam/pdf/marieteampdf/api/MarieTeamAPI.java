package fr.marieteam.pdf.marieteampdf.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MarieTeamAPI {
    private final String hostname = "localhost";
    private final String port = "3306";
    private final String database = "marieteam";    private final String username = "root";
    private final String password = "";
    
    /**
     * Obtient une connexion à la base de données
     * Cette méthode est protégée pour permettre de la surcharger dans les tests
     * @return une connexion à la base de données
     * @throws SQLException si une erreur survient lors de la connexion
     */
    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, username, password);
    }
    
    public ArrayList<String> getAllBoats() throws Exception {
        System.out.println("Récupération de la liste des bateaux");
        ArrayList<String> boats = new ArrayList<>();        String query = "SELECT \n" +
                "    b.idBat, b.nomBat AS 'Nom du Bateau',\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT(c.lettre, ' - ', c.libelleCat) SEPARATOR ', ') AS 'Catégories',\n" +
                "    GROUP_CONCAT(DISTINCT co.capaciteMax SEPARATOR ', ') AS 'Capacités',\n" +
                "    COUNT(DISTINCT t.numTra) AS 'Nombre de Traversées',\n" +
                "    b.lienImage AS 'Image',\n" +
                "    b.Equipements\n" +
                "FROM \n" +
                "    bateau b\n" +
                "LEFT JOIN \n" +
                "    contenir co ON b.idBat = co.idBat\n" +
                "LEFT JOIN \n" +
                "    categorie c ON co.lettre = c.lettre\n" +
                "LEFT JOIN \n" +
                "    traversee t ON b.idBat = t.idBat\n" +
                "GROUP BY \n" +
                "    b.idBat, b.nomBat, b.Equipements\n" +
                "ORDER BY b.nomBat;";
        try (Connection connection = getConnection()) {
            System.out.println("Connexion à la base de données établie");
            try (Statement statement = connection.createStatement()) {
                System.out.println("Exécution de la requête: " + query);
                try (ResultSet resultSet = statement.executeQuery(query)) {
                    while (resultSet.next()) {                        String boatName = resultSet.getString("Nom du Bateau");
                        String categories = resultSet.getString("Catégories");
                        String capacities = resultSet.getString("Capacités");
                        int numTraversees = resultSet.getInt("Nombre de Traversées");
                        String imageUrl = resultSet.getString("Image");
                        
                        // Format: "Nom du Bateau - Catégories (Capacités), X traversée(s)"
                        String boat = boatName + " - " + categories + " (" + capacities + "), " + numTraversees + " traversée(s)";
                        System.out.println("Bateau trouvé: " + boat);
                        boats.add(boat);
                        
                        // Si une image est disponible, la télécharger
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            downloadBoatImage(boatName, imageUrl);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de la liste des bateaux: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        System.out.println("Nombre total de bateaux trouvés: " + boats.size());
        return boats;
    }
      public Map<Integer, Map<String, Object>> getDetailedBoatsInfo() throws Exception {
        Map<Integer, Map<String, Object>> boatsInfo = new HashMap<>();
        
        String query = "SELECT \n" +
                "    b.idBat, b.nomBat,\n" +
                "    c.lettre, c.libelleCat,\n" +
                "    co.capaciteMax,\n" +
                "    COUNT(DISTINCT t.numTra) AS nombreTraversees,\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT(p1.nomPort, ' -> ', p2.nomPort) SEPARATOR ', ') AS liaisons,\n" +
                "    b.Equipements\n" +
                "FROM \n" +
                "    bateau b\n" +
                "LEFT JOIN \n" +
                "    contenir co ON b.idBat = co.idBat\n" +
                "LEFT JOIN \n" +
                "    categorie c ON co.lettre = c.lettre\n" +
                "LEFT JOIN \n" +
                "    traversee t ON b.idBat = t.idBat\n" +
                "LEFT JOIN \n" +
                "    liaison l ON t.code = l.code\n" +
                "LEFT JOIN \n" +
                "    port p1 ON l.idPort_Depart = p1.idPort\n" +
                "LEFT JOIN \n" +
                "    port p2 ON l.idPort_Arrivee = p2.idPort\n" +
                "GROUP BY \n" +
                "    b.idBat, b.nomBat, c.lettre, c.libelleCat, co.capaciteMax, b.Equipements\n" +
                "ORDER BY b.nomBat;";
                
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, username, password)) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(query)) {
                    while (resultSet.next()) {
                        int idBat = resultSet.getInt("idBat");
                        Map<String, Object> boatInfo = new HashMap<>();
                          boatInfo.put("id", idBat);
                        boatInfo.put("nom", resultSet.getString("nomBat"));
                        boatInfo.put("categorieLetter", resultSet.getString("lettre"));
                        boatInfo.put("categorieLibelle", resultSet.getString("libelleCat"));
                        boatInfo.put("capaciteMax", resultSet.getInt("capaciteMax"));
                        boatInfo.put("nombreTraversees", resultSet.getInt("nombreTraversees"));
                        boatInfo.put("liaisons", resultSet.getString("liaisons"));
                        boatInfo.put("equipements", resultSet.getString("Equipements"));
                        
                        // Ajout des détails des traversées pour ce bateau
                        boatInfo.put("traversees", getBoatTraversees(idBat));
                        
                        boatsInfo.put(idBat, boatInfo);
                    }
                }
            }
        }
        return boatsInfo;
    }
    
    private ArrayList<Map<String, Object>> getBoatTraversees(int idBat) throws Exception {
        System.out.println("Récupération des traversées pour le bateau ID: " + idBat);
        ArrayList<Map<String, Object>> traversees = new ArrayList<>();
        
        String query = "SELECT \n" +
                "    t.numTra, t.date, t.heure,\n" +
                "    l.code, l.distance, l.tempsLiaison,\n" +
                "    p1.nomPort AS portDepart, p2.nomPort AS portArrivee,\n" +
                "    s.nomSecteur\n" +
                "FROM \n" +
                "    traversee t\n" +
                "JOIN \n" +
                "    liaison l ON t.code = l.code\n" +
                "JOIN \n" +
                "    port p1 ON l.idPort_Depart = p1.idPort\n" +
                "JOIN \n" +
                "    port p2 ON l.idPort_Arrivee = p2.idPort\n" +
                "JOIN \n" +
                "    secteur s ON l.idSecteur = s.idSecteur\n" +
                "WHERE \n" +
                "    t.idBat = ?\n" +
                "ORDER BY \n" +
                "    t.date, t.heure;";
                
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, username, password)) {
            System.out.println("Connexion à la base de données établie");
            try (java.sql.PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, idBat);
                System.out.println("Exécution de la requête: " + query.replace("?", String.valueOf(idBat)));
                
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Map<String, Object> traversee = new HashMap<>();
                        
                        traversee.put("numTra", resultSet.getInt("numTra"));
                        traversee.put("date", resultSet.getDate("date").toString());
                        traversee.put("heure", resultSet.getTime("heure").toString());
                        traversee.put("portDepart", resultSet.getString("portDepart"));
                        traversee.put("portArrivee", resultSet.getString("portArrivee"));
                        traversee.put("distance", resultSet.getFloat("distance"));
                        traversee.put("tempsLiaison", resultSet.getTime("tempsLiaison").toString());
                        traversee.put("secteur", resultSet.getString("nomSecteur"));
                        
                        System.out.println("Traversée trouvée: " + traversee.get("numTra") + " - " + 
                                           traversee.get("date") + " " + traversee.get("heure") + " - " +
                                           traversee.get("portDepart") + " -> " + traversee.get("portArrivee"));
                        
                        traversees.add(traversee);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des traversées: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        System.out.println("Nombre total de traversées trouvées: " + traversees.size());
        return traversees;
    }    public Map<String, Object> getBoatInfoByName(String boatName) throws Exception {
        System.out.println("Recherche du bateau: " + boatName);
        Map<String, Object> boatInfo = new HashMap<>();
        
        String query = "SELECT \n" +
                "    b.idBat, b.nomBat,\n" +
                "    c.lettre, c.libelleCat,\n" +
                "    co.capaciteMax,\n" +
                "    b.lienImage,\n" +
                "    b.Equipements\n" +
                "FROM \n" +
                "    bateau b\n" +
                "LEFT JOIN \n" +
                "    contenir co ON b.idBat = co.idBat\n" +
                "LEFT JOIN \n" +
                "    categorie c ON co.lettre = c.lettre\n" +
                "WHERE \n" +
                "    b.nomBat = ?\n" +
                "LIMIT 1;";
                
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, username, password)) {
            System.out.println("Connexion à la base de données établie");
            try (java.sql.PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, boatName);
                System.out.println("Exécution de la requête: " + query.replace("?", "'" + boatName + "'"));
                
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int idBat = resultSet.getInt("idBat");
                        System.out.println("Bateau trouvé avec l'ID: " + idBat);                        boatInfo.put("id", idBat);
                        boatInfo.put("nom", resultSet.getString("nomBat"));
                        boatInfo.put("categorieLetter", resultSet.getString("lettre"));
                        boatInfo.put("categorieLibelle", resultSet.getString("libelleCat"));
                        boatInfo.put("capaciteMax", resultSet.getInt("capaciteMax"));
                        boatInfo.put("lienImage", resultSet.getString("lienImage"));
                        boatInfo.put("equipements", resultSet.getString("Equipements"));
                        
                        System.out.println("Récupération des traversées pour le bateau ID: " + idBat);
                        ArrayList<Map<String, Object>> traversees = getBoatTraversees(idBat);
                        System.out.println("Nombre de traversées trouvées: " + traversees.size());
                        boatInfo.put("traversees", traversees);
                    } else {
                        System.out.println("Aucun bateau trouvé avec le nom: " + boatName);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des informations du bateau: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return boatInfo;
    }

    private void downloadBoatImage(String boatName, String imageUrl) {
        try {
            // Créer le dossier images s'il n'existe pas
            File imageDir = new File("images");
            if (!imageDir.exists()) {
                imageDir.mkdir();
            }
            
            // Télécharger l'image
            URL url = new URL(imageUrl);
            String fileName = "images/" + boatName.replaceAll("[^a-zA-Z0-9]", "_") + ".jpg";
            try (InputStream in = url.openStream();
                 OutputStream out = new FileOutputStream(fileName)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            System.out.println("Image téléchargée pour le bateau: " + boatName);
        } catch (Exception e) {
            System.err.println("Erreur lors du téléchargement de l'image pour le bateau " + boatName + ": " + e.getMessage());
        }
    }
    
    /**
     * Met à jour les informations d'un bateau dans la base de données
     * @param idBat l'identifiant du bateau
     * @param nomBat le nouveau nom du bateau
     * @param capaciteMax la nouvelle capacité maximale
     * @param equipements les nouveaux équipements
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateBoatInfo(int idBat, String nomBat, int capaciteMax, String equipements) throws Exception {
        System.out.println("Mise à jour des informations pour le bateau ID: " + idBat);
        boolean success = false;
        
        // Requête pour mettre à jour le nom et les équipements du bateau
        String updateBateau = "UPDATE bateau SET nomBat = ?, Equipements = ? WHERE idBat = ?";
        
        // Requête pour mettre à jour la capacité
        String updateContenir = "UPDATE contenir SET capaciteMax = ? WHERE idBat = ?";
        
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, username, password);
            connection.setAutoCommit(false);  // Commencer une transaction
            
            // Mettre à jour le nom et les équipements
            try (java.sql.PreparedStatement psBateau = connection.prepareStatement(updateBateau)) {
                psBateau.setString(1, nomBat);
                psBateau.setString(2, equipements);
                psBateau.setInt(3, idBat);
                
                int rowsUpdatedBateau = psBateau.executeUpdate();
                System.out.println("Bateau mis à jour, lignes affectées: " + rowsUpdatedBateau);
            }
            
            // Mettre à jour la capacité
            try (java.sql.PreparedStatement psContenir = connection.prepareStatement(updateContenir)) {
                psContenir.setInt(1, capaciteMax);
                psContenir.setInt(2, idBat);
                
                int rowsUpdatedContenir = psContenir.executeUpdate();
                System.out.println("Capacité mise à jour, lignes affectées: " + rowsUpdatedContenir);
            }
            
            connection.commit();  // Valider la transaction
            success = true;
            System.out.println("Mise à jour des informations du bateau réussie");
            
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();  // Annuler la transaction en cas d'erreur
                } catch (SQLException se) {
                    System.err.println("Erreur lors du rollback: " + se.getMessage());
                }
            }
            System.err.println("Erreur lors de la mise à jour des informations du bateau: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException se) {
                    System.err.println("Erreur lors de la fermeture de la connexion: " + se.getMessage());
                }
            }
        }
        
        return success;
    }
}