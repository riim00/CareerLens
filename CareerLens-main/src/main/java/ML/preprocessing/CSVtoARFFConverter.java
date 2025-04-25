package ML.preprocessing;

import weka.core.converters.CSVLoader;
import weka.core.converters.ArffSaver;
import weka.core.Instances;
import java.io.File;

public class CSVtoARFFConverter {
    public static void main(String[] args) {
        try {
            // Charger le fichier CSV
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File("job_offers_cleaned.csv"));
            Instances data = loader.getDataSet();

            // Sauvegarder au format ARFF
            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(new File("job_offers_cleaned.arff"));
            saver.writeBatch();

            System.out.println("Conversion terminée : job_offers_cleaned.arff");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

