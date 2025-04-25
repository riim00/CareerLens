package ML.preprocessing;

import java.sql.*;
import java.io.*;
import java.util.*;

public class TextCleaner {

    // Informations de connexion MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/careerlens";
    private static final String USER = "root";
    private static final String PASSWORD = "7891230456.";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Connexion réussie à la base de données !");

            // Requête SQL pour récupérer les données filtrées
            String query = "SELECT * FROM job_offers WHERE site_name IN ('https://www.emploi.ma/', 'https://www.wetech.ma/', 'https://www.rekrute.com/')";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Préparer le fichier CSV pour les données nettoyées
            FileWriter csvWriter = new FileWriter("job_offers_cleaned.csv");
            csvWriter.append("id,titre,site_name,date_publication,nom_entreprise,description_poste,region,ville,secteur_activite,metier,type_contrat,niveau_etudes,experience,profil_recherche,hard_skills,soft_skills,competences_recommandees,langue,salaire,teletravail\n");

            // Préparer les documents pour la vectorisation TF-IDF
            List<String> tfidfDocuments = new ArrayList<>();

            while (rs.next()) {
                // Nettoyer les colonnes nécessaires
                int id = rs.getInt("id");
                String titre = cleanText(rs.getString("titre"));
                String siteName = rs.getString("site_name");
                String datePublication = rs.getString("date_publication");
                String nomEntreprise = cleanText(rs.getString("nom_entreprise"));
                String descriptionPoste = cleanText(rs.getString("description_poste"));
                String region = cleanText(rs.getString("region"));
                String ville = cleanText(rs.getString("ville"));
                String secteurActivite = cleanText(rs.getString("secteur_activite"));
                String metier = cleanText(rs.getString("metier"));
                String typeContrat = cleanText(rs.getString("type_contrat"));
                String niveauEtudes = cleanText(rs.getString("niveau_etudes"));
                String experience = cleanText(rs.getString("experience"));
                String profilRecherche = cleanText(rs.getString("profil_recherche"));
                String hardSkills = cleanText(rs.getString("hard_skills"));
                String softSkills = cleanText(rs.getString("soft_skills"));
                String competencesRecommandees = cleanText(rs.getString("competences_recommandees"));
                String langue = cleanText(rs.getString("langue"));
                String salaire = cleanText(rs.getString("salaire"));
                int teletravail = rs.getInt("teletravail");

                // Ajouter au CSV nettoyé
                csvWriter.append(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d\n",
                        id, titre, siteName, datePublication, nomEntreprise, descriptionPoste, region, ville, secteurActivite, metier, typeContrat,
                        niveauEtudes, experience, profilRecherche, hardSkills, softSkills, competencesRecommandees, langue, salaire, teletravail));

                // Ajouter pour la vectorisation TF-IDF
                tfidfDocuments.add(descriptionPoste + " " + profilRecherche + " " + hardSkills + " " + softSkills + " " + competencesRecommandees);
            }

            csvWriter.flush();
            csvWriter.close();
            rs.close();
            stmt.close();

            // Appliquer la vectorisation TF-IDF
            TFIDFVectorizer tfidfVectorizer = new TFIDFVectorizer();
            for (String doc : tfidfDocuments) {
                tfidfVectorizer.addDocument(doc);
            }
            tfidfVectorizer.fitTransform();
            tfidfVectorizer.saveVectors("job_offers_tfidf.csv");

            System.out.println("Export des données nettoyées et vectorisées terminé avec succès dans job_offers_cleaned.csv et job_offers_tfidf.csv");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode pour nettoyer le texte
    public static String cleanText(String text) {
        if (text == null || text.isEmpty()) return "Non spécifié";
        text = text.toLowerCase(); // Convertir tout en minuscule
        text = text.replaceAll("\\n", " ").replaceAll("\\r", " "); // Remplacer les sauts de ligne par des espaces
        text = text.replaceAll("[^a-zA-Z0-9éèàçùôêâîïûö\\s]", ""); // Supprimer les caractères spéciaux
        text = text.trim(); // Supprimer les espaces en début et fin
        return text;
    }
}
