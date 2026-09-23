package co.edu.unicauca.microkernel.pipeline.filters;

import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.support.TestRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Filtro CorrectAnswerValidationFilter")
class CorrectAnswerValidationFilterTest {

    private final CorrectAnswerValidationFilter filter = new CorrectAnswerValidationFilter();

    private static QuestionRequest withAnswer(String answer) {
        return TestRequests.validMultipleChoice().toBuilder().correctAnswer(answer).build();
    }

    @Test
    @DisplayName("Acepta una respuesta correcta que pertenece a las opciones")
    void aceptaRespuestaValida() {
        assertEquals("Single Responsibility", filter.process(TestRequests.validMultipleChoice()).getCorrectAnswer());
    }

    @Test
    @DisplayName("Deja la respuesta escrita exactamente como la opción")
    void normalizaRespuesta() {
        assertEquals("Single Responsibility", filter.process(withAnswer("  single responsibility ")).getCorrectAnswer());
    }

    @Test
    @DisplayName("Rechaza una respuesta vacía")
    void rechazaRespuestaVacia() {
        QuestionValidationException ex = assertThrows(QuestionValidationException.class,
                () -> filter.process(withAnswer("")));
        assertEquals("CorrectAnswerValidationFilter", ex.getFilterName());
        assertThrows(QuestionValidationException.class, () -> filter.process(withAnswer(null)));
    }

    @Test
    @DisplayName("Rechaza una respuesta que no está entre las opciones")
    void rechazaRespuestaFueraDeOpciones() {
        QuestionValidationException ex = assertThrows(QuestionValidationException.class,
                () -> filter.process(withAnswer("Dependency Inversion")));
        assertTrue(ex.getReason().contains("no corresponde"));
    }

    @Test
    @DisplayName("Rechaza la solicitud si no hay opciones contra las cuales validar")
    void rechazaSinOpciones() {
        QuestionRequest request = TestRequests.validMultipleChoice().toBuilder().options(null).build();
        assertThrows(QuestionValidationException.class, () -> filter.process(request));
    }

    @Test
    @DisplayName("Rechaza una respuesta que coincide con más de una opción (consistencia)")
    void rechazaRespuestaAmbigua() {
        QuestionRequest request = TestRequests.validMultipleChoice().toBuilder()
                .options(List.of("MVC", "mvc", "Capas", "Microkernel"))
                .correctAnswer("MVC")
                .build();

        QuestionValidationException ex = assertThrows(QuestionValidationException.class, () -> filter.process(request));
        assertTrue(ex.getReason().contains("única respuesta"));
    }
}
