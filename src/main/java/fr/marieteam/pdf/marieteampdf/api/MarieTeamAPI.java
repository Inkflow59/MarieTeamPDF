package fr.marieteam.pdf.marieteampdf.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MarieTeamAPI {
    private String hostname = "localhost";
    private String port = "3306";
    private String database = "marieteam";
    private String username = "root";
    private String password = "";
    
    public ArrayList<String> getAllBoats() throws Exception {
        System.out.println("Récupération de la liste des bateaux");
        ArrayList<String> boats = new ArrayList<>();
        String query = "SELECT \n" +
                "    b.idBat, b.nomBat AS 'Nom du Bateau',\n" +
                "    c.lettre, c.libelleCat AS 'Catégorie',\n" +
                "    co.capaciteMax AS 'Capacité',\n" +
                "    COUNT(DISTINCT t.numTra) AS 'Nombre de Traversées'\n" +
                "FROM \n" +
                "    bateau b\n" +
                "LEFT JOIN \n" +
                "    contenir co ON b.idBat = co.idBat\n" +
                "LEFT JOIN \n" +
                "    categorie c ON co.lettre = c.lettre\n" +
                "LEFT JOIN \n" +
                "    traversee t ON b.idBat = t.idBat\n" +
                "GROUP BY \n" +
                "    b.idBat, b.nomBat, c.lettre, c.libelleCat, co.capaciteMax\n" +
                "ORDER BY b.nomBat;";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, username, password)) {
            System.out.println("Connexion à la base de données établie");
            try (Statement statement = connection.createStatement()) {
                System.out.println("Exécution de la requête: " + query);
                try (ResultSet resultSet = statement.executeQuery(query)) {
                    while (resultSet.next()) {
                        String boatName = resultSet.getString("Nom du Bateau");
                        String category = resultSet.getString("Catégorie");
                        int capacity = resultSet.getInt("Capacité");
                        int numTraversees = resultSet.getInt("Nombre de Traversées");
                        
                        // Format: "Nom du Bateau - Catégorie (Capacité autorisés), X traversée(s)"
                        String boat = boatName + " - " + category + " (" + capacity + " autorisés), " + numTraversees + " traversée(s)";
                        System.out.println("Bateau trouvé: " + boat);
                        boats.add(boat);
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
                "    GROUP_CONCAT(DISTINCT CONCAT(p1.nomPort, ' -> ', p2.nomPort) SEPARATOR ', ') AS liaisons\n" +
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
                "    b.idBat, b.nomBat, c.lettre, c.libelleCat, co.capaciteMax\n" +
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
    }

    public Map<String, Object> getBoatInfoByName(String boatName) throws Exception {
        System.out.println("Recherche du bateau: " + boatName);
        Map<String, Object> boatInfo = new HashMap<>();
        
        String query = "SELECT \n" +
                "    b.idBat, b.nomBat,\n" +
                "    c.lettre, c.libelleCat,\n" +
                "    co.capaciteMax\n" +
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
                        System.out.println("Bateau trouvé avec l'ID: " + idBat);
                        boatInfo.put("id", idBat);
                        boatInfo.put("nom", resultSet.getString("nomBat"));
                        boatInfo.put("categorieLetter", resultSet.getString("lettre"));
                        boatInfo.put("categorieLibelle", resultSet.getString("libelleCat"));
                        boatInfo.put("capaciteMax", resultSet.getInt("capaciteMax"));
                        
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
}