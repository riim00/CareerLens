package ML.preprocessing;

import java.io.*;
import java.util.*;

public class TFIDFVectorizer {

    private List<String[]> documents = new ArrayList<>();
    private Map<String, Double> idfScores = new HashMap<>();
    private List<Map<String, Double>> tfidfVectors = new ArrayList<>();

    // Ajouter un document (ligne CSV)
    public void addDocument(String text) {
        documents.add(preprocess(text));
    }

    // Prétraiter le texte
    private String[] preprocess(String text) {
        if (text == null || text.trim().isEmpty()) return new String[]{};
        text = text.toLowerCase().replaceAll("[^a-zA-Z0-9éèàçùôêâîïûö\\s]", ""); // Nettoyer
        return text.split("\\s+"); // Tokeniser par espace
    }

    // Calculer le TF-IDF pour tous les documents
    public void fitTransform() {
        // Calculer l'IDF
        computeIDF();

        // Calculer le TF-IDF
        for (String[] doc : documents) {
            Map<String, Double> tfidf = new HashMap<>();
            Map<String, Integer> termFreq = new HashMap<>();

            // Calculer TF
            for (String word : doc) {
                termFreq.put(word, termFreq.getOrDefault(word, 0) + 1);
            }

            for (String word : termFreq.keySet()) {
                double tf = (double) termFreq.get(word) / doc.length;
                double idf = idfScores.getOrDefault(word, 0.0);
                tfidf.put(word, tf * idf);
            }
            tfidfVectors.add(tfidf);
        }
    }

    // Calculer l'IDF
    private void computeIDF() {
        Map<String, Integer> docFreq = new HashMap<>();
        int totalDocs = documents.size();

        for (String[] doc : documents) {
            Set<String> uniqueWords = new HashSet<>(Arrays.asList(doc));
            for (String word : uniqueWords) {
                docFreq.put(word, docFreq.getOrDefault(word, 0) + 1);
            }
        }

        for (String word : docFreq.keySet()) {
            idfScores.put(word, Math.log((double) totalDocs / (1 + docFreq.get(word))));
        }
    }

    // Récupérer un vecteur TF-IDF sous forme de chaîne pour CSV
    public String getVectorAsString(int index) {
        if (index < 0 || index >= tfidfVectors.size()) return "Non spécifié";
        Map<String, Double> vector = tfidfVectors.get(index);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> entry : vector.entrySet()) {
            sb.append(entry.getKey()).append(":").append(String.format("%.5f", entry.getValue())).append(";");
        }
        return sb.toString();
    }


    // Enregistrer dans un fichier CSV
    public void saveVectors(String fileName) throws IOException {
        FileWriter csvWriter = new FileWriter(fileName);
        for (Map<String, Double> vector : tfidfVectors) {
            for (String key : vector.keySet()) {
                csvWriter.append(key).append(":").append(String.format("%.5f", vector.get(key))).append(",");
            }
            csvWriter.append("\n");
        }
        csvWriter.flush();
        csvWriter.close();
    }
}
