package fr.marieteam.pdf.marieteampdf;

import fr.marieteam.pdf.marieteampdf.api.MarieTeamAPI;

public class Program {
    public static void main(String[] args) throws Exception {
        MarieTeamAPI db = new MarieTeamAPI();
        System.out.println(db.getAllBoats());
    }
}