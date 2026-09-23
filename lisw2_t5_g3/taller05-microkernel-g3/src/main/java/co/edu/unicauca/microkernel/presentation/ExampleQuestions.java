package co.edu.unicauca.microkernel.presentation;

import co.edu.unicauca.microkernel.common.entities.QuestionRequest;

import java.util.List;

/**
 * Preguntas de ejemplo para probar la aplicación desde la interfaz:
 * una válida por cada tipo y un escenario inválido por cada filtro del pipeline.
 */
public final class ExampleQuestions {

    public static final String MULTIPLE_CHOICE = "MULTIPLE_CHOICE";
    public static final String CASE = "CASE";
    public static final String MULTIMEDIA = "MULTIMEDIA";

    private ExampleQuestions() {
    }

    /** Pregunta válida para el tipo indicado. */
    public static QuestionRequest valid(String type) {
        if (CASE.equalsIgnoreCase(type)) {
            return validCase();
        }
        if (MULTIMEDIA.equalsIgnoreCase(type)) {
            return validMultimedia();
        }
        return validMultipleChoice();
    }

    public static QuestionRequest validMultipleChoice() {
        return QuestionRequest.builder()
                .type(MULTIPLE_CHOICE)
                .title("Pregunta SOLID")
                .content("¿Qué representa la S en los principios SOLID?")
                .classification("Arquitectura de software")
                .options(List.of("Single Responsibility", "Open Closed", "Liskov Substitution", "Interface Segregation"))
                .correctAnswer("Single Responsibility")
                .justification("La S corresponde al principio de responsabilidad única: una clase debe tener "
                        + "una sola razón para cambiar.")
                .build();
    }

    public static QuestionRequest validCase() {
        return QuestionRequest.builder()
                .type(CASE)
                .title("Estilo arquitectónico para un banco de preguntas extensible")
                .context("Una universidad desarrolla un banco de preguntas para preparar las pruebas Saber PRO. "
                        + "Cada semestre aparecen nuevos tipos de preguntas (análisis de casos, multimedia, "
                        + "generadas con inteligencia artificial) y el equipo quiere incorporarlos sin modificar "
                        + "ni volver a probar el núcleo del sistema, que ya es estable.")
                .content("¿Qué estilo arquitectónico responde mejor a esta necesidad?")
                .classification("Arquitectura de software")
                .options(List.of(
                        "Microkernel con plugins registrados en configuración",
                        "Monolito en capas sin puntos de extensión",
                        "Cliente-servidor con una base de datos compartida",
                        "Tuberías y filtros para toda la aplicación"))
                .correctAnswer("Microkernel con plugins registrados en configuración")
                .justification("El microkernel permite agregar funcionalidades como plugins que cumplen un "
                        + "contrato común, sin cambiar el núcleo.")
                .build();
    }

    public static QuestionRequest validMultimedia() {
        return QuestionRequest.builder()
                .type(MULTIMEDIA)
                .title("Interpretación de un diagrama de clases")
                .content("Observe el diagrama UML del recurso adjunto. ¿Qué relación existe entre las clases "
                        + "Pedido y LineaPedido?")
                .classification("Ingeniería de software")
                .mediaType("IMAGEN")
                .mediaUrl("https://recursos.unicauca.edu.co/saberpro/diagrama-pedido.png")
                .options(List.of("Composición", "Herencia", "Dependencia", "Realización"))
                .correctAnswer("Composición")
                .justification("Una línea de pedido no existe sin su pedido: su ciclo de vida depende del todo.")
                .build();
    }

    /** Escenarios inválidos: cada uno es rechazado por un filtro distinto del pipeline. */
    public static List<Scenario> invalidScenarios() {
        QuestionRequest base = validMultipleChoice();
        return List.of(
                new Scenario("Título vacío (lo rechaza ContentValidationFilter)",
                        base.toBuilder().title("   ").build()),
                new Scenario("Opción \"Todas las anteriores\" (lo rechaza OptionsValidationFilter)",
                        base.toBuilder()
                                .options(List.of("Single Responsibility", "Open Closed", "Liskov Substitution",
                                        "Todas las anteriores"))
                                .build()),
                new Scenario("Área fuera del catálogo (lo rechaza ClassificationFilter)",
                        base.toBuilder().classification("Astrología").build()),
                new Scenario("Sin respuesta correcta (lo rechaza CorrectAnswerValidationFilter)",
                        base.toBuilder().correctAnswer("").build())
        );
    }

    /**
     * Escenario de prueba con una descripción.
     */
    public static final class Scenario {
        private final String description;
        private final QuestionRequest request;

        public Scenario(String description, QuestionRequest request) {
            this.description = description;
            this.request = request;
        }

        public String getDescription() {
            return description;
        }

        public QuestionRequest getRequest() {
            return request;
        }
    }
}
