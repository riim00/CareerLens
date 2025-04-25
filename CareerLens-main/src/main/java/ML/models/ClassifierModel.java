package ML.models;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.trees.J48;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.ThresholdVisualizePanel;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;

import javax.swing.JFrame;
import java.util.Random;

public class ClassifierModel {

    public static void main(String[] args) {
        try {
            // Charger les données depuis un fichier ARFF
            DataSource source = new DataSource("job_offers_cleaned.arff");
            Instances data = source.getDataSet();
            System.out.println(data.numInstances() + " instances chargées.");

            // Définir l'attribut cible (secteur d'activité)
            data.setClassIndex(data.attribute("secteur_activite").index());

            // Supprimer les attributs non pertinents (exemple : ID)
            String[] opts = new String[]{"-R", "1"};
            Remove remove = new Remove();
            remove.setOptions(opts);
            remove.setInputFormat(data);
            data = Filter.useFilter(data, remove);

            // Sélection des caractéristiques
            AttributeSelection attSelect = new AttributeSelection();
            InfoGainAttributeEval eval = new InfoGainAttributeEval();
            Ranker search = new Ranker();
            attSelect.setEvaluator(eval);
            attSelect.setSearch(search);
            attSelect.SelectAttributes(data);
            int[] indices = attSelect.selectedAttributes();
            System.out.println("Attributs sélectionnés : " + Utils.arrayToString(indices));

            // Construire un arbre de décision J48
            String[] options = new String[1];
            options[0] = "-U";
            J48 tree = new J48();
            tree.setOptions(options);
            tree.buildClassifier(data);
            System.out.println(tree);

            // Classifier une nouvelle instance (exemple fictif)
            double[] vals = new double[data.numAttributes()];
            vals[0] = 1.0; // Exemple d'attributs fictifs
            vals[1] = 0.0;
            vals[2] = 0.0;
            vals[3] = 1.0;
            vals[4] = 0.0;
            vals[5] = 0.0;
            vals[6] = 0.0;
            vals[7] = 1.0;
            Instance myInstance = new DenseInstance(1.0, vals);
            myInstance.setDataset(data);

            double label = tree.classifyInstance(myInstance);
            System.out.println("Classe prédite : " + data.classAttribute().value((int) label));

            // Visualisation de l'arbre de décision
            TreeVisualizer tv = new TreeVisualizer(null, tree.graph(), new PlaceNode2());
            JFrame frame = new javax.swing.JFrame("Visualisation de l'arbre");
            frame.setSize(800, 500);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(tv);
            frame.setVisible(true);
            tv.fitToScreen();

            // Évaluation du modèle
            Classifier cl = new J48();
            Evaluation eval_roc = new Evaluation(data);
            eval_roc.crossValidateModel(cl, data, 10, new Random(1), new Object[]{});
            System.out.println(eval_roc.toSummaryString());
            System.out.println(eval_roc.toMatrixString());

            // Tracer la courbe ROC
            ThresholdCurve tc = new ThresholdCurve();
            int classIndex = 0;
            Instances result = tc.getCurve(eval_roc.predictions(), classIndex);
            ThresholdVisualizePanel vmc = new ThresholdVisualizePanel();
            vmc.setROCString("(Aire sous la courbe ROC = " + tc.getROCArea(result) + ")");
            vmc.setName(result.relationName());
            PlotData2D tempd = new PlotData2D(result);
            tempd.setPlotName(result.relationName());
            tempd.addInstanceNumberAttribute();
            boolean[] cp = new boolean[result.numInstances()];
            for (int n = 1; n < cp.length; n++)
                cp[n] = true;
            tempd.setConnectPoints(cp);

            vmc.addPlot(tempd);
            JFrame frameRoc = new javax.swing.JFrame("Courbe ROC");
            frameRoc.setSize(800, 500);
            frameRoc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frameRoc.getContentPane().add(vmc);
            frameRoc.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
