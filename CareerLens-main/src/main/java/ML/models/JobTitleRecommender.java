package ML.models;

import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class JobTitleRecommender {

    private static final Logger LOGGER = Logger.getLogger(JobTitleRecommender.class.getName());
    private Instances data; // Dataset
    private JFrame frame;
    private JTextField skillsField;
    private JPanel resultPanel;
    private NaiveBayes model; // ML model

    // Words to ignore
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "français", "anglais", "espagnol", "allemand", // Languages
            "le", "la", "les", "de", "des", "du", "et", "à", "au", "avec", "pour" // Articles and prepositions
    ));

    // Colors and Fonts
    private static final Color DARK_BG = new Color(22, 27, 34);
    private static final Color DARKER_BG = new Color(13, 17, 23);
    private static final Color ACCENT_COLOR = new Color(88, 166, 255);
    private static final Color TEXT_COLOR = new Color(230, 237, 243);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new JobTitleRecommender().createAndShowGUI();
            } catch (Exception e) {
                LOGGER.severe("Error during startup: " + e.getMessage());
            }
        });
    }

    // Create the GUI
    public void createAndShowGUI() throws Exception {
        // Load data
        loadData("C:\\Users\\pc\\Desktop\\hardskills.csv"); // Load the new filtered CSV file
        trainModel(); // Train the ML model

        // Setup the frame
        setupFrame();
        setupMainPanel();
        frame.setVisible(true);
    }

    private void setupFrame() {
        frame = new JFrame("Job Title Recommender");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setBackground(DARKER_BG);
    }

    private void setupMainPanel() {
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, DARKER_BG, 0, getHeight(), DARK_BG);
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Add components to main panel
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);

        frame.add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new TransparentPanel();
        headerPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Job Title Recommender", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);

        JLabel subtitleLabel = new JLabel("Enter your skills to get personalized job recommendations", SwingConstants.CENTER);
        subtitleLabel.setFont(REGULAR_FONT);
        subtitleLabel.setForeground(new Color(TEXT_COLOR.getRGB() & 0x00FFFFFF | (180 << 24), true));

        JPanel titleWrapper = new TransparentPanel();
        titleWrapper.setLayout(new BoxLayout(titleWrapper, BoxLayout.Y_AXIS));
        titleWrapper.add(titleLabel);
        titleWrapper.add(Box.createVerticalStrut(10));
        titleWrapper.add(subtitleLabel);

        headerPanel.add(titleWrapper, BorderLayout.CENTER);
        return headerPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new TransparentPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Input Section
        JPanel inputSection = createInputSection();
        centerPanel.add(inputSection);
        centerPanel.add(Box.createVerticalStrut(30));

        // Results Section
        resultPanel = createResultsPanel();
        centerPanel.add(resultPanel);

        return centerPanel;
    }

    private JPanel createInputSection() {
        JPanel inputPanel = new RoundedPanel(15);
        inputPanel.setLayout(new BorderLayout(15, 15));
        inputPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        inputPanel.setBackground(DARK_BG);
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel skillsLabel = new JLabel("Skills");
        skillsLabel.setFont(HEADER_FONT);
        skillsLabel.setForeground(TEXT_COLOR);

        skillsField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DARKER_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        skillsField.setOpaque(false);
        skillsField.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        skillsField.setForeground(TEXT_COLOR);
        skillsField.setCaretColor(TEXT_COLOR);
        skillsField.setFont(REGULAR_FONT);

        JButton searchButton = new JButton("Get Recommendations") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                        0, 0, ACCENT_COLOR,
                        0, getHeight(), new Color(64, 120, 192)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(HEADER_FONT);
        searchButton.setBorderPainted(false);
        searchButton.setFocusPainted(false);
        searchButton.setContentAreaFilled(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.addActionListener(e -> {
            try {
                generateJobTitleRecommendations();
            } catch (Exception ex) {
                LOGGER.severe("Error during recommendations: " + ex.getMessage());
            }
        });

        JPanel inputWrapper = new TransparentPanel();
        inputWrapper.setLayout(new BorderLayout(10, 0));
        inputWrapper.add(skillsField, BorderLayout.CENTER);
        inputWrapper.add(searchButton, BorderLayout.EAST);

        inputPanel.add(skillsLabel, BorderLayout.NORTH);
        inputPanel.add(inputWrapper, BorderLayout.CENTER);

        return inputPanel;
    }

    private JPanel createResultsPanel() {
        JPanel resultsPanel = new RoundedPanel(15);
        resultsPanel.setLayout(new BorderLayout(15, 15));
        resultsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        resultsPanel.setBackground(DARK_BG);

        JLabel resultsLabel = new JLabel("Recommended Job Titles");
        resultsLabel.setFont(HEADER_FONT);
        resultsLabel.setForeground(TEXT_COLOR);

        resultsPanel.add(resultsLabel, BorderLayout.NORTH);

        return resultsPanel;
    }

    // Load data
    private void loadData(String filePath) throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(filePath));
        data = loader.getDataSet();

        if (data.attribute(data.numAttributes() - 1).isNumeric()) {
            NumericToNominal filter = new NumericToNominal();
            filter.setInputFormat(data);
            data = Filter.useFilter(data, filter);
        }

        data.setClassIndex(data.attribute("titre").index()); // Set class index to "titre"
    }

    // Train the model
    private void trainModel() throws Exception {
        model = new NaiveBayes();
        model.buildClassifier(data);
    }

    // Generate job title recommendations based on skills
    private void generateJobTitleRecommendations() throws Exception {
        // Clear previous results
        resultPanel.removeAll();

        JLabel resultsLabel = new JLabel("Recommended Job Titles");
        resultsLabel.setFont(HEADER_FONT);
        resultsLabel.setForeground(TEXT_COLOR);
        resultPanel.add(resultsLabel, BorderLayout.NORTH);

        // Create results container
        JPanel recommendationsPanel = new TransparentPanel();
        recommendationsPanel.setLayout(new BoxLayout(recommendationsPanel, BoxLayout.Y_AXIS));

        // Add animated loading indicator
        JPanel loadingPanel = new LoadingPanel();
        recommendationsPanel.add(loadingPanel);
        resultPanel.add(recommendationsPanel, BorderLayout.CENTER);
        resultPanel.revalidate();
        resultPanel.repaint();

        // Simulate recommendation generation with a delay
        Timer timer = new Timer(1500, e -> {
            recommendationsPanel.removeAll();

            String inputSkills = skillsField.getText().trim().toLowerCase(); // Use skillsField for input

            // Prepare job title frequency
            Map<String, Integer> jobTitleCount = new HashMap<>();
            int titleIndex = data.attribute("titre").index();
            int hardSkillsIndex = data.attribute("hard_skills").index();

            for (int i = 0; i < data.numInstances(); i++) {
                String jobTitle = data.instance(i).stringValue(titleIndex).toLowerCase();
                String hardSkills = data.instance(i).stringValue(hardSkillsIndex).toLowerCase();
                LOGGER.info("Job Title: " + jobTitle + ", Skills: " + hardSkills);

                double similarity = calculateSkillSimilarity(inputSkills, hardSkills);
                LOGGER.info("Similarity: " + similarity);

                if (similarity > 0.1) { // Lower threshold
                    jobTitleCount.put(jobTitle, jobTitleCount.getOrDefault(jobTitle, 0) + 1);
                }
            }

            // Sort and display recommendations
            List<Map.Entry<String, Integer>> sortedJobTitles = new ArrayList<>(jobTitleCount.entrySet());
            sortedJobTitles.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

            int count = 0;
            for (Map.Entry<String, Integer> entry : sortedJobTitles) {
                if (count++ == 3) break; // Limit to 3 recommendations
                JPanel titlePanel = createRecommendationPanel(entry.getKey());
                recommendationsPanel.add(titlePanel);
                recommendationsPanel.add(Box.createVerticalStrut(10));
            }

            recommendationsPanel.revalidate();
            recommendationsPanel.repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }

    // Calculate similarity between input skills and job skills
    private double calculateSkillSimilarity(String inputSkills, String jobSkills) {
        // Normalize special characters and delimiters
        inputSkills = inputSkills.replace("c#", "csharp").replace("c++", "cpp");
        jobSkills = jobSkills.replace("c#", "csharp").replace("c++", "cpp");

        // Split skills by commas, spaces, or slashes
        Set<String> inputSkillSet = new HashSet<>(Arrays.asList(inputSkills.split("[, /]+")));
        Set<String> jobSkillSet = new HashSet<>(Arrays.asList(jobSkills.split("[, /]+")));

        // Remove stopwords
        inputSkillSet.removeAll(STOPWORDS);
        jobSkillSet.removeAll(STOPWORDS);

        // Keep only common skills
        inputSkillSet.retainAll(jobSkillSet);

        // Calculate similarity
        return (double) inputSkillSet.size() / Math.max(inputSkillSet.size(), jobSkillSet.size());
    }

    private JPanel createRecommendationPanel(String title) {
        JPanel panel = new RoundedPanel(10);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));
        panel.setBackground(DARKER_BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(REGULAR_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel, BorderLayout.CENTER);

        return panel;
    }

    // Custom transparent panel
    private static class TransparentPanel extends JPanel {
        TransparentPanel() {
            setOpaque(false);
        }
    }

    // Custom rounded panel
    private static class RoundedPanel extends JPanel {
        private final int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
        }
    }

    // Custom loading animation panel
    private static class LoadingPanel extends JPanel {
        private final Timer timer;
        private int angle = 0;

        LoadingPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(40, 40));

            timer = new Timer(50, e -> {
                angle = (angle + 30) % 360;
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 10;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setStroke(new BasicStroke(3));
            g2.setColor(new Color(ACCENT_COLOR.getRGB() & 0x00FFFFFF | (50 << 24), true));
            g2.drawArc(x, y, size, size, 0, 360);

            g2.setColor(ACCENT_COLOR);
            g2.drawArc(x, y, size, size, angle, 120);
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            timer.stop();
        }
    }
}