package fr.marieteam.pdf.marieteampdf.bdd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class MarieTeamAPI {
    public ArrayList<String> getAllBoats() throws Exception {
        ArrayList<String> boats = new ArrayList<>();
        String hostname = "localhost";
        String port = "3306";
        String database = "marieteam";
        String username = "root";
        String password = "";
        String query = "SELECT \n" +
                "    b.nomBat AS 'Nom du Bateau',\n" +
                "    c.libelleCat AS 'Catégorie',\n" +
                "    MAX(co.capaciteMax) AS 'Capacité',\n" +
                "    COUNT(t.numTra) AS 'Nombre de Traversées'\n" +
                "FROM \n" +
                "    bateau b\n" +
                "LEFT JOIN \n" +
                "    contenir co ON b.idBat = co.idBat\n" +
                "LEFT JOIN \n" +
                "    categorie c ON co.lettre = c.lettre\n" +
                "LEFT JOIN \n" +
                "    traversee t ON b.idBat = t.idBat\n" +
                "GROUP BY \n" +
                "    b.nomBat, c.libelleCat;";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, username, password)) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(query)) {
                    while (resultSet.next()) {
                        String boat = resultSet.getString("Nom du Bateau") + " - " + resultSet.getString("Catégorie") + " (" + resultSet.getString("Capacité") + " autorisés), " + resultSet.getString("Nombre de Traversées") + " traversees)";
                        boats.add(boat);
                    }
                }
            }
        }
        return boats;
    }
}