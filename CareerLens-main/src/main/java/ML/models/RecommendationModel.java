package ML.models;

import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


public class RecommendationModel {

    private static final Logger LOGGER = Logger.getLogger(RecommendationModel.class.getName());
    private Instances data; // Ensemble de données
    private JFrame frame;
    private JTextField profileField;
    private JLabel resultLabel;
    private JLabel predictionResultLabel; // Résultat de la prédiction
    private NaiveBayes model; // Modèle NaiveBayes ML

    // Mots à ignorer
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "français", "anglais", "espagnol", "allemand", // Langues
            "le", "la", "les", "de", "des", "du", "et", "à", "au", "avec", "pour" // Articles et prépositions
    ));

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new RecommendationModel().createAndShowGUI();
            } catch (Exception e) {
                LOGGER.severe("Erreur au démarrage : " + e.getMessage());
            }
        });
    }

    // Création de l'interface graphique
    public void createAndShowGUI() throws Exception {
        // Charger les données
        loadData("job_offers_cleaned_ready.csv");
        trainModel(); // Entraîner le modèle ML

        // Créer la fenêtre principale
        frame = new JFrame("CareerLens - Recommandations");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(30, 30, 30)); // Fond sombre
        frame.setLayout(null);

        // Titre principal
        JLabel titleLabel = new JLabel("GET RECOMMENDATIONS.");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(100, 169, 255)); // Bleu clair
        titleLabel.setBounds(200, 20, 400, 30);
        frame.add(titleLabel);

        // Étiquette "PROFIL"
        JLabel profileLabel = new JLabel("PROFIL :");
        profileLabel.setFont(new Font("Arial", Font.BOLD, 14));
        profileLabel.setForeground(Color.WHITE);
        profileLabel.setBounds(50, 100, 150, 30);
        frame.add(profileLabel);

        // Champ de saisie des profils
        profileField = new JTextField();
        profileField.setBounds(180, 100, 400, 30);
        profileField.setFont(new Font("Arial", Font.PLAIN, 14));
        profileField.setBackground(Color.DARK_GRAY);
        profileField.setForeground(Color.WHITE);
        profileField.setCaretColor(Color.WHITE);
        profileField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        frame.add(profileField);

        // Bouton DÉMARRER
        JButton startButton = new JButton("START");
        startButton.setBounds(600, 100, 120, 30);
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.setBackground(new Color(100, 169, 255)); // Bleu clair
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        frame.add(startButton);

        // Bouton PREDIR
        JButton predictButton = new JButton("PREDICT");
        predictButton.setBounds(600, 150, 120, 30);
        predictButton.setFont(new Font("Arial", Font.BOLD, 14));
        predictButton.setBackground(new Color(255, 123, 0)); // Orange
        predictButton.setForeground(Color.WHITE);
        predictButton.setFocusPainted(false);
        frame.add(predictButton);

        // Résultat de la prédiction
        JLabel predictionLabel = new JLabel(" EXPERINCE :");
        predictionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        predictionLabel.setForeground(Color.WHITE);
        predictionLabel.setBounds(50, 300, 700, 30);
        frame.add(predictionLabel);

        predictionResultLabel = new JLabel("", SwingConstants.CENTER);
        predictionResultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        predictionResultLabel.setForeground(Color.WHITE);
        predictionResultLabel.setBounds(50, 340, 700, 40);
        frame.add(predictionResultLabel);

        // Étiquette "VOUS DEVEZ APPRENDRE"
        JLabel learnLabel = new JLabel("REQUIRED SKILLS :");
        learnLabel.setFont(new Font("Arial", Font.BOLD, 14));
        learnLabel.setForeground(Color.WHITE);
        learnLabel.setBounds(50, 180, 700, 30);
        frame.add(learnLabel);

        // Résultat stylisé
        resultLabel = new JLabel("", SwingConstants.CENTER); // Centré
        resultLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Texte en gras et plus grand
        resultLabel.setForeground(Color.WHITE); // Texte en blanc
        resultLabel.setBounds(50, 230, 700, 40);
        frame.add(resultLabel);

        // Actions des boutons
        startButton.addActionListener(e -> {
            try {
                generateRecommendations();
            } catch (Exception ex) {
                LOGGER.severe("Error during recommendations : " + ex.getMessage());
            }
        });

        predictButton.addActionListener(e -> {
            try {
                String predictedExperience = predictExperienceWithSimilarity(profileField.getText().trim());
                predictionResultLabel.setText(predictedExperience); // Afficher la prédiction
            } catch (Exception ex) {
                LOGGER.severe("Error during prediction : " + ex.getMessage());
            }
        });

        frame.setVisible(true);
    }

    // Charger les données
    private void loadData(String filePath) throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(filePath));
        data = loader.getDataSet();

        if (data.attribute(data.numAttributes() - 1).isNumeric()) {
            NumericToNominal filter = new NumericToNominal();
            filter.setInputFormat(data);
            data = Filter.useFilter(data, filter);
        }

        data.setClassIndex(data.attribute("experience").index()); // Colonne expérience
    }

    // Entraîner le modèle
    private void trainModel() throws Exception {
        model = new NaiveBayes();
        model.buildClassifier(data);
    }

    // Prédiction basée sur similarité
    private String predictExperienceWithSimilarity(String input) {
        int maxMatch = 0;
        String predictedExperience = "0-1 ans"; // Valeur par défaut
        int titleIndex = data.attribute("titre").index();
        int experienceIndex = data.attribute("experience").index();

        for (int i = 0; i < data.numInstances(); i++) {
            String title = data.instance(i).stringValue(titleIndex).toLowerCase();
            int match = calculateMatch(input, title);

            if (match > maxMatch) {
                maxMatch = match;
                predictedExperience = data.instance(i).stringValue(experienceIndex);
            }
        }
        return predictedExperience;
    }

    // Calculer les correspondances
    private int calculateMatch(String input, String title) {
        Set<String> inputWords = new HashSet<>(Arrays.asList(input.split(" ")));
        Set<String> titleWords = new HashSet<>(Arrays.asList(title.split(" ")));
        inputWords.retainAll(titleWords);
        return inputWords.size(); // Nombre de mots correspondants
    }



    // Générer les recommandations
    private void generateRecommendations() throws Exception {
        String inputTitle = profileField.getText().trim().toLowerCase(); // Utiliser profileField


        // Préparer la fréquence des compétences
        Map<String, Integer> wordCount = new HashMap<>();
        int titleIndex = data.attribute("titre").index();
        int hardSkillsIndex = data.attribute("hard_skills").index();

        for (int i = 0; i < data.numInstances(); i++) {
            String title = data.instance(i).stringValue(titleIndex).toLowerCase();
            String hardSkills = data.instance(i).stringValue(hardSkillsIndex).toLowerCase();

            double similarity = calculateSimilarity(inputTitle, title);
            if (similarity > 0.2) { // Seuil ajustable
                for (String skill : hardSkills.split("[, ]+")) {
                    skill = skill.trim();
                    if (!skill.isEmpty() && !STOPWORDS.contains(skill)) {
                        wordCount.put(skill, wordCount.getOrDefault(skill, 0) + 1);
                    }
                }
            }
        }

        // Trier et afficher les recommandations
        List<Map.Entry<String, Integer>> sortedWords = new ArrayList<>(wordCount.entrySet());
        sortedWords.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        StringBuilder result = new StringBuilder("<html>");
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedWords) {
            if (count++ == 3) break;
            result.append("<span style='color:#64A9FF; font-weight:bold;'>")
                    .append(entry.getKey()).append("</span>");
            if (count < 3) result.append(", ");
        }
        result.append("</html>");
        resultLabel.setText(result.toString());
    }

    // Calculer la similarité
    private double calculateSimilarity(String input, String title) {
        String[] inputWords = input.split(" ");
        String[] titleWords = title.split(" ");
        int matchCount = 0;
        for (String word : inputWords) {
            for (String titleWord : titleWords) {
                if (titleWord.contains(word)) matchCount++;
            }
        }
        return (double) matchCount / Math.max(inputWords.length, titleWords.length);
    }
}
