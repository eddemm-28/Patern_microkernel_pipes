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

@DisplayName("Plugin MultimediaQuestionPlugin")
class MultimediaQuestionPluginTest {

    private final MultimediaQuestionPlugin plugin = new MultimediaQuestionPlugin();

    @Test
    @DisplayName("Cumple el contrato y solo atiende preguntas multimedia")
    void cumpleElContrato() {
        assertEquals("multimedia", plugin.getName());
        assertTrue(plugin.supports("multimedia"));
        assertFalse(plugin.supports("CASE"));
    }

    @Test
    @DisplayName("Genera la pregunta conservando el recurso multimedia")
    void generaPreguntaConRecurso() {
        Question question = plugin.generate(TestRequests.validMultimedia());

        assertEquals("MULTIMEDIA", question.getType());
        assertEquals("IMAGEN", question.getMediaType());
        assertEquals("https://recursos.unicauca.edu.co/diagrama.png", question.getMediaUrl());
        assertEquals(5, question.getProcessingTrace().size());
    }

    @Test
    @DisplayName("Rechaza un recurso cuyo formato no corresponde al tipo")
    void rechazaRecursoInvalido() {
        QuestionValidationException ex = assertThrows(QuestionValidationException.class,
                () -> plugin.generate(TestRequests.validMultimedia().toBuilder()
                        .mediaUrl("https://sitio.edu.co/archivo.docx").build()));

        assertEquals("MediaResourceValidationFilter", ex.getFilterName());
    }
}
