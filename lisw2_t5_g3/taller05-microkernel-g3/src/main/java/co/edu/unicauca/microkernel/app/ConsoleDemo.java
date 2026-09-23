package co.edu.unicauca.microkernel.app;

import co.edu.unicauca.microkernel.common.entities.Question;
import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.core.PluginDescriptor;
import co.edu.unicauca.microkernel.core.QuestionMicrokernel;
import co.edu.unicauca.microkernel.presentation.ExampleQuestions;
import co.edu.unicauca.microkernel.presentation.QuestionFormatter;

import java.io.PrintStream;
import java.util.Arrays;

/**
 * Demostración por consola del microkernel (equivalente al Main de la guía del taller).
 */
public class ConsoleDemo {

    private final QuestionMicrokernel microkernel;
    private final PrintStream out;

    public ConsoleDemo(QuestionMicrokernel microkernel, PrintStream out) {
        this.microkernel = microkernel;
        this.out = out;
    }

    public void run() {
        title("PLUGINS CARGADOS POR REFLEXIÓN");
        for (PluginDescriptor descriptor : microkernel.getPluginDescriptors()) {
            out.println(" - " + descriptor.getKey() + " -> " + descriptor.getClassName()
                    + " [" + descriptor.getState().getLabel() + "]");
        }

        // Ejemplo de la guía del taller
        QuestionRequest requestValida = new QuestionRequest(
                "Pregunta SOLID",
                "¿Qué representa la S en SOLID?",
                "MULTIPLE_CHOICE",
                "Arquitectura de software",
                Arrays.asList("Single Responsibility", "Open Closed", "Liskov", "Interface Segregation"),
                "Single Responsibility"
        );
        execute("1) Pregunta de selección múltiple válida", "MULTIPLE_CHOICE", requestValida);
        execute("2) Pregunta de análisis de caso válida", "CASE", ExampleQuestions.validCase());
        execute("3) Pregunta multimedia válida", "MULTIMEDIA", ExampleQuestions.validMultimedia());

        int number = 4;
        for (ExampleQuestions.Scenario scenario : ExampleQuestions.invalidScenarios()) {
            execute(number++ + ") " + scenario.getDescription(), "MULTIPLE_CHOICE", scenario.getRequest());
        }
        execute(number + ") Tipo sin plugin registrado", "TRUE_FALSE", requestValida);

        title("BANCO DE PREGUNTAS (" + microkernel.getQuestionCount() + " preguntas)");
        for (Question question : microkernel.getQuestions().values()) {
            out.println(QuestionFormatter.format(question));
            out.println("------------------------------------------------------------");
        }
    }

    private void execute(String description, String type, QuestionRequest request) {
        title(description);
        try {
            Question question = microkernel.executePlugin(type, request);
            question.getProcessingTrace().forEach(step -> out.println("   " + step));
            out.println("Pregunta agregada exitosamente al banco con ID: " + question.getId());
        } catch (QuestionValidationException ex) {
            ex.getTrace().forEach(step -> out.println("   " + step));
            out.println("La validación falló en el filtro: " + ex.getFilterName());
        } catch (IllegalArgumentException ex) {
            out.println("Error: " + ex.getMessage());
        }
    }

    private void title(String text) {
        out.println();
        out.println("=== " + text + " ===");
    }
}
