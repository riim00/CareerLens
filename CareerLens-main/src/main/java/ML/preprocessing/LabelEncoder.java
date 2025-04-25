package ML.preprocessing;

import java.io.*;
import java.util.*;

public class LabelEncoder {

    private Map<String, Integer> labelToIndex = new HashMap<>();
    private Map<Integer, String> indexToLabel = new HashMap<>();

    // Constructeur pour initialiser les labels
    public LabelEncoder(String[] labels) {
        for (int i = 0; i < labels.length; i++) {
            labelToIndex.put(labels[i], i);
            indexToLabel.put(i, labels[i]);
        }
    }

    // Encoder
    public int encode(String label) {
        if (label == null || label.trim().isEmpty()) {
            label = "Non spécifié";
        }
        return labelToIndex.getOrDefault(label, -1);
    }

    // Décoder
    public String decode(int index) {
        return indexToLabel.getOrDefault(index, "Non spécifié");
    }

    public static void main(String[] args) {
        try {
            // Charger le fichier CSV
            String csvPath = "job_offers_cleaned.csv";
            BufferedReader br = new BufferedReader(new FileReader(csvPath));
            String header = br.readLine(); // Lire l'en-tête
            String[] columns = header.split(",");

            // Lire les données
            List<String[]> rows = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                rows.add(line.split(","));
            }
            br.close();

            // Définir les catégories pour encodage
            String[] contrats = {"CDI", "CDD", "Stage", "Freelance", "Non spécifié"};
            String[] langues = {"français", "anglais", "arabe", "Non spécifié"};

            LabelEncoder contratEncoder = new LabelEncoder(contrats);
            LabelEncoder langueEncoder = new LabelEncoder(langues);

            // Créer un fichier encodé
            FileWriter csvWriter = new FileWriter("job_offers_encoded.csv");
            csvWriter.append(header + ",type_contrat_encoded,langue_encoded\n");

            // Initialiser TF-IDF Vectorizer
            TFIDFVectorizer tfidfVectorizer = new TFIDFVectorizer();

            for (String[] row : rows) {
                int contratCode = contratEncoder.encode(row[10]); // type_contrat
                int langueCode = langueEncoder.encode(row[17]);   // langue

                // Ajouter au vectoriseur TF-IDF
                String textContent = row[5] + " " + row[13] + " " + row[14] + " " + row[15] + " " + row[16];
                tfidfVectorizer.addDocument(textContent);

                // Ajouter les colonnes encodées
                StringBuilder sb = new StringBuilder(String.join(",", row));
                sb.append(",").append(contratCode).append(",").append(langueCode);

                csvWriter.append(sb.toString()).append("\n");
            }

            // Sauvegarder les vecteurs TF-IDF
            tfidfVectorizer.fitTransform();
            tfidfVectorizer.saveVectors("job_offers_tfidf.csv");

            csvWriter.flush();
            csvWriter.close();

            System.out.println("Encodage et vectorisation TF-IDF terminés dans job_offers_encoded.csv et job_offers_tfidf.csv");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
