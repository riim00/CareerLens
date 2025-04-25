package ML.preprocessing;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;


public class DataCleaner {

    public static void main(String[] args) {
        String inputFile = "C:\\Users\\pc\\Desktop\\table.csv";
        String outputFile = "cleaned_output.csv";

        try {
            cleanAndPrepareData(inputFile, outputFile);
            System.out.println("Fichier nettoyé et sauvegardé sous : " + outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cleanAndPrepareData(String inputFile, String outputFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        // Lire l'en-tête et écrire dans le fichier de sortie
        String header = reader.readLine();
        writer.write(header + "\n");

        // Index des colonnes importantes
        int titreIndex = -1;
        int skillsIndex = -1;
        int experienceIndex = -1;

        // Diviser les colonnes
        String[] headers = header.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equalsIgnoreCase("titre")) titreIndex = i;
            if (headers[i].equalsIgnoreCase("hard_skills")) skillsIndex = i;
            if (headers[i].equalsIgnoreCase("experience")) experienceIndex = i;
        }

        if (titreIndex == -1 || skillsIndex == -1 || experienceIndex == -1) {
            throw new IllegalArgumentException("Colonnes manquantes : titre, hard_skills ou experience");
        }

        // Lire et nettoyer les lignes
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(",", -1);

            // Nettoyer le champ 'titre'
            fields[titreIndex] = cleanText(fields[titreIndex]);

            // Nettoyer le champ 'hard_skills'
            fields[skillsIndex] = cleanSkills(fields[skillsIndex]);

            // Nettoyer et normaliser la colonne 'experience'
            fields[experienceIndex] = normalizeExperience(fields[experienceIndex]);

            // Réécrire la ligne nettoyée
            writer.write(String.join(",", fields) + "\n");
        }

        reader.close();
        writer.close();
    }

    // Nettoyer un texte (titre)
    private static String cleanText(String text) {
        if (text == null || text.isEmpty()) return "inconnu"; // Valeur par défaut
        return text.trim()
                .toLowerCase()
                .replaceAll("[^a-zA-Z0-9 ]", "") // Supprimer caractères spéciaux
                .replaceAll("\\s+", " "); // Remplacer espaces multiples par un seul
    }

    // Nettoyer et formater les compétences
    private static String cleanSkills(String skills) {
        if (skills == null || skills.isEmpty()) return "aucune_compétence"; // Valeur par défaut
        String[] skillList = skills.split(",|;|\\|"); // Diviser par différents séparateurs
        return Arrays.stream(skillList)
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(skill -> !skill.isEmpty()) // Supprimer les vides
                .distinct() // Supprimer les doublons
                .collect(Collectors.joining(", ")); // Réassembler
    }

    // Normaliser l'expérience
    private static String normalizeExperience(String experience) {
        if (experience == null || experience.trim().isEmpty()) return "0-1 ans"; // Par défaut pour débutant

        experience = experience.toLowerCase().replaceAll("[^0-9a-zàâçéèêëîïôûùüÿñæœ ]", ""); // Nettoyer

        // Traitement des cas spécifiques
        if (experience.contains("débutant")) return "0-1 ans";
        if (experience.contains("confirmé")) return "3-5 ans";

        // Rechercher des valeurs numériques
        Pattern pattern = Pattern.compile("(\\d+)[ -]?ans");
        Matcher matcher = pattern.matcher(experience);

        List<Integer> values = new ArrayList<>();
        while (matcher.find()) {
            values.add(Integer.parseInt(matcher.group(1)));
        }

        // Définir la catégorie d'expérience
        if (values.isEmpty()) return "1-3 ans"; // Pas d'information
        if (values.size() == 1) {
            int val = values.get(0);
            if (val <= 1) return "0-1 ans";
            if (val <= 2) return "1-2 ans";
            if (val <= 5) return "3-5 ans";
            if (val <= 10) return "6-10 ans";
            return "10 ans ou plus";
        }

        // Si plage d'années (ex: 1-2 ans ou 3 à 5 ans)
        Collections.sort(values);
        int min = values.get(0);
        int max = values.get(values.size() - 1);

        if (max <= 1) return "0-1 ans";
        if (max <= 2) return "1-2 ans";
        if (max <= 5) return "3-5 ans";
        if (max <= 10) return "6-10 ans";

        return "10 ans ou plus"; // Plus de 10 ans
    }
}
