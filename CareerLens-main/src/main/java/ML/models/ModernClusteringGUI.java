package ML.models;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;
import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.*;
import weka.filters.unsupervised.attribute.*;
import weka.clusterers.*;
import weka.filters.unsupervised.instance.RemoveDuplicates;

import javax.swing.plaf.basic.BasicScrollBarUI;

public class ModernClusteringGUI extends JFrame {
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 35);
    private static final Color FOREGROUND_COLOR = new Color(220, 220, 220);
    private static final Color ACCENT_COLOR = new Color(0, 150, 255);
    private static final Color BUTTON_HOVER_COLOR = new Color(60, 60, 65);
    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    private JTextArea resultArea;
    private JButton loadButton;
    private JButton clusterButton;
    private JPanel statusPanel;
    private JLabel statusLabel;
    private File selectedFile;

    public ModernClusteringGUI() {
        setTitle("Advanced Clustering Analysis");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set up the main container with a dark theme
        Container contentPane = getContentPane();
        contentPane.setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(10, 10));

        // Create the main content panel
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);

        // Create the status panel
        statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);

        // Apply modern look and feel
        applyModernLookAndFeel();
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        panel.add(buttonPanel, BorderLayout.NORTH);

        // Create result area
        resultArea = createResultArea();
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(createModernBorder());
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setBackground(BACKGROUND_COLOR);

        loadButton = createStyledButton("Load Dataset", new ImageIcon("load_icon.png"));
        clusterButton = createStyledButton("Analyze Clusters", new ImageIcon("cluster_icon.png"));

        panel.add(loadButton);
        panel.add(clusterButton);

        setupButtonListeners();

        return panel;
    }

    private JButton createStyledButton(String text, ImageIcon icon) {
        JButton button = new JButton(text);
        button.setFont(MAIN_FONT);
        button.setForeground(FOREGROUND_COLOR);
        button.setBackground(BACKGROUND_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_HOVER_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(BACKGROUND_COLOR);
            }
        });

        return button;
    }

    private JTextArea createResultArea() {
        JTextArea area = new JTextArea();
        area.setFont(new Font("Consolas", Font.PLAIN, 14));
        area.setBackground(new Color(40, 40, 45));
        area.setForeground(FOREGROUND_COLOR);
        area.setCaretColor(FOREGROUND_COLOR);
        area.setMargin(new Insets(10, 10, 10, 10));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(25, 25, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(MAIN_FONT);
        statusLabel.setForeground(FOREGROUND_COLOR);
        panel.add(statusLabel, BorderLayout.WEST);

        return panel;
    }

    private void setupButtonListeners() {
        loadButton.addActionListener(e -> {
            JFileChooser fileChooser = createStyledFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                updateStatus("Dataset loaded: " + selectedFile.getName());
            }
        });

        clusterButton.addActionListener(e -> {
            if (selectedFile != null) {
                try {
                    updateStatus("Processing clusters...");
                    SwingWorker<String, Void> worker = new SwingWorker<>() {
                        @Override
                        protected String doInBackground() throws Exception {
                            return performClustering(selectedFile);
                        }

                        @Override
                        protected void done() {
                            try {
                                resultArea.setText(get());
                                updateStatus("Clustering complete");
                            } catch (Exception ex) {
                                resultArea.setText("Error: " + ex.getMessage());
                                updateStatus("Error during clustering");
                            }
                        }
                    };
                    worker.execute();
                } catch (Exception ex) {
                    updateStatus("Error: " + ex.getMessage());
                }
            } else {
                updateStatus("Please load a dataset first");
            }
        });
    }

    private String performClustering(File file) throws Exception {
        // Load dataset
        DataSource source = new DataSource(file.getAbsolutePath());
        Instances data = source.getDataSet();

        if (data == null) {
            throw new Exception("Failed to load dataset. Check the file format and path.");
        }

        data.setClassIndex(-1); // No class attribute for clustering

        // Remove duplicate rows
        RemoveDuplicates removeDuplicatesFilter = new RemoveDuplicates();
        removeDuplicatesFilter.setInputFormat(data);
        data = Filter.useFilter(data, removeDuplicatesFilter);

        // Remove unnecessary attributes (assuming first column is ID)
        Remove removeFilter = new Remove();
        removeFilter.setAttributeIndices("1"); // Remove the first column (id)
        removeFilter.setInputFormat(data);
        data = Filter.useFilter(data, removeFilter);

        // Convert text attributes to word vectors
        StringToWordVector wordVectorFilter = new StringToWordVector();
        wordVectorFilter.setInputFormat(data);
        wordVectorFilter.setLowerCaseTokens(true);
        wordVectorFilter.setWordsToKeep(1000);
        wordVectorFilter.setOutputWordCounts(true);
        Instances wordData = Filter.useFilter(data, wordVectorFilter);

        // Apply PCA for dimensionality reduction
        PrincipalComponents pca = new PrincipalComponents();
        pca.setInputFormat(wordData);
        pca.setMaximumAttributes(2); // Reduce to 2 dimensions for visualization
        pca.setVarianceCovered(0.95);
        Instances reducedData = Filter.useFilter(wordData, pca);

        // Create and configure KMeans clusterer
        SimpleKMeans kMeans = new SimpleKMeans();
        kMeans.setNumClusters(20);
        kMeans.setPreserveInstancesOrder(true);
        kMeans.buildClusterer(reducedData);

        // Evaluate clustering
        ClusterEvaluation eval = new ClusterEvaluation();
        eval.setClusterer(kMeans);
        eval.evaluateClusterer(reducedData);

        // Visualize clusters
        visualizeClusters(reducedData, kMeans);

        // Return clustering results
        StringBuilder results = new StringBuilder();
        results.append("=== Clustering Results ===\n\n");
        results.append("Clustering Algorithm: K-Means\n");
        results.append("Number of clusters: ").append(kMeans.getNumClusters()).append("\n\n");
        results.append("=== Model ===\n");
        results.append(kMeans.toString()).append("\n");
        results.append("\n=== Cluster Evaluation ===\n");
        results.append("Number of clustered instances: ").append(reducedData.numInstances()).append("\n");

        // Add cluster sizes
        double[] assignments = eval.getClusterAssignments();
        int[] clusterSizes = new int[kMeans.getNumClusters()];
        for (double assignment : assignments) {
            clusterSizes[(int) assignment]++;
        }
        results.append("\nCluster sizes:\n");
        for (int i = 0; i < clusterSizes.length; i++) {
            double percentage = (clusterSizes[i] * 100.0) / reducedData.numInstances();
            results.append(String.format("Cluster %d: %d instances (%.1f%%)\n",
                    i, clusterSizes[i], percentage));
        }

        // Add hard skills for each cluster (with counts)
        results.append("\n=== Hard Skills per Cluster ===\n");
        for (int i = 0; i < kMeans.getNumClusters(); i++) {
            results.append("\nCluster ").append(i).append(":\n");

            // Use a map to count occurrences of each skill
            Map<String, Integer> skillCounts = new HashMap<>();

            for (int j = 0; j < data.numInstances(); j++) {
                if (kMeans.clusterInstance(reducedData.instance(j)) == i) {
                    String hardSkills = data.instance(j).stringValue(data.attribute("hard_skills"));

                    // Split skills if they are comma-separated
                    String[] skills = hardSkills.split(",");
                    for (String skill : skills) {
                        skill = skill.trim(); // Remove leading/trailing spaces
                        skillCounts.put(skill, skillCounts.getOrDefault(skill, 0) + 1);
                    }
                }
            }

            // Format the skills with their counts
            for (Map.Entry<String, Integer> entry : skillCounts.entrySet()) {
                results.append("- ").append(entry.getKey()).append("(").append(entry.getValue()).append(")\n");
            }
        }

        return results.toString();
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private JFreeChart createStyledChart(XYSeriesCollection dataset) {
        JFreeChart chart = ChartFactory.createScatterPlot(
                "Cluster Analysis Results",
                "Principal Component 1",
                "Principal Component 2",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Apply dark theme to chart
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(BACKGROUND_COLOR);
        plot.setDomainGridlinePaint(new Color(70, 70, 75));
        plot.setRangeGridlinePaint(new Color(70, 70, 75));

        chart.setBackgroundPaint(BACKGROUND_COLOR);
        chart.getTitle().setPaint(FOREGROUND_COLOR);
        chart.getLegend().setBackgroundPaint(BACKGROUND_COLOR);
        chart.getLegend().setItemPaint(FOREGROUND_COLOR);

        // Style the points
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
        renderer.setSeriesPaint(0, new Color(255, 100, 100));
        renderer.setSeriesPaint(1, new Color(100, 255, 100));
        renderer.setSeriesPaint(2, new Color(100, 100, 255));
        plot.setRenderer(renderer);

        return chart;
    }

    private void visualizeClusters(Instances data, SimpleKMeans kMeans) throws Exception {
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (int i = 0; i < kMeans.getNumClusters(); i++) {
            XYSeries series = new XYSeries("Cluster " + (i + 1));
            for (int j = 0; j < data.numInstances(); j++) {
                if (kMeans.clusterInstance(data.instance(j)) == i) {
                    series.add(data.instance(j).value(0), data.instance(j).value(1));
                }
            }
            dataset.addSeries(series);
        }

        JFreeChart chart = createStyledChart(dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(BACKGROUND_COLOR);

        JFrame chartFrame = new JFrame("Cluster Visualization");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.setBackground(BACKGROUND_COLOR);
        chartFrame.add(chartPanel);
        chartFrame.pack();
        chartFrame.setLocationRelativeTo(this);
        chartFrame.setVisible(true);
    }

    private JFileChooser createStyledFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        updateFileChooserUI(fileChooser.getComponents());
        return fileChooser;
    }

    private void updateFileChooserUI(Component[] components) {
        for (Component component : components) {
            if (component instanceof JPanel || component instanceof JTextField) {
                component.setBackground(BACKGROUND_COLOR);
                component.setForeground(FOREGROUND_COLOR);
            }
            if (component instanceof Container) {
                updateFileChooserUI(((Container) component).getComponents());
            }
        }
    }

    private Border createModernBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60, 60, 65), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );
    }

    private void applyModernLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Panel.background", BACKGROUND_COLOR);
            UIManager.put("TextArea.background", new Color(40, 40, 45));
            UIManager.put("TextArea.foreground", FOREGROUND_COLOR);
            UIManager.put("ScrollBar.thumb", new Color(100, 100, 105));
            UIManager.put("ScrollBar.track", new Color(50, 50, 55));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(100, 100, 105);
            thumbDarkShadowColor = thumbColor;
            thumbHighlightColor = thumbColor;
            thumbLightShadowColor = thumbColor;
            trackColor = new Color(50, 50, 55);
            trackHighlightColor = trackColor;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ModernClusteringGUI().setVisible(true);
        });
    }
}