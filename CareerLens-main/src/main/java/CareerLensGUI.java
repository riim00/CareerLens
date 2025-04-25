import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.geom.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ML.models.HardSkillsAnalyzer;
import ML.models.JobTitleRecommender;
import ML.models.ModernClusteringGUI;
import ML.models.RecommendationModel;
import org.mindrot.jbcrypt.BCrypt;
import javax.mail.*;
import javax.mail.internet.*;

public class CareerLensGUI extends JFrame {
    private static final Color DARK_PRIMARY = new Color(32, 33, 36);
    private static final Color DARK_SECONDARY = new Color(41, 42, 45);
    private static final Color ACCENT_1 = new Color(64, 169, 255);
    private static final Color ACCENT_2 = new Color(130, 87, 229);
    private static final Color GLASS_WHITE = new Color(255, 255, 255, 25);
    private boolean isDarkMode = true;
    private Process scrapingProcess;

    public CareerLensGUI() {
        setTitle("CareerLens Premium");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(DARK_PRIMARY);

        JPanel mainPanel = new GlassPanel();
        mainPanel.setLayout(new BorderLayout(40, 40));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Top bar with BACK button and dark mode toggle
        JPanel topBar = new GlassPanel();
        topBar.setLayout(new BorderLayout());

        // BACK button
        JButton backButton = new JButton("← Back");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(DARK_PRIMARY);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            // Navigate back to WelcomePage
            this.dispose(); // Close the current window
            new WelcomePage().setVisible(true); // Open the WelcomePage
        });

        // Dark mode toggle
        JToggleButton darkModeToggle = createToggleButton();

        // Add components to top bar
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        leftPanel.add(backButton);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(darkModeToggle);

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);

        // Particle header
        ParticleHeader headerPanel = new ParticleHeader();
        headerPanel.setPreferredSize(new Dimension(0, 150));

        // Content panel
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(createFeatureCard("Scrapping", "Data Collection", "Automated Multi-Sources", "🔍"));
        contentPanel.add(createFeatureCard("Artificial Intelligence", "And Visualization Options", "Advanced Insights", "🤖"));
        contentPanel.add(createFeatureCard("Visualize", "View table", "Dynamic Filters", "📊"));

        // Add components to main panel
        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(headerPanel, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void showDatabaseViewer() {
        JDialog dialog = createStyledDialog("Job Listings Visualization");
        dialog.setSize(800, 700); // Taille ajustée
        dialog.setResizable(true);
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Filtres
        JPanel filterPanel = new JPanel(new GridLayout(3, 4, 10, 10)); // Grille pour les filtres
        filterPanel.setOpaque(false);

        // Champs de recherche
        JTextField searchField = new JTextField();
        searchField.setBorder(BorderFactory.createTitledBorder("Recherche"));

        JTextField salaireField = new JTextField();
        salaireField.setBorder(BorderFactory.createTitledBorder("Salaire"));

        JTextField experienceField = new JTextField();
        experienceField.setBorder(BorderFactory.createTitledBorder("Expérience"));

        JComboBox<String> secteurCombo = new JComboBox<>(new String[]{"", "IT", "Finance", "Santé", "Education"});
        secteurCombo.setBorder(BorderFactory.createTitledBorder("Secteur"));

        JComboBox<String> contratCombo = new JComboBox<>(new String[]{"", "CDI", "CDD", "Stage", "Freelance"});
        contratCombo.setBorder(BorderFactory.createTitledBorder("Contrat"));

        JComboBox<String> niveauEtudeCombo = new JComboBox<>(new String[]{"", "Bac", "Bac+2", "Licence", "Master", "Doctorat"});
        niveauEtudeCombo.setBorder(BorderFactory.createTitledBorder("Niveau d'Étude"));

        JTextField hardSkillsField = new JTextField();
        hardSkillsField.setBorder(BorderFactory.createTitledBorder("Hard Skills"));

        // Ajout des filtres dans la grille
        filterPanel.add(searchField);
        filterPanel.add(salaireField);
        filterPanel.add(experienceField);
        filterPanel.add(hardSkillsField);
        filterPanel.add(secteurCombo);
        filterPanel.add(contratCombo);
        filterPanel.add(niveauEtudeCombo);

        contentPanel.add(filterPanel, BorderLayout.NORTH);

        // Tableau
        String[] columns = {"ID", "Titre", "Entreprise", "Date", "Salaire", "Secteur", "Contrat", "Expérience", "Hard Skills"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(750, 400));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Bouton Charger
        JButton loadButton = new JButton("Charger");
        loadButton.addActionListener(e -> loadTableData(tableModel, searchField.getText(), salaireField.getText(),
                experienceField.getText(), (String) secteurCombo.getSelectedItem(),
                (String) contratCombo.getSelectedItem(), (String) niveauEtudeCombo.getSelectedItem(),
                hardSkillsField.getText()));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(loadButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void loadTableData(DefaultTableModel tableModel, String filter, String salaire, String experience,
                               String secteur, String contrat, String niveauEtude, String hardSkills) {
        tableModel.setRowCount(0); // Nettoyer les anciennes données

        try {
            DatabaseConnection db = new DatabaseConnection();
            Connection conn = db.getConnection();

            // Nettoyer les données avant de charger

            StringBuilder query = new StringBuilder("SELECT * FROM job_offers WHERE 1=1"); // Requête dynamique

            // Ajouter les filtres si non vide
            if (filter != null && !filter.trim().isEmpty()) {
                query.append(" AND (titre LIKE ? OR nom_entreprise LIKE ? OR competences_recommandees LIKE ?)");
            }
            if (salaire != null && !salaire.trim().isEmpty()) {
                query.append(" AND salaire >= ?");
            }
            if (experience != null && !experience.trim().isEmpty()) {
                query.append(" AND experience LIKE ?");
            }
            if (secteur != null && !secteur.trim().isEmpty()) {
                query.append(" AND secteur_activite LIKE ?");
            }
            if (contrat != null && !contrat.trim().isEmpty()) {
                query.append(" AND type_contrat LIKE ?");
            }
            if (niveauEtude != null && !niveauEtude.trim().isEmpty()) {
                query.append(" AND niveau_etudes LIKE ?");
            }
            if (hardSkills != null && !hardSkills.trim().isEmpty()) {
                query.append(" AND hard_skills LIKE ?");
            }

            PreparedStatement stmt = conn.prepareStatement(query.toString());

            // Remplir les paramètres
            int index = 1;
            if (filter != null && !filter.trim().isEmpty()) {
                stmt.setString(index++, "%" + filter + "%");
                stmt.setString(index++, "%" + filter + "%");
                stmt.setString(index++, "%" + filter + "%");
            }
            if (salaire != null && !salaire.trim().isEmpty()) {
                stmt.setDouble(index++, Double.parseDouble(salaire)); // Convertir en double
            }
            if (experience != null && !experience.trim().isEmpty()) {
                stmt.setString(index++, "%" + experience + "%");
            }
            if (secteur != null && !secteur.trim().isEmpty()) {
                stmt.setString(index++, "%" + secteur + "%");
            }
            if (contrat != null && !contrat.trim().isEmpty()) {
                stmt.setString(index++, "%" + contrat + "%");
            }
            if (niveauEtude != null && !niveauEtude.trim().isEmpty()) {
                stmt.setString(index++, "%" + niveauEtude + "%");
            }
            if (hardSkills != null && !hardSkills.trim().isEmpty()) {
                stmt.setString(index++, "%" + hardSkills + "%");
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("nom_entreprise"),
                        rs.getString("date_publication"),
                        rs.getString("salaire"),
                        rs.getString("secteur_activite"),
                        rs.getString("type_contrat"),
                        rs.getString("experience"),
                        rs.getString("hard_skills")
                });
            }
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur lors du chargement des données !");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Une erreur inattendue s'est produite !");
        }
    }

    private JToggleButton createToggleButton() {
        JToggleButton button = new JToggleButton("🌙");
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.addActionListener(e -> toggleDarkMode());
        return button;
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        SwingUtilities.updateComponentTreeUI(this);
    }

    private JPanel createFeatureCard(String title, String subtitle, String description, String emoji) {
        JPanel card = new GlassPanel();
        card.setLayout(new BorderLayout(15, 15));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        emojiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 24));
        titleLabel.setForeground(ACCENT_1);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Montserrat", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.LIGHT_GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        descLabel.setForeground(Color.GRAY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(Box.createVerticalGlue());
        content.add(emojiLabel);
        content.add(Box.createVerticalStrut(15));
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(5));
        content.add(subtitleLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(descLabel);
        content.add(Box.createVerticalGlue());

        card.add(content, BorderLayout.CENTER);
        addHoverEffect(card);
        addCardClickListener(card, title);

        return card;
    }

    private void addCardClickListener(JPanel card, String type) {
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (type.contains("Scrapping")) {
                    showScrappingDialog();
                } else if (type.contains("Visualize")) {
                    showDatabaseViewer();
                } else {
                    showAIDialog();
                }
            }
        });
    }

    private void addHoverEffect(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                panel.setBorder(new CompoundBorder(
                        new EmptyBorder(20, 20, 20, 20),
                        BorderFactory.createLineBorder(ACCENT_1, 2, true)
                ));
            }

            public void mouseExited(MouseEvent e) {
                panel.setBorder(new EmptyBorder(25, 25, 25, 25));
            }
        });
    }

    private void showScrappingDialog() {
        JDialog dialog = createStyledDialog("Scraping Sources");
        JPanel optionsPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] options = {"Emploi", "Wetech", "Rekrute","Stop"};
        for (String option : options) {
            addOptionButton(optionsPanel, option);
        }

        dialog.add(optionsPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void showAIDialog() {
        JDialog dialog = createStyledDialog("Artificial Intelligence Options");
        JPanel optionsPanel = new JPanel(new GridLayout(2, 1, 15, 15));
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] options = {"Required Skills", "Jobs To Apply To","In Demand Skills","Clusters of skills"};
        for (String option : options) {
            JPanel buttonPanel = new GlassPanel();
            buttonPanel.setLayout(new BorderLayout());
            buttonPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel label = new JLabel(option, SwingConstants.CENTER);
            label.setFont(new Font("Montserrat", Font.PLAIN, 18));
            label.setBorder(new EmptyBorder(15, 15, 15, 15));
            buttonPanel.add(label);

            buttonPanel.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    buttonPanel.setBackground(ACCENT_1);
                    label.setForeground(Color.WHITE);
                }

                public void mouseExited(MouseEvent e) {
                    buttonPanel.setBackground(null);
                    label.setForeground(null);
                }

                public void mouseClicked(MouseEvent e) {
                    if (option.equals("Jobs To Apply To")) {
                        // Launch the JobTitleRecommender GUI
                        SwingUtilities.invokeLater(() -> {
                            try {
                                JobTitleRecommender jobTitleRecommender = new JobTitleRecommender();
                                jobTitleRecommender.createAndShowGUI();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    } else if (option.equals("Required Skills")) {
                        // Launch the RecommendationModel GUI
                        SwingUtilities.invokeLater(() -> {
                            try {
                                RecommendationModel recommendationModel = new RecommendationModel();
                                recommendationModel.createAndShowGUI();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    } else if (option.equals("In Demand Skills")) {
                        // Launch the HardSkillsAnalyzer GUI
                        SwingUtilities.invokeLater(() -> {
                            try {
                                HardSkillsAnalyzer recommendationSkills = new HardSkillsAnalyzer();
                                recommendationSkills.createAndShowGUI();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    }else if (option.equals("Clusters of skills")) {
                        // Launch the ModernClusteringGUI
                        SwingUtilities.invokeLater(() -> {
                            try {
                                ModernClusteringGUI clusteringGUI = new ModernClusteringGUI();
                                clusteringGUI.setVisible(true);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    }

                    else {
                        // Handle other options if needed
                        System.out.println("Selected option: " + option);
                    }
                }
            });

            optionsPanel.add(buttonPanel);
        }

        dialog.add(optionsPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private JDialog createStyledDialog(String title) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(20, 20));

        JPanel headerPanel = new GlassPanel();
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 24));
        titleLabel.setForeground(ACCENT_1);
        headerPanel.add(titleLabel);

        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.getContentPane().setBackground(DARK_PRIMARY);
        return dialog;
    }

    private void startScrapping(String site) {
        String className = ""; // Nom de la classe Java

        switch (site) {
            case "Emploi":
                className = "emploi"; // Nom de la classe Java pour Emploi
                break;
            case "Wetech":
                className = "WeTech";
                break;
            case "Rekrute":
                className = "Rekrute";
                break;
            default:
                System.out.println("Option inconnue");
                return; // Sortir si l'option est invalide
        }

        try {
            // Exécuter la classe Java
            ProcessBuilder pb = new ProcessBuilder(
                    "java",
                    "-cp",
                    "C:\\Users\\pc\\Desktop\\jsoup-1.18.1.jar;C:\\Users\\pc\\Downloads\\mysql-connector-j-8.0.33.jar;C:\\Users\\pc\\IdeaProjects\\CareerLens\\src\\main\\java",
                    className
            );
            scrapingProcess = pb.start();

            pb.inheritIO(); // Affiche la sortie dans la console Java
            Process process = pb.start();
            int exitCode = process.waitFor(); // Attendre la fin du processus
            System.out.println("Scrapping terminé avec code : " + exitCode);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addOptionButton(JPanel panel, String text) {
        JPanel buttonPanel = new GlassPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Montserrat", Font.PLAIN, 18));
        label.setBorder(new EmptyBorder(15, 15, 15, 15));
        buttonPanel.add(label);

        buttonPanel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                buttonPanel.setBackground(ACCENT_1);
                label.setForeground(Color.WHITE);
            }

            public void mouseExited(MouseEvent e) {
                buttonPanel.setBackground(null);
                label.setForeground(null);
            }

            public void mouseClicked(MouseEvent e) {
                // Action spécifique pour lancer le scrapping
                startScrapping(text);
            }
        });

        panel.add(buttonPanel);
    }

    class GlassPanel extends JPanel {
        public GlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 25),
                    getWidth(), getHeight(), new Color(255, 255, 255, 10)
            );
            g2d.setPaint(gradient);
            g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

            g2d.dispose();
        }
    }

    class ParticleHeader extends JPanel {
        private final List<Particle> particles = new ArrayList<>();
        private final Timer timer;

        public ParticleHeader() {
            setOpaque(false);
            for (int i = 0; i < 50; i++) {
                particles.add(new Particle());
            }

            timer = new Timer(16, e -> {
                particles.forEach(Particle::update);
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint titleGradient = new GradientPaint(
                    0, getHeight()/2, ACCENT_1,
                    getWidth(), getHeight()/2, ACCENT_2
            );
            g2d.setPaint(titleGradient);
            g2d.setFont(new Font("Montserrat", Font.BOLD, 40));

            String title = "CareerLens";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(title,
                    (getWidth() - fm.stringWidth(title))/2,
                    getHeight()/2);

            particles.forEach(p -> p.draw(g2d));
            g2d.dispose();
        }
    }

    class Particle {
        private double x, y, vx, vy;
        private final Color color;
        private final int size;

        public Particle() {
            reset();
            color = new Color(
                    ACCENT_1.getRed() + (int)(Math.random() * (ACCENT_2.getRed() - ACCENT_1.getRed())),
                    ACCENT_1.getGreen() + (int)(Math.random() * (ACCENT_2.getGreen() - ACCENT_1.getGreen())),
                    ACCENT_1.getBlue() + (int)(Math.random() * (ACCENT_2.getBlue() - ACCENT_1.getBlue())),
                    50 + (int)(Math.random() * 150)
            );
            size = 2 + (int)(Math.random() * 4);
        }

        public void reset() {
            x = Math.random() * 1000;
            y = Math.random() * 600;
            vx = -0.5 + Math.random();
            vy = -0.5 + Math.random();
        }

        public void update() {
            x += vx;
            y += vy;
            if (x < 0 || x > 1000 || y < 0 || y > 600) reset();
        }

        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.fill(new Ellipse2D.Double(x, y, size, size));
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            CareerLensGUI gui = new CareerLensGUI();
            gui.setVisible(true);
        });
    }
}