package co.edu.unicauca.microkernel.support;

import co.edu.unicauca.microkernel.common.entities.QuestionRequest;

import java.util.List;

/**
 * Solicitudes de prueba reutilizadas por las pruebas unitarias.
 */
public final class TestRequests {

    public static final List<String> SOLID_OPTIONS =
            List.of("Single Responsibility", "Open Closed", "Liskov", "Interface Segregation");

    private TestRequests() {
    }

    /** Solicitud de selección múltiple válida (ejemplo de la guía del taller). */
    public static QuestionRequest validMultipleChoice() {
        return new QuestionRequest(
                "Pregunta SOLID",
                "¿Qué representa la S en SOLID?",
                "MULTIPLE_CHOICE",
                "Arquitectura de software",
                SOLID_OPTIONS,
                "Single Responsibility");
    }

    /** Solicitud de análisis de caso válida. */
    public static QuestionRequest validCase() {
        return QuestionRequest.builder()
                .type("CASE")
                .title("Caso de arquitectura extensible")
                .context("Una universidad necesita agregar cada semestre nuevos tipos de preguntas a su banco "
                        + "Saber PRO sin modificar ni volver a probar el núcleo del sistema.")
                .content("¿Qué estilo arquitectónico es el más adecuado?")
                .classification("Arquitectura de software")
                .options(List.of("Microkernel", "Monolito sin extensiones", "Cliente-servidor", "Peer to peer"))
                .correctAnswer("Microkernel")
                .build();
    }

    /** Solicitud multimedia válida. */
    public static QuestionRequest validMultimedia() {
        return QuestionRequest.builder()
                .type("MULTIMEDIA")
                .title("Diagrama de clases")
                .content("Observe el diagrama. ¿Qué relación existe entre Pedido y LineaPedido?")
                .classification("Ingeniería de software")
                .mediaType("IMAGEN")
                .mediaUrl("https://recursos.unicauca.edu.co/diagrama.png")
                .options(List.of("Composición", "Herencia", "Dependencia", "Realización"))
                .correctAnswer("Composición")
                .build();
    }
}
