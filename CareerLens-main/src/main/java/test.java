import javax.swing.*;

public class test {
    public static void main(String[] args) {
        // Fixer l'échelle DPI
        System.setProperty("sun.java2d.uiScale", "1.0");

        // Appliquer le LookAndFeel
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
