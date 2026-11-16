
package StudentManagementSystem;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {
                // Use the system's native appearance
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
                );

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Open the Student Management System
            StudentManagementSystem frame =
                    new StudentManagementSystem();

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
