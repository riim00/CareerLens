import java.sql.*;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/careerlens";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public Connection getConnection() {
        Connection conn = null;
        try {
            // Chargement explicite du driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connexion à la base de données
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion réussie !");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver MySQL introuvable !");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Erreur SQL !");
            e.printStackTrace();
        }
        return conn;
    }

    // Méthode pour établir une connexion à la base de données
    public Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Méthode pour vérifier si une offre existe déjà
    private boolean doesJobExist(String titre) {
        String query = "SELECT COUNT(*) FROM job_offers WHERE titre = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, titre);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0; // Retourne true si l'offre existe déjà
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'existence de l'offre : " + e.getMessage());
        }
        return false;
    }

    // Méthode pour insérer une offre d'emploi dans la table job_offers
    public void insertJob(String titre, String url, String siteName, String datePublication, String datePostuler,
                          String adresseEntreprise, String siteWebEntreprise, String nomEntreprise, String descriptionEntreprise,
                          String descriptionPoste, String region, String ville, String secteurActivite, String metier,
                          String typeContrat, String niveauEtudes, String specialiteDiplome, String experience,
                          String profilRecherche, String traitsPersonnalite, String hardSkills, String softSkills,
                          String competencesRecommandees, String langue, String niveauLangue, String salaire,
                          String avantagesSociaux, Boolean teletravail) {

        // Vérifier si l'offre existe déjà
        if (doesJobExist(titre)) {
            System.out.println("L'offre existe déjà dans la base de données. Insertion ignorée.");
            return;
        }

        String query = "INSERT INTO job_offers (titre, url, site_name, date_publication, date_postuler, adresse_entreprise, " +
                "site_web_entreprise, nom_entreprise, description_entreprise, description_poste, region, ville, secteur_activite, " +
                "metier, type_contrat, niveau_etudes, specialite_diplome, experience, profil_recherche, traits_personnalite, " +
                "hard_skills, soft_skills, competences_recommandees, langue, niveau_langue, salaire, avantages_sociaux, teletravail) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Assigner les valeurs aux paramètres de la requête
            statement.setString(1, titre);
            statement.setString(2, url);
            statement.setString(3, siteName);
            statement.setString(4, datePublication);
            statement.setString(5, datePostuler);
            statement.setString(6, adresseEntreprise);
            statement.setString(7, siteWebEntreprise);
            statement.setString(8, nomEntreprise);
            statement.setString(9, descriptionEntreprise);
            statement.setString(10, descriptionPoste);
            statement.setString(11, region);
            statement.setString(12, ville);
            statement.setString(13, secteurActivite);
            statement.setString(14, metier);
            statement.setString(15, typeContrat);
            statement.setString(16, niveauEtudes);
            statement.setString(17, specialiteDiplome);
            statement.setString(18, experience);
            statement.setString(19, profilRecherche);
            statement.setString(20, traitsPersonnalite);
            statement.setString(21, hardSkills);
            statement.setString(22, softSkills);
            statement.setString(23, competencesRecommandees);
            statement.setString(24, langue);
            statement.setString(25, niveauLangue);
            statement.setString(26, salaire);
            statement.setString(27, avantagesSociaux);
            statement.setBoolean(28, (teletravail != null) ? teletravail : false);

            // Exécuter la requête
            statement.executeUpdate();
            System.out.println("Offre d'emploi insérée avec succès dans la table job_offers !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion en base de données : " + e.getMessage());
        }
    }

    public void cleanData() {
        try (Connection connection = connect()) {
            // Specify the table name and fields to check for emptiness
            String tableName = "job_offers"; // Replace with your actual table name
            String[] fieldsToCheck = {
                    "titre", "url", "site_name", "date_publication", "date_postuler",
                    "adresse_entreprise", "site_web_entreprise", "nom_entreprise",
                    "description_entreprise", "description_poste", "region", "ville",
                    "secteur_activite", "metier", "type_contrat", "niveau_etudes",
                    "specialite_diplome", "experience", "profil_recherche",
                    "traits_personnalite", "hard_skills", "soft_skills",
                    "competences_recommandees", "langue", "niveau_langue",
                    "salaire", "avantages_sociaux", "teletravail"
            };

            // Build and execute the DELETE query
            String deleteQuery = buildDeleteQuery(tableName, fieldsToCheck);
            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
                int rowsDeleted = preparedStatement.executeUpdate();
                System.out.println(rowsDeleted + " rows deleted.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du nettoyage des données : " + e.getMessage());
        }
    }

    private String buildDeleteQuery(String tableName, String[] fieldsToCheck) {
        StringBuilder queryBuilder = new StringBuilder("DELETE FROM " + tableName + " WHERE ");

        // Add conditions for each field
        for (String field : fieldsToCheck) {
            queryBuilder.append(field).append(" IS NULL OR ").append(field).append(" = '' OR ");
        }

        // Remove the trailing " OR "
        queryBuilder.delete(queryBuilder.length() - 4, queryBuilder.length());

        return queryBuilder.toString();
    }

    // Méthode pour récupérer les données sous forme de tableau
    public DefaultTableModel getJobsData() {
        DefaultTableModel model = new DefaultTableModel();
        String query = "SELECT * FROM job_offers";

        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            // Créer les colonnes à partir des métadonnées
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }

            // Ajouter les lignes
            while (resultSet.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(resultSet.getObject(i));
                }
                model.addRow(row);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des données : " + e.getMessage());
        }
        return model;
    }
}