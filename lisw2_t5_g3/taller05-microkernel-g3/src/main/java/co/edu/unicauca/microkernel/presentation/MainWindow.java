package co.edu.unicauca.microkernel.presentation;

import co.edu.unicauca.microkernel.core.QuestionMicrokernel;

import javax.swing.JFrame;
import java.awt.Dimension;

/**
 * Ventana principal (Java Desktop con Swing).
 */
public class MainWindow extends JFrame {

    public MainWindow(QuestionMicrokernel kernel) {
        super("Banco de Preguntas Saber PRO - Microkernel y Tuberías y Filtros (Grupo 3)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(new MainPanel(kernel));
        setMinimumSize(new Dimension(980, 680));
        setSize(1180, 820);
        setLocationRelativeTo(null);
    }
}
