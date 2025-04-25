import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.Timer;
import javax.swing.border.*;

public class WelcomePage extends JFrame {
    private static final Color DARK_PRIMARY = new Color(32, 33, 36);
    private static final Color DARK_SECONDARY = new Color(41, 42, 45);
    private static final Color ACCENT_1 = new Color(64, 169, 255);
    private static final Color ACCENT_2 = new Color(130, 87, 229);
    private static final Color GLASS_WHITE = new Color(255, 255, 255, 25);
    private static final Color TEXT_COLOR = new Color(255, 255, 255);
    private static final Color PARTICLE_COLOR = new Color(255, 255, 255, 50);

    private ArrayList<Particle> particles;
    private Timer particleTimer;

    public WelcomePage() {
        setTitle("Welcome to CareerLens");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true); // Remove default window decorations
        setShape(new RoundRectangle2D.Double(0, 0, 1000, 600, 20, 20)); // Rounded corners

        // Initialize particles
        particles = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle());
        }

        // Main panel with gradient background and particles
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, DARK_PRIMARY, getWidth(), getHeight(), DARK_SECONDARY);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw particles
                for (Particle p : particles) {
                    g2d.setColor(PARTICLE_COLOR);
                    g2d.fill(new Ellipse2D.Double(p.x, p.y, p.size, p.size));
                }

                // Glassmorphism effect
                g2d.setColor(GLASS_WHITE);
                g2d.fillRoundRect(50, 50, getWidth() - 100, getHeight() - 100, 20, 20);
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        // Title label
        JLabel titleLabel = new JLabel("Welcome to CareerLens", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 36));
        titleLabel.setForeground(TEXT_COLOR);

        // Authors label
        JLabel authorsLabel = new JLabel("Réalisé par: Berraho Khalil, Akestaf Ahmed, Souiles Aymane, Taouab Rim", SwingConstants.CENTER);
        authorsLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        authorsLabel.setForeground(Color.GRAY);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setOpaque(false);

        // Login button
        JButton loginButton = createStyledButton("Log In", ACCENT_1);
        loginButton.addActionListener(e -> {
            setVisible(false); // Hide the WelcomePage
            new LoginPage().setVisible(true); // Launch the LoginPage
        });

        // Sign Up button
        JButton signUpButton = createStyledButton("Sign Up", ACCENT_2);
        signUpButton.addActionListener(e -> {
            setVisible(false); // Hide the WelcomePage
            new SignUpPage().setVisible(true); // Launch the SignUpPage
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(signUpButton);

        // South panel
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(authorsLabel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Center panel with decorative elements
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        // Add a decorative icon or image
        JLabel decorativeIcon = new JLabel(new ImageIcon("C:\\Users\\pc\\Downloads\\white_lens-removebg-preview.png")); // Replace with your icon path
        decorativeIcon.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(decorativeIcon);

        // Add components to main panel
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        setContentPane(mainPanel);

        // Add window drag support
        addWindowDragSupport(mainPanel);

        // Start particle animation
        startParticleAnimation();
    }

    // Helper method to create styled buttons
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, color, getWidth(), getHeight(), color.darker());
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Draw text
                g2d.setColor(TEXT_COLOR);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);
            }
        };

        button.setFont(new Font("Montserrat", Font.PLAIN, 18));
        button.setForeground(TEXT_COLOR);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    // Helper method to add window drag support
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

    // Particle class for animation
    private class Particle {
        double x, y;
        double speedX, speedY;
        double size;
        double opacity;

        Particle() {
            Random random = new Random();
            x = random.nextDouble() * getWidth();
            y = random.nextDouble() * getHeight();
            speedX = (random.nextDouble() - 0.5) * 2;
            speedY = (random.nextDouble() - 0.5) * 2;
            size = 2 + random.nextDouble() * 4;
            opacity = 0.3 + random.nextDouble() * 0.7;
        }

        void update() {
            x += speedX;
            y += speedY;
            opacity -= 0.005;

            if (x < 0 || x > getWidth() || y < 0 || y > getHeight() || opacity <= 0) {
                Random random = new Random();
                x = random.nextDouble() * getWidth();
                y = random.nextDouble() * getHeight();
                opacity = 0.3 + random.nextDouble() * 0.7;
            }
        }
    }

    // Start particle animation
    private void startParticleAnimation() {
        particleTimer = new Timer(16, e -> {
            for (Particle p : particles) {
                p.update();
            }
            repaint();
        });
        particleTimer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WelcomePage welcome = new WelcomePage();
            welcome.setVisible(true);
        });
    }
}