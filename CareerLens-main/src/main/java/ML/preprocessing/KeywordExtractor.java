package ML.preprocessing;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class KeywordExtractor {

    // Liste de mots techniques (Hard Skills) spécifiques
    private static final Set<String> TECHNICAL_TERMS = new HashSet<>(Arrays.asList(
            "perl", "python", "flask", "django", "postgresql", "angular", "typescript",
            "java", "javascript", "html", "css", "react", "vue", "nodejs", "spring",
            "hibernate", "sql", "mysql", "oracle", "mongodb", "redis", "docker",
            "kubernetes", "jenkins", "git", "svn", "aws", "azure", "gcp",
            "tensorflow", "pytorch", "scikit-learn", "keras", "numpy", "pandas",
            "matplotlib", "seaborn", "spark", "hadoop", "kafka", "elastic",
            "c", "c++", "c#", "ruby", "go", "swift", "kotlin", "dart",
            "php", "bash", "shell", "linux", "unix", "windows", "macos",
            "api", "rest", "graphql", "soap", "json", "xml", "jwt",
            "scrum", "agile", "kanban", "jira", "trello", "confluence",
            "selenium", "cucumber", "junit", "testng", "postman",
            "firebase", "heroku", "netlify", "terraform", "ansible",
            "vscode", "intellij", "eclipse", "pycharm", "xcode",
            "opencv", "nltk", "spacy", "beautifulsoup", "scrapy",
            "restful", "microservices", "lambda", "express", "django-rest",
            "graphql", "apollo", "webpack", "babel", "grunt", "gulp"
    ));

    // Nettoyer et tokeniser une chaîne de texte
    public static List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();

        // Nettoyage du texte
        text = text.toLowerCase().replaceAll("[^a-z0-9éèàçùôêâîïûö\\s]", " "); // Supprimer caractères spéciaux
        text = text.replaceAll("\\s+", " "); // Réduire les espaces multiples
        text = text.trim(); // Supprimer espaces en début/fin

        // Diviser en mots (tokens) et filtrer les mots non techniques
        return Arrays.stream(text.split("\\s+"))
                .filter(word -> TECHNICAL_TERMS.contains(word)) // Ne garder que les termes techniques
                .collect(Collectors.toList());
    }

    // Extraction de mots-clés uniques avec dédoublonnage et tri
    public static Set<String> extractKeywords(List<String> tokens) {
        return new HashSet<>(tokens); // Retourne les mots uniques
    }

    public static void main(String[] args) {
        try {
            // Lire le fichier nettoyé
            String inputCsv = "job_offers_cleaned.csv";
            BufferedReader br = new BufferedReader(new FileReader(inputCsv));

            // Préparer les fichiers de sortie
            FileWriter csvWriterTFIDF = new FileWriter("job_offers_tfidf_keywords.csv");
            FileWriter csvWriterKeywords = new FileWriter("job_offers_keywords.csv");

            csvWriterTFIDF.append("id,hard_skills_keywords,tfidf_keywords\n");
            csvWriterKeywords.append("id,hard_skills,soft_skills,competences_recommandees\n");

            // Stocker les documents pour la vectorisation TF-IDF
            List<String> documents = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();

            String header = br.readLine(); // Lire l'en-tête
            String line;

            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");

                // Lire les colonnes nécessaires
                int id = Integer.parseInt(columns[0]); // ID
                String hardSkills = columns[14];       // Hard skills
                String softSkills = columns[15];       // Soft skills
                String recommendedSkills = columns[16]; // Compétences recommandées

                // Tokenization et extraction de mots-clés pour hard skills
                List<String> hardSkillTokens = tokenize(hardSkills);
                Set<String> hardKeywords = extractKeywords(hardSkillTokens);

                // Stocker les documents pour la vectorisation TF-IDF
                documents.add(String.join(" ", hardSkillTokens));
                ids.add(id);

                // Ajouter dans le fichier job_offers_keywords.csv
                csvWriterKeywords.append(String.format("%d,%s,%s,%s\n",
                        id,
                        hardSkills,
                        softSkills,
                        recommendedSkills));

                // Ajouter les mots-clés extraits
                csvWriterTFIDF.append(String.format("%d,%s,",
                        id, String.join(" ", hardKeywords)));
            }

            br.close();

            // Calculer les scores TF-IDF
            Map<String, Double> idfScores = computeIDF(documents);
            for (int i = 0; i < documents.size(); i++) {
                String tfidf = computeTFIDF(documents.get(i), idfScores);
                csvWriterTFIDF.append(tfidf).append("\n");
            }

            csvWriterTFIDF.flush();
            csvWriterTFIDF.close();
            csvWriterKeywords.flush();
            csvWriterKeywords.close();

            System.out.println("Extraction des mots-clés et TF-IDF terminée.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Calculer l'IDF pour tous les mots
    private static Map<String, Double> computeIDF(List<String> documents) {
        Map<String, Integer> docFreq = new HashMap<>();
        int totalDocs = documents.size();

        for (String doc : documents) {
            Set<String> uniqueWords = new HashSet<>(Arrays.asList(doc.split(" ")));
            for (String word : uniqueWords) {
                docFreq.put(word, docFreq.getOrDefault(word, 0) + 1);
            }
        }

        Map<String, Double> idfScores = new HashMap<>();
        for (String word : docFreq.keySet()) {
            idfScores.put(word, Math.log((double) totalDocs / (1 + docFreq.get(word))));
        }
        return idfScores;
    }

    // Calculer le TF-IDF d'un document donné
    private static String computeTFIDF(String document, Map<String, Double> idfScores) {
        Map<String, Integer> termFreq = new HashMap<>();
        String[] words = document.split(" ");
        for (String word : words) {
            termFreq.put(word, termFreq.getOrDefault(word, 0) + 1);
        }

        Map<String, Double> tfidf = new HashMap<>();
        for (String word : termFreq.keySet()) {
            double tf = (double) termFreq.get(word) / words.length;
            double idf = idfScores.getOrDefault(word, 0.0);
            tfidf.put(word, tf * idf);
        }

        // Construire la chaîne TF-IDF formatée
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> entry : tfidf.entrySet()) {
            sb.append(entry.getKey()).append(":" ).append(String.format("%.5f", entry.getValue())).append(";");
        }
        return sb.toString();
    }
}
