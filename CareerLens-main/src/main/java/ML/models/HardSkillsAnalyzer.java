package ML.models;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.List;

public class HardSkillsAnalyzer extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/careerlens";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    private static final Set<String> WORDS_TO_IGNORE = new HashSet<>(Arrays.asList(
            "sage", "comptabilité", "audit", "coordination", "planifiquation", "word",
            "qualite", "français", "anglais", "sem"
    ));

    private JPanel chartPanel;
    private JComboBox<String> timeRangeComboBox;
    private JSpinner topNSpinner;
    private JTextField searchField;
    private JList<String> skillsList;
    private DefaultListModel<String> skillsListModel;
    private JProgressBar analysisProgress;
    private JLabel totalRecordsLabel;
    private JLabel lastUpdateLabel;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(() -> {
            new HardSkillsAnalyzer().setVisible(true);
        });
    }

    public HardSkillsAnalyzer() {
        setTitle("CareerLens - Hard Skills Analysis Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(1200, 800));

        initializeComponents();
        setupMainPanel();
        pack();
        setLocationRelativeTo(null);

        // Initial analysis
        runAnalysis();
    }

    public void createAndShowGUI() {
        SwingUtilities.invokeLater(() -> {
            HardSkillsAnalyzer analyzer = new HardSkillsAnalyzer();
            analyzer.setVisible(true);
        });
    }

    private void initializeComponents() {
        // Initialize all components
        chartPanel = new JPanel(new BorderLayout());
        timeRangeComboBox = new JComboBox<>(new String[]{
                "Last 30 days", "Last 90 days", "Last 6 months", "Last year", "All time"
        });
        topNSpinner = new JSpinner(new SpinnerNumberModel(10, 5, 50, 5));
        searchField = new JTextField(20);
        skillsListModel = new DefaultListModel<>();
        skillsList = new JList<>(skillsListModel);
        analysisProgress = new JProgressBar();
        totalRecordsLabel = new JLabel("Total records: 0");
        lastUpdateLabel = new JLabel("Last updated: Never");
    }

    private void setupMainPanel() {
        // Create main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(240, 242, 245),
                        0, h, new Color(233, 236, 239)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        createHeaderPanel(mainPanel);
        createToolbarPanel(mainPanel);
        createContentPanel(mainPanel);
        createFooterPanel(mainPanel);

        add(mainPanel);

        // Add listeners
        setupEventListeners();
    }

    private void createHeaderPanel(JPanel mainPanel) {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(new Color(64, 169, 255));
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Title and subtitle
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Hard Skills Analysis Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Real-time analysis of in-demand skills");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Stats panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.add(totalRecordsLabel);
        statsPanel.add(lastUpdateLabel);
        headerPanel.add(statsPanel, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
    }

    private void createToolbarPanel(JPanel mainPanel) {
        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 5));
        toolbarPanel.setBackground(new Color(255, 255, 255, 200));
        toolbarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // Time range selector
        JPanel timeRangePanel = createLabeledComponent("Time Range:", timeRangeComboBox);
        timeRangeComboBox.setPreferredSize(new Dimension(150, 30));

        // Top N selector
        JPanel topNPanel = createLabeledComponent("Top Skills:", topNSpinner);
        topNSpinner.setPreferredSize(new Dimension(80, 30));
        ((JSpinner.DefaultEditor) topNSpinner.getEditor()).getTextField().setEditable(false);

        // Search field
        JPanel searchPanel = createLabeledComponent("Search:", searchField);
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.putClientProperty("JTextField.placeholderText", "Search skills...");

        // Progress bar
        analysisProgress.setPreferredSize(new Dimension(150, 30));
        analysisProgress.setStringPainted(true);

        // Add components to toolbar
        toolbarPanel.add(timeRangePanel);
        toolbarPanel.add(topNPanel);
        toolbarPanel.add(searchPanel);
        toolbarPanel.add(analysisProgress);

        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setPreferredSize(new Dimension(100, 30));
        refreshButton.addActionListener(e -> runAnalysis());
        toolbarPanel.add(refreshButton);

        mainPanel.add(toolbarPanel, BorderLayout.NORTH);
    }

    private JPanel createLabeledComponent(String labelText, JComponent component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(label);
        panel.add(component);
        return panel;
    }

    private void createContentPanel(JPanel mainPanel) {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setOpaque(false);

        // Chart panel
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // Skills list
        skillsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        skillsList.setCellRenderer(new SkillListCellRenderer());
        JScrollPane skillsScrollPane = new JScrollPane(skillsList);
        skillsScrollPane.setPreferredSize(new Dimension(250, 0));
        skillsScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        contentPanel.add(chartPanel, BorderLayout.CENTER);
        contentPanel.add(skillsScrollPane, BorderLayout.EAST);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private void createFooterPanel(JPanel mainPanel) {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(64, 169, 255));
        footerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel copyrightLabel = new JLabel("©️ 2024 CareerLens | All rights reserved");
        copyrightLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        copyrightLabel.setForeground(Color.WHITE);
        footerPanel.add(copyrightLabel, BorderLayout.WEST);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        timeRangeComboBox.addActionListener(e -> runAnalysis());
        topNSpinner.addChangeListener(e -> runAnalysis());
        searchField.addActionListener(e -> runAnalysis());
    }

    private void runAnalysis() {
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            private List<Map.Entry<String, Integer>> topSkills;
            private int totalRecords = 0;

            @Override
            protected Void doInBackground() throws Exception {
                analysisProgress.setIndeterminate(true);
                analysisProgress.setString("Analyzing...");

                List<String> hardSkills = extractHardSkills();
                totalRecords = hardSkills.size();
                Map<String, Integer> skillCounts = countSkillOccurrences(hardSkills);
                topSkills = getTopSkills(skillCounts, (Integer) topNSpinner.getValue());

                return null;
            }

            @Override
            protected void done() {
                updateChart(topSkills);
                updateSkillsList(topSkills);
                updateStats(totalRecords);
                analysisProgress.setIndeterminate(false);
                analysisProgress.setString("Analysis complete");
            }
        };
        worker.execute();
    }

    private List<String> extractHardSkills() {
        List<String> hardSkills = new ArrayList<>();
        String timeFilter = getTimeFilter();
        String query = "SELECT hard_skills FROM job_offers " + timeFilter;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String skills = rs.getString("hard_skills");
                if (skills != null && !skills.isEmpty()) {
                    hardSkills.add(skills);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Database error: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            });
        }
        return hardSkills;
    }

    private String getTimeFilter() {
        return switch(timeRangeComboBox.getSelectedIndex()) {
            case 0 -> "WHERE date_publication >= DATE_SUB(NOW(), INTERVAL 30 DAY)";
            case 1 -> "WHERE date_publication >= DATE_SUB(NOW(), INTERVAL 90 DAY)";
            case 2 -> "WHERE date_publication >= DATE_SUB(NOW(), INTERVAL 180 DAY)";
            case 3 -> "WHERE date_publication >= DATE_SUB(NOW(), INTERVAL 1 YEAR)";
            default -> "";
        };
    }

    private String decodeSpecialCharacters(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.ISO_8859_1);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private Map<String, Integer> countSkillOccurrences(List<String> hardSkills) {
        Map<String, Integer> skillCounts = new HashMap<>();
        String searchTerm = searchField.getText().toLowerCase().trim();

        for (String skills : hardSkills) {
            // Decode special characters
            skills = decodeSpecialCharacters(skills);

            // Split the skills string into individual skills
            String[] skillArray = skills.split("[,\\-]"); // Split by comma or hyphen
            for (String skill : skillArray) {
                skill = skill.trim().toLowerCase();
                if (!skill.isEmpty() && !WORDS_TO_IGNORE.contains(skill) &&
                        (searchTerm.isEmpty() || skill.contains(searchTerm))) {
                    skillCounts.merge(skill, 1, Integer::sum);
                }
            }
        }
        return skillCounts;
    }

    private List<Map.Entry<String, Integer>> getTopSkills(Map<String, Integer> skillCounts, int topN) {
        return skillCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topN)
                .toList();
    }

    private void updateChart(List<Map.Entry<String, Integer>> topSkills) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : topSkills) {
            dataset.addValue(entry.getValue(), "Skills", entry.getKey());
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Most In-Demand Hard Skills",
                "Skills",
                "Frequency",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        customizeChart(barChart);

        ChartPanel newChartPanel = new ChartPanel(barChart);
        newChartPanel.setPreferredSize(new Dimension(800, 500));
        newChartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        chartPanel.removeAll();
        chartPanel.add(newChartPanel);
        chartPanel.revalidate();
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void customizeChart(JFreeChart chart) {
        // Customize plot
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setDomainGridlinePaint(new Color(230, 230, 230));

        // Customize renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(64, 169, 255));
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(true);
        renderer.setSeriesOutlinePaint(0, new Color(48, 127, 191));
        renderer.setItemMargin(0.1);

        // Customize domain axis (x-axis)
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
        );
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        domainAxis.setLabelFont(new Font("Segoe UI", Font.BOLD, 14));

        // Customize range axis (y-axis)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        rangeAxis.setLabelFont(new Font("Segoe UI", Font.BOLD, 14));

        // Customize chart title
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 18));
        chart.setBackgroundPaint(Color.WHITE);
    }

    private void updateSkillsList(List<Map.Entry<String, Integer>> topSkills) {
        skillsListModel.clear();
        for (Map.Entry<String, Integer> entry : topSkills) {
            skillsListModel.addElement(entry.getKey() + " (" + entry.getValue() + ")");
        }
    }

    private void updateStats(int totalRecords) {
        totalRecordsLabel.setText("Total records: " + totalRecords);
        lastUpdateLabel.setText("Last updated: " + new java.text.SimpleDateFormat("HH:mm:ss")
                .format(new Date()));
    }

    // Custom renderer for the skills list
    private static class SkillListCellRenderer extends DefaultListCellRenderer {
        private static final Color SELECTED_BACKGROUND = new Color(64, 169, 255);
        private static final Color HOVER_BACKGROUND = new Color(245, 247, 250);
        private static final Border BOTTOM_BORDER = BorderFactory.createMatteBorder(
                0, 0, 1, 0, new Color(230, 230, 230));
        private static final Border COMPOUND_BORDER = BorderFactory.createCompoundBorder(
                BOTTOM_BORDER, new EmptyBorder(8, 10, 8, 10));

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            // Set border
            label.setBorder(COMPOUND_BORDER);

            // Set colors
            if (isSelected) {
                label.setBackground(SELECTED_BACKGROUND);
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(index % 2 == 0 ? Color.WHITE : HOVER_BACKGROUND);
                label.setForeground(new Color(33, 37, 41));
            }

            // Set font
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            // Format the text
            String text = value.toString();
            int startCount = text.lastIndexOf('(');
            if (startCount > -1) {
                String skillName = text.substring(0, startCount).trim();
                String count = text.substring(startCount);
                text = String.format("<html><b>%s</b> <span style='color: %s;'>%s</span></html>",
                        skillName,
                        isSelected ? "white" : "gray",
                        count);
                label.setText(text);
            }

            return label;
        }
    }

    // Export functionality
    private void exportData(List<Map.Entry<String, Integer>> skills) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Data");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "CSV Files", "csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(
                    new java.io.FileWriter(fileChooser.getSelectedFile()))) {

                writer.println("Skill,Frequency");
                for (Map.Entry<String, Integer> entry : skills) {
                    writer.printf("%s,%d%n", entry.getKey(), entry.getValue());
                }

                JOptionPane.showMessageDialog(this,
                        "Data exported successfully!",
                        "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting data: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Settings dialog
    private void showSettingsDialog() {
        JDialog settingsDialog = new JDialog(this, "Settings", true);
        settingsDialog.setLayout(new BorderLayout());
        settingsDialog.setSize(400, 300);
        settingsDialog.setLocationRelativeTo(this);

        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Database settings
        gbc.gridx = 0; gbc.gridy = 0;
        settingsPanel.add(new JLabel("Database URL:"), gbc);

        gbc.gridx = 1;
        JTextField dbUrlField = new JTextField(DB_URL);
        settingsPanel.add(dbUrlField, gbc);

        // Add more settings as needed...

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            // Save settings logic here
            settingsDialog.dispose();
        });

        cancelButton.addActionListener(e -> settingsDialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        settingsDialog.add(settingsPanel, BorderLayout.CENTER);
        settingsDialog.add(buttonPanel, BorderLayout.SOUTH);
        settingsDialog.setVisible(true);
    }
}