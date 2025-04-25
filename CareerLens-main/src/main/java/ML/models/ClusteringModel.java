package ML.models;

import weka.clusterers.SimpleKMeans;
import weka.core.*;
import weka.core.converters.ArffSaver;

import java.io.*;
import java.util.*;

public class ClusteringModel {

    // Méthode pour convertir le fichier CSV en format ARFF avec filtre optionnel
    public static Instances convertToArff(String inputFile, boolean filterImportantValues) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        // Lire la première ligne (header) pour détecter les attributs
        String line = reader.readLine();
        String[] attributes = line.split(",");
        int numAttributes = attributes.length;

        // Liste des attributs
        ArrayList<Attribute> attList = new ArrayList<>();
        Set<String> uniqueNames = new HashSet<>(); // Pour vérifier les doublons
        int count = 1;

        // Ajouter les attributs au dataset
        for (String attr : attributes) {
            String name = attr.trim();

            // Gérer les doublons d'attributs
            while (uniqueNames.contains(name)) {
                name = attr.trim() + "_" + count;
                count++;
            }

            uniqueNames.add(name); // Ajouter au set pour suivi
            attList.add(new Attribute(name)); // Ajouter comme attribut numérique
        }

        // Ajouter un attribut pour le cluster
        Attribute clusterAttr = new Attribute("Cluster");
        attList.add(clusterAttr);

        // Créer un dataset vide
        Instances dataset = new Instances("JobOffersTFIDF", attList, 0);

        // Lire et traiter chaque ligne de données
        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");
            double[] instanceValues = new double[attList.size()];

            for (int i = 0; i < attList.size() - 1; i++) {
                if (i < values.length) {
                    try {
                        double val = Double.parseDouble(values[i].trim());

                        // Appliquer le filtre pour les valeurs importantes
                        if (filterImportantValues && val <= 0.01) {
                            instanceValues[i] = 0.0; // Ignorer les valeurs faibles
                        } else {
                            instanceValues[i] = val; // Conserver les valeurs importantes
                        }

                    } catch (NumberFormatException e) {
                        instanceValues[i] = 0.0; // Valeur par défaut pour erreurs
                    }
                } else {
                    instanceValues[i] = 0.0; // Valeur par défaut pour colonnes manquantes
                }
            }

            // Ajouter une valeur par défaut pour le cluster
            instanceValues[attList.size() - 1] = -1; // Valeur par défaut
            dataset.add(new DenseInstance(1.0, instanceValues));
        }

        reader.close();
        return dataset;
    }

    // Méthode principale
    public static void main(String[] args) throws Exception {
        // Chemin vers le fichier CSV d'entrée
        String inputFile = "job_offers_cleaned.csv";

        // Paramètre pour filtrer les colonnes avec des valeurs importantes
        boolean filterImportantValues = true; // Mettez à false pour désactiver le filtre

        // Convertir en ARFF
        Instances data = convertToArff(inputFile, filterImportantValues);

        // Configuration de K-Means avec k=10 clusters
        SimpleKMeans kmeans = new SimpleKMeans();
        kmeans.setNumClusters(10); // Nombre de clusters
        kmeans.buildClusterer(data);

        // Ajouter les clusters aux données
        Instances clusteredData = new Instances(data);
        for (int i = 0; i < data.numInstances(); i++) {
            int cluster = kmeans.clusterInstance(data.instance(i));
            clusteredData.instance(i).setValue(clusteredData.numAttributes() - 1, cluster);
        }

        // Sauvegarder le fichier ARFF
        ArffSaver saver = new ArffSaver();
        saver.setInstances(clusteredData);
        saver.setFile(new File("job_offers_clusters.arff"));
        saver.writeBatch();

        // Sauvegarder en CSV
        BufferedWriter writer = new BufferedWriter(new FileWriter("job_offers_clusters.csv"));

        // En-tête CSV
        for (int i = 0; i < clusteredData.numAttributes(); i++) {
            writer.write(clusteredData.attribute(i).name());
            if (i < clusteredData.numAttributes() - 1) writer.write(",");
        }
        writer.newLine();

        // Données CSV
        for (int i = 0; i < clusteredData.numInstances(); i++) {
            for (int j = 0; j < clusteredData.numAttributes(); j++) {
                writer.write(String.valueOf(clusteredData.instance(i).value(j)));
                if (j < clusteredData.numAttributes() - 1) writer.write(",");
            }
            writer.newLine();
        }

        writer.close();
        System.out.println("Clustering terminé avec succès !");
    }
}