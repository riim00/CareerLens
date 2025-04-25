import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;
import org.mindrot.jbcrypt.BCrypt;

public class LoginPage extends JFrame {
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
    private Random random = new Random();

    // Particle class
    private class Particle {
        double x, y;
        double speedX, speedY;
        double size;
        double opacity;

        Particle() {
            reset();
            x = random.nextDouble() * getWidth();
            y = random.nextDouble() * getHeight();
        }

        void reset() {
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

    public LoginPage() {
        setTitle("CareerLens - Next Gen");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 500, 600, 20, 20));

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

                // Glow effect
                float alpha = Math.min(0.3f + (glowIntensity * 0.2f), 0.5f);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setColor(GLOW_COLOR);

                // Multiple glow sources
                g2d.fillOval(-100, -100, 300, 300);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.5f));
                g2d.fillOval(getWidth() - 200, getHeight() - 200, 300, 300);
            }
        };
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Particle animation timer
        particleTimer = new Timer(16, e -> {
            for (Particle p : particles) {
                p.update();
            }
            mainPanel.repaint();
        });
        particleTimer.start();

        // Glow animation timer
        glowTimer = new Timer(50, e -> {
            if (glowIncreasing) {
                glowIntensity += 0.05f;
                if (glowIntensity >= 1.0f) glowIncreasing = false;
            } else {
                glowIntensity -= 0.05f;
                if (glowIntensity <= 0.0f) glowIncreasing = true;
            }
            mainPanel.repaint();
        });
        glowTimer.start();

        // Logo Panel with enhanced visuals
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("CAREERLENS");
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(new Font("Orbitron", Font.BOLD, 36));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 30, 0));

        // Add drop shadow effect to title
        titleLabel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 0, 30, 0),
                new Border() {
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(GLOW_COLOR);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                        g2.drawString(((JLabel)c).getText(), x + 2, y + height - 29);
                        g2.dispose();
                    }
                    public Insets getBorderInsets(Component c) { return new Insets(0, 0, 0, 0); }
                    public boolean isBorderOpaque() { return true; }
                }
        ));

        JLabel subtitleLabel = new JLabel("NEXT GENERATION CAREER PLATFORM");
        subtitleLabel.setForeground(ACCENT_COLOR);
        subtitleLabel.setFont(new Font("Orbitron", Font.PLAIN, 14));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        logoPanel.add(titleLabel, BorderLayout.CENTER);
        logoPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Enhanced Form Panel
        JPanel formPanel = new JPanel(new GridLayout(3, 1, 25, 25));
        formPanel.setOpaque(false);

        JTextField emailField = createStyledTextField("ENTER YOUR EMAIL");
        JPasswordField passwordField = createStyledPasswordField("ENTER YOUR PASSWORD");
        JButton loginButton = createStyledButton("ACCESS SYSTEM");

        // Enhanced hover effects
        MouseAdapter hoverEffect = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                JComponent component = (JComponent) e.getComponent();
                component.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GLOW_COLOR, 2),
                        BorderFactory.createEmptyBorder(8, 13, 8, 13)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                JComponent component = (JComponent) e.getComponent();
                component.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                        BorderFactory.createEmptyBorder(9, 14, 9, 14)
                ));
            }
        };

        emailField.addMouseListener(hoverEffect);
        passwordField.addMouseListener(hoverEffect);

        // Login button action
        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                showErrorMessage("SYSTEM ACCESS DENIED", "Please provide all required credentials.");
                return;
            }

            if (validateUser(email, password)) {
                showSuccessMessage("ACCESS GRANTED", "Welcome to CareerLens System");
            } else {
                showErrorMessage("ACCESS DENIED", "Invalid credentials provided.");
            }
        });

        formPanel.add(emailField);
        formPanel.add(passwordField);
        formPanel.add(loginButton);

        // Enhanced close button
        JButton closeButton = createCloseButton();

        // Back button
        JButton backButton = createBackButton();

        // Layout assembly
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(closeButton, BorderLayout.EAST);
        topPanel.add(backButton, BorderLayout.WEST);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(logoPanel, BorderLayout.CENTER);
        mainPanel.add(formPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Add window drag support
        addWindowDragSupport(mainPanel);
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background with gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, FIELD_BACKGROUND,
                        0, getHeight(), new Color(28, 33, 40)
                );
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));

                super.paintComponent(g);
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

                GradientPaint gradient = new GradientPaint(
                        0, 0, FIELD_BACKGROUND,
                        0, getHeight(), new Color(28, 33, 40)
                );
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));

                super.paintComponent(g);
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
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)
        ));
        field.putClientProperty("JTextField.placeholderText", placeholder);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Button gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, ACCENT_COLOR,
                        getWidth(), getHeight(), GLOW_COLOR
                );
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));

                // Text with glow effect
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();

                // Draw glow
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.drawString(getText(), x + 1, y + 1);

                // Draw text
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
        button.setPreferredSize(new Dimension(200, 45));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setFont(button.getFont().deriveFont(Font.BOLD, 17.0f));
            }
            public void mouseExited(MouseEvent e) {
                button.setFont(button.getFont().deriveFont(Font.BOLD, 16.0f));
            }
        });

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
        button.setPreferredSize(new Dimension(30, 30));
        button.setHorizontalAlignment(SwingConstants.RIGHT);

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setForeground(GLOW_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                button.setForeground(TEXT_COLOR);
            }
        });

        button.addActionListener(e -> {
            // Fade out animation before closing
            Timer fadeTimer = new Timer(20, new ActionListener() {
                float opacity = 1.0f;
                @Override
                public void actionPerformed(ActionEvent e) {
                    opacity -= 0.05f;
                    if (opacity <= 0) {
                        opacity = 0f; // Ensure opacity does not go below 0.0f
                        ((Timer)e.getSource()).stop();
                        dispose();
                        System.exit(0);
                    }
                    setOpacity(opacity);
                }
            });
            fadeTimer.start();
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
        button.setPreferredSize(new Dimension(30, 30));
        button.setHorizontalAlignment(SwingConstants.LEFT);

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setForeground(GLOW_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                button.setForeground(TEXT_COLOR);
            }
        });

        button.addActionListener(e -> {
            // Fade out animation before closing
            Timer fadeTimer = new Timer(20, new ActionListener() {
                float opacity = 1.0f;
                @Override
                public void actionPerformed(ActionEvent e) {
                    opacity -= 0.05f;
                    if (opacity <= 0) {
                        opacity = 0f; // Ensure opacity does not go below 0.0f
                        ((Timer)e.getSource()).stop();
                        dispose();
                        openWelcomePage();
                    }
                    setOpacity(opacity);
                }
            });
            fadeTimer.start();
        });

        return button;
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
                Point p = getLocation();
                setLocation(p.x + e.getX() - offset.x, p.y + e.getY() - offset.y);
            }
        });
    }

    private void showErrorMessage(String title, String message) {
        JDialog dialog = createCustomDialog(title, message, new Color(255, 69, 58));
        dialog.setVisible(true);
    }

    private void showSuccessMessage(String title, String message) {
        JDialog dialog = createCustomDialog(title, message, new Color(46, 204, 113));
        dialog.setVisible(true);

        // Close the dialog after a short delay and launch CareerLensGUI
        Timer timer = new Timer(500, e -> {
            dialog.dispose();
            setOpacity(1.0f); // Reset opacity to fully opaque
            dispose(); // Close the login window

            // Set DPI scaling
            System.setProperty("sun.java2d.uiScale", "1.0");

            // Set Look and Feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Launch CareerLensGUI on the Event Dispatch Thread (EDT)
            SwingUtilities.invokeLater(() -> {
                CareerLensGUI gui = new CareerLensGUI();
                gui.setVisible(true);
            });
        });
        timer.setRepeats(false); // Ensure the timer only runs once
        timer.start();
    }

    private JDialog createCustomDialog(String title, String message, Color accentColor) {
        JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setShape(new RoundRectangle2D.Double(0, 0, 300, 150, 15, 15));

        JPanel panel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, BACKGROUND_COLOR,
                        0, getHeight(), new Color(23, 32, 42)
                );
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));

                // Border
                g2d.setColor(accentColor);
                g2d.setStroke(new BasicStroke(2f));
                g2d.draw(new RoundRectangle2D.Double(1, 1, getWidth()-2, getHeight()-2, 15, 15));
            }
        };
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Orbitron", Font.BOLD, 18));
        titleLabel.setForeground(accentColor);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Share Tech Mono", Font.PLAIN, 14));
        messageLabel.setForeground(TEXT_COLOR);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton okButton = new JButton("OK") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, accentColor,
                        getWidth(), getHeight(), accentColor.brighter()
                );
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));

                g2d.setColor(TEXT_COLOR);
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(getText(), x, y);
            }
        };
        okButton.setFont(new Font("Share Tech Mono", Font.BOLD, 14));
        okButton.setForeground(TEXT_COLOR);
        okButton.setBorderPainted(false);
        okButton.setContentAreaFilled(false);
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setPreferredSize(new Dimension(100, 30));
        okButton.addActionListener(e -> dialog.dispose());

        JPanel messagePanel = new JPanel(new BorderLayout(10, 10));
        messagePanel.setOpaque(false);
        messagePanel.add(titleLabel, BorderLayout.NORTH);
        messagePanel.add(messageLabel, BorderLayout.CENTER);
        messagePanel.add(okButton, BorderLayout.SOUTH);

        panel.add(messagePanel, BorderLayout.CENTER);
        dialog.add(panel);

        // Add fade in animation
        dialog.setOpacity(0f);
        Timer fadeInTimer = new Timer(20, new ActionListener() {
            float opacity = 0f;
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity += 0.1f;
                if (opacity >= 1f) {
                    opacity = 1f; // Ensure opacity does not exceed 1.0f
                    ((Timer)e.getSource()).stop();
                }
                dialog.setOpacity(opacity);
            }
        });
        fadeInTimer.start();

        return dialog;
    }

    private boolean validateUser(String email, String password) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "SELECT mot_de_passe FROM user WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("mot_de_passe");
                return BCrypt.checkpw(password, hashedPassword);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorMessage("DATABASE ERROR", ex.getMessage());
        }
        return false;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginPage loginPage = new LoginPage();
            loginPage.setOpacity(0f);
            loginPage.setVisible(true);

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
                    loginPage.setOpacity(opacity);
                }
            });
            fadeInTimer.start();
        });
    }
}