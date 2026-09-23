package co.edu.unicauca.microkernel.pipeline.filters;

import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.support.TestRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Filtro CaseContextValidationFilter")
class CaseContextValidationFilterTest {

    private final CaseContextValidationFilter filter = new CaseContextValidationFilter();

    @Test
    @DisplayName("Acepta un caso con información suficiente")
    void aceptaCasoValido() {
        QuestionRequest request = TestRequests.validCase();
        assertEquals(request.getContext(), filter.process(request).getContext());
    }

    @Test
    @DisplayName("Rechaza una pregunta de caso sin contexto")
    void rechazaSinContexto() {
        QuestionRequest request = TestRequests.validCase().toBuilder().context(null).build();
        QuestionValidationException ex = assertThrows(QuestionValidationException.class, () -> filter.process(request));
        assertEquals("CaseContextValidationFilter", ex.getFilterName());
    }

    @Test
    @DisplayName("Rechaza un caso demasiado corto")
    void rechazaCasoCorto() {
        QuestionRequest request = TestRequests.validCase().toBuilder().context("Caso muy corto.").build();
        assertThrows(QuestionValidationException.class, () -> filter.process(request));
    }

    @Test
    @DisplayName("Rechaza un enunciado igual al caso")
    void rechazaEnunciadoIgualAlCaso() {
        QuestionRequest base = TestRequests.validCase();
        QuestionRequest request = base.toBuilder().content(base.getContext()).build();
        assertThrows(QuestionValidationException.class, () -> filter.process(request));
    }
}
