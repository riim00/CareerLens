import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.sql.*;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;

public class SignUpPage extends JFrame {
    private static final String URL = "jdbc:mysql://localhost:3306/careerlens";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    // Enhanced color scheme
    private static final Color BACKGROUND_COLOR = new Color(13, 17, 23);
    private static final Color ACCENT_COLOR = new Color(88, 166, 255);
    private static final Color GLOW_COLOR = new Color(0, 218, 255);
    private static final Color TEXT_COLOR = new Color(255, 255, 255);
    private static final Color FIELD_BACKGROUND = new Color(22, 27, 34);
    private static final Color PARTICLE_COLOR = new Color(88, 166, 255, 50);

    private ArrayList<Particle> particles;
    private Timer particleTimer;
    private Timer glowTimer;
    private float glowIntensity = 0.0f;
    private boolean glowIncreasing = true;

    // Particle class
    private class Particle {
        double x, y;
        double speedX, speedY;
        double size;
        double opacity;

        Particle() {
            reset();
            x = new Random().nextDouble() * getWidth();
            y = new Random().nextDouble() * getHeight();
        }

        void reset() {
            Random random = new Random();
            x = random.nextDouble() * getWidth();
            y = getHeight() + 10;
            speedX = (random.nextDouble() - 0.5) * 2;
            speedY = -1 - random.nextDouble() * 2;
            size = 2 + random.nextDouble() * 4;
            opacity = 0.3 + random.nextDouble() * 0.7;
        }

        void update() {
            x += speedX;
            y += speedY;
            opacity -= 0.005;

            if (y < -10 || opacity <= 0) {
                reset();
            }
        }
    }

    public SignUpPage() {
        setTitle("CareerLens - Registration");
        setSize(500, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 500, 700, 20, 20));

        // Initialize particles
        particles = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle());
        }

        // Main panel with particles and effects
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, BACKGROUND_COLOR,
                        0, getHeight(), new Color(23, 32, 42)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw particles
                for (Particle p : particles) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)p.opacity));
                    g2d.setColor(PARTICLE_COLOR);
                    g2d.fill(new Ellipse2D.Double(p.x, p.y, p.size, p.size));
                }

                // Glow effects
                float alpha = Math.min(0.3f + (glowIntensity * 0.2f), 0.5f);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setColor(GLOW_COLOR);
                g2d.fillOval(-100, -100, 300, 300);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.5f));
                g2d.fillOval(getWidth() - 200, getHeight() - 200, 300, 300);
            }
        };
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Animation timers
        setupAnimationTimers();

        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Title
        JLabel titleLabel = createTitleLabel("JOIN CAREERLENS");
        JLabel subtitleLabel = createSubtitleLabel("NEXT GENERATION CAREER PLATFORM");

        // Form fields
        JTextField nomField = createStyledTextField("SURNAME");
        JTextField prenomField = createStyledTextField("NAME");
        JTextField dateNaissanceField = createStyledTextField("BIRTH DATE (YYYY-MM-DD)");
        JTextField emailField = createStyledTextField("EMAIL");
        JPasswordField passwordField = createStyledPasswordField("PASSWORD");
        JPasswordField confirmPasswordField = createStyledPasswordField("CONFIRM PASSWORD");

        // Signup button
        JButton signUpButton = createStyledButton("INITIALIZE REGISTRATION");

        // Add action listener
        signUpButton.addActionListener(e -> handleSignUp(
                nomField.getText(),
                prenomField.getText(),
                dateNaissanceField.getText(),
                emailField.getText(),
                new String(passwordField.getPassword()),
                new String(confirmPasswordField.getPassword())
        ));

        // Close button
        JButton closeButton = createCloseButton();

        // Back button
        JButton backButton = createBackButton();

        // Layout assembly
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(closeButton, BorderLayout.EAST);
        headerPanel.add(backButton, BorderLayout.WEST);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitleLabel);
        titlePanel.add(Box.createVerticalStrut(30));

        // Add components with spacing
        contentPanel.add(titlePanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(nomField);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(prenomField);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(dateNaissanceField);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(emailField);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(passwordField);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(confirmPasswordField);
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(signUpButton);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
        addWindowDragSupport(mainPanel);
    }

    private void setupAnimationTimers() {
        particleTimer = new Timer(16, e -> {
            for (Particle p : particles) {
                p.update();
            }
            repaint();
        });
        particleTimer.start();

        glowTimer = new javax.swing.Timer(50, e -> {
            if (glowIncreasing) {
                glowIntensity += 0.05f;
                if (glowIntensity >= 1.0f) glowIncreasing = false;
            } else {
                glowIntensity -= 0.05f;
                if (glowIntensity <= 0.0f) glowIncreasing = true;
            }
            repaint();
        });
        glowTimer.start();
    }

    private JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Orbitron", Font.BOLD, 32));
        label.setForeground(TEXT_COLOR);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JLabel createSubtitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Orbitron", Font.PLAIN, 12));
        label.setForeground(ACCENT_COLOR);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw the background
                GradientPaint gradient = new GradientPaint(
                        0, 0, FIELD_BACKGROUND,
                        0, getHeight(), new Color(28, 33, 40)
                );
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));

                // Draw the text
                super.paintComponent(g);

                // Draw the placeholder text if the field is empty
                if (getText().isEmpty() && !hasFocus()) {
                    g2d.setColor(Color.GRAY); // Placeholder color
                    FontMetrics fm = g2d.getFontMetrics();
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(placeholder, 15, y); // Adjust the x-position for padding
                }
            }
        };
        styleTextField(field, placeholder);
        return field;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw the background
                GradientPaint gradient = new GradientPaint(
                        0, 0, FIELD_BACKGROUND,
                        0, getHeight(), new Color(28, 33, 40)
                );
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));

                // Draw the text
                super.paintComponent(g);

                // Draw the placeholder text if the field is empty
                if (getPassword().length == 0 && !hasFocus()) {
                    g2d.setColor(Color.GRAY); // Placeholder color
                    FontMetrics fm = g2d.getFontMetrics();
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(placeholder, 15, y); // Adjust the x-position for padding
                }
            }
        };
        styleTextField(field, placeholder);
        return field;
    }

    private void styleTextField(JTextField field, String placeholder) {
        field.setBackground(new Color(0, 0, 0, 0));
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(ACCENT_COLOR);
        field.setFont(new Font("Share Tech Mono", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(400, 40));
        field.setPreferredSize(new Dimension(400, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Add hover effect
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GLOW_COLOR, 2),
                        BorderFactory.createEmptyBorder(9, 14, 9, 14)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
        });
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, ACCENT_COLOR,
                        getWidth(), getHeight(), GLOW_COLOR
                );
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));

                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();

                g2d.setColor(TEXT_COLOR);
                g2d.drawString(getText(), x, y);
            }
        };

        button.setFont(new Font("Share Tech Mono", Font.BOLD, 16));
        button.setForeground(TEXT_COLOR);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(400, 45));
        button.setPreferredSize(new Dimension(400, 45));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        return button;
    }

    private JButton createCloseButton() {
        JButton button = new JButton("×");
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setForeground(TEXT_COLOR);
        button.setContentAreaFilled(false);
        button.setBorder(null);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setForeground(GLOW_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                button.setForeground(TEXT_COLOR);
            }
        });

        button.addActionListener(e -> {
            fadeOutAndExit();
        });

        return button;
    }

    private JButton createBackButton() {
        JButton button = new JButton("←");
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setForeground(TEXT_COLOR);
        button.setContentAreaFilled(false);
        button.setBorder(null);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setForeground(GLOW_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                button.setForeground(TEXT_COLOR);
            }
        });

        button.addActionListener(e -> {
            fadeOutAndOpenWelcomePage();
        });

        return button;
    }

    private void fadeOutAndExit() {
        javax.swing.Timer fadeTimer = new javax.swing.Timer(20, new ActionListener() {
            float opacity = 1.0f;

            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.1f;
                if (opacity <= 0.0f) {
                    ((javax.swing.Timer)e.getSource()).stop();
                    dispose();
                    System.exit(0);
                }
                setOpacity(Math.max(0.0f, opacity));
            }
        });
        fadeTimer.start();
    }

    private void fadeOutAndOpenWelcomePage() {
        javax.swing.Timer fadeTimer = new javax.swing.Timer(20, new ActionListener() {
            float opacity = 1.0f;

            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.1f;
                if (opacity <= 0.0f) {
                    ((javax.swing.Timer)e.getSource()).stop();
                    dispose();
                    openWelcomePage();
                }
                setOpacity(Math.max(0.0f, opacity));
            }
        });
        fadeTimer.start();
    }

    private void openWelcomePage() {
        SwingUtilities.invokeLater(() -> {
            WelcomePage welcomePage = new WelcomePage();
            welcomePage.setOpacity(0f);
            welcomePage.setVisible(true);

            // Add fade in animation on startup
            Timer fadeInTimer = new Timer(20, new ActionListener() {
                float opacity = 0f;
                @Override
                public void actionPerformed(ActionEvent e) {
                    opacity += 0.05f;
                    if (opacity >= 1f) {
                        opacity = 1f; // Ensure opacity does not exceed 1.0f
                        ((Timer)e.getSource()).stop();
                    }
                    welcomePage.setOpacity(opacity);
                }
            });
            fadeInTimer.start();
        });
    }

    private void addWindowDragSupport(JPanel panel) {
        Point offset = new Point();
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                offset.setLocation(e.getPoint());
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point currentLocation = getLocation();
                setLocation(
                        currentLocation.x + e.getX() - offset.x,
                        currentLocation.y + e.getY() - offset.y
                );
            }
        });
    }

    private void handleSignUp(String surname, String name, String birthDate,
                              String email, String password, String confirmPassword) {
        // Input validation
        if (surname.isEmpty() || name.isEmpty() || birthDate.isEmpty() ||
                email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Please enter a valid email address");
            return;
        }

        if (!isValidDate(birthDate)) {
            showError("Please enter a valid date in YYYY-MM-DD format");
            return;
        }

        // Hash password before storing
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Store user data in database
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "INSERT INTO user (nom, prenom, date_naissance, email, mot_de_passe) " +
                    "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, surname);
            pstmt.setString(2, name);
            pstmt.setDate(3, java.sql.Date.valueOf(birthDate));
            pstmt.setString(4, email);
            pstmt.setString(5, hashedPassword);

            pstmt.executeUpdate();

            // Show success message
            showSuccess("Registration successful!");

            // Redirect to login page
            redirectToLoginPage();

        } catch (SQLException ex) {
            if (ex.getSQLState().equals("23000")) { // Duplicate entry
                showError("This email is already registered");
            } else {
                showError("Database error occurred. Please try again later.");
                ex.printStackTrace();
            }
        }
    }

    private void redirectToLoginPage() {
        // Close the current sign-up page
        this.dispose();

        // Open the login page
        SwingUtilities.invokeLater(() -> {
            new LoginPage().setVisible(true);
        });
    }

    private boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(regex);
    }

    private boolean isValidDate(String date) {
        try {
            java.sql.Date.valueOf(date);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new SignUpPage().setVisible(true);
        });
    }
}