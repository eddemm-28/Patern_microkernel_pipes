package co.edu.unicauca.microkernel.app;

import co.edu.unicauca.microkernel.core.QuestionMicrokernel;
import co.edu.unicauca.microkernel.presentation.MainWindow;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

/**
 * Punto de entrada de la aplicación.
 * <p>
 * Por defecto abre la interfaz de escritorio en Swing. Con el argumento
 * {@code --consola} (o en un entorno sin pantalla) ejecuta la demostración por consola.
 */
public class Main {

    public static void main(String[] args) {
        QuestionMicrokernel microkernel = new QuestionMicrokernel();

        boolean consoleMode = Arrays.asList(args).contains("--consola") || GraphicsEnvironment.isHeadless();
        if (consoleMode) {
            new ConsoleDemo(microkernel, System.out).run();
            return;
        }

        SwingUtilities.invokeLater(() -> {
            applyLookAndFeel();
            new MainWindow(microkernel).setVisible(true);
        });
    }

    private static void applyLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception ex) {
            // Si Nimbus no está disponible se usa el aspecto por defecto de Swing.
        }
    }
}
