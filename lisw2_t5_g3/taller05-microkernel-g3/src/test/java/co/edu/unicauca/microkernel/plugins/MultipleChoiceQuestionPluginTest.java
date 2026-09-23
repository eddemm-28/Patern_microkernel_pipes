package co.edu.unicauca.microkernel.plugins;

import co.edu.unicauca.microkernel.common.entities.Question;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.support.TestRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Plugin MultipleChoiceQuestionPlugin")
class MultipleChoiceQuestionPluginTest {

    private final MultipleChoiceQuestionPlugin plugin = new MultipleChoiceQuestionPlugin();

    @Test
    @DisplayName("Cumple el contrato: nombre y tipos soportados")
    void cumpleElContrato() {
        assertEquals("multiple-choice", plugin.getName());
        assertTrue(plugin.supports("MULTIPLE_CHOICE"));
        assertTrue(plugin.supports("multiple_choice"));
        assertFalse(plugin.supports("CASE"));
        assertFalse(plugin.supports(null));
    }

    @Test
    @DisplayName("Su pipeline tiene los cuatro filtros del taller, en orden")
    void pipelineConLosCuatroFiltros() {
        List<String> filtros = plugin.getPipeline().getFilters().stream()
                .map(filter -> filter.getName())
                .collect(java.util.stream.Collectors.toList());

        assertEquals(List.of("ContentValidationFilter", "OptionsValidationFilter",
                "ClassificationFilter", "CorrectAnswerValidationFilter"), filtros);
    }

    @Test
    @DisplayName("Genera la pregunta con los datos que produjo el pipeline")
    void generaPreguntaValida() {
        Question question = plugin.generate(TestRequests.validMultipleChoice());

        assertNotNull(question.getId());
        assertEquals("MULTIPLE_CHOICE", question.getType());
        assertEquals("multiple-choice", question.getGeneratedBy());
        assertEquals("Diseño de software", question.getCompetency());
        assertEquals("Single Responsibility", question.getCorrectAnswer());
        assertEquals(4, question.getOptions().size());
        assertEquals(4, question.getProcessingTrace().size());
    }

    @Test
    @DisplayName("Cada pregunta generada tiene un identificador único")
    void generaIdentificadoresUnicos() {
        Question first = plugin.generate(TestRequests.validMultipleChoice());
        Question second = plugin.generate(TestRequests.validMultipleChoice());
        assertFalse(first.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("Rechaza una solicitud inválida indicando el filtro que falló")
    void rechazaSolicitudInvalida() {
        QuestionValidationException ex = assertThrows(QuestionValidationException.class,
                () -> plugin.generate(TestRequests.validMultipleChoice().toBuilder()
                        .options(List.of("Uno", "Dos", "Tres")).build()));

        assertEquals("OptionsValidationFilter", ex.getFilterName());
        assertFalse(ex.getTrace().isEmpty());
    }
}
