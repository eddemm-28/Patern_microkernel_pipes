package co.edu.unicauca.microkernel.plugins;

import co.edu.unicauca.microkernel.common.entities.Question;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.support.TestRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Plugin CaseQuestionPlugin")
class CaseQuestionPluginTest {

    private final CaseQuestionPlugin plugin = new CaseQuestionPlugin();

    @Test
    @DisplayName("Cumple el contrato y solo atiende preguntas de caso")
    void cumpleElContrato() {
        assertEquals("case-analysis", plugin.getName());
        assertTrue(plugin.supports("CASE"));
        assertFalse(plugin.supports("MULTIPLE_CHOICE"));
    }

    @Test
    @DisplayName("Reutiliza los cuatro filtros estándar y agrega la validación del caso")
    void reutilizaLosFiltrosEstandar() {
        assertEquals(5, plugin.getPipeline().getFilters().size());
        assertTrue(plugin.getPipeline().getFilters().stream()
                .anyMatch(filter -> "CaseContextValidationFilter".equals(filter.getName())));
    }

    @Test
    @DisplayName("Genera la pregunta conservando el caso")
    void generaPreguntaConCaso() {
        Question question = plugin.generate(TestRequests.validCase());

        assertEquals("CASE", question.getType());
        assertEquals("case-analysis", question.getGeneratedBy());
        assertTrue(question.getContext().startsWith("Una universidad"));
        assertEquals(5, question.getProcessingTrace().size());
    }

    @Test
    @DisplayName("Rechaza una pregunta de caso sin la descripción del caso")
    void rechazaCasoSinContexto() {
        QuestionValidationException ex = assertThrows(QuestionValidationException.class,
                () -> plugin.generate(TestRequests.validCase().toBuilder().context(null).build()));

        assertEquals("CaseContextValidationFilter", ex.getFilterName());
    }
}
