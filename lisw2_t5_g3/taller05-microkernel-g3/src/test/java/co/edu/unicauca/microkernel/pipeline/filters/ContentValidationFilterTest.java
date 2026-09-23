package co.edu.unicauca.microkernel.pipeline.filters;

import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.support.TestRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Filtro ContentValidationFilter")
class ContentValidationFilterTest {

    private final ContentValidationFilter filter = new ContentValidationFilter();

    @Test
    @DisplayName("Acepta una pregunta con título y enunciado válidos")
    void aceptaContenidoValido() {
        QuestionRequest output = filter.process(TestRequests.validMultipleChoice());
        assertEquals("Pregunta SOLID", output.getTitle());
        assertEquals("¿Qué representa la S en SOLID?", output.getContent());
    }

    @Test
    @DisplayName("Rechaza título y enunciado vacíos (caso de la guía)")
    void rechazaContenidoVacio() {
        QuestionRequest requestInvalido = new QuestionRequest("", "", "MULTIPLE_CHOICE", "Arquitectura", null, "");

        QuestionValidationException ex = assertThrows(QuestionValidationException.class,
                () -> filter.process(requestInvalido));

        assertEquals("ContentValidationFilter", ex.getFilterName());
    }

    @Test
    @DisplayName("Rechaza un título nulo o formado solo por espacios")
    void rechazaTituloNuloOEnBlanco() {
        QuestionRequest nullTitle = TestRequests.validMultipleChoice().toBuilder().title(null).build();
        QuestionRequest blankTitle = TestRequests.validMultipleChoice().toBuilder().title("    ").build();

        assertThrows(QuestionValidationException.class, () -> filter.process(nullTitle));
        assertThrows(QuestionValidationException.class, () -> filter.process(blankTitle));
    }

    @Test
    @DisplayName("Rechaza un título más corto que la longitud mínima")
    void rechazaTituloCorto() {
        QuestionRequest request = TestRequests.validMultipleChoice().toBuilder().title("POO").build();

        QuestionValidationException ex = assertThrows(QuestionValidationException.class, () -> filter.process(request));
        assertTrue(ex.getReason().contains("al menos"));
    }

    @Test
    @DisplayName("Rechaza un enunciado más corto que la longitud mínima")
    void rechazaEnunciadoCorto() {
        QuestionRequest request = TestRequests.validMultipleChoice().toBuilder().content("¿SOLID?").build();
        assertThrows(QuestionValidationException.class, () -> filter.process(request));
    }

    @Test
    @DisplayName("Rechaza un enunciado sin letras (formato incorrecto)")
    void rechazaEnunciadoSinTexto() {
        QuestionRequest request = TestRequests.validMultipleChoice().toBuilder().content("1234567890 + 98765 = ?").build();
        assertThrows(QuestionValidationException.class, () -> filter.process(request));
    }

    @Test
    @DisplayName("Rechaza un enunciado con más de una pregunta directa")
    void rechazaVariasPreguntas() {
        QuestionRequest request = TestRequests.validMultipleChoice().toBuilder()
                .content("¿Qué es SOLID? ¿Quién lo propuso?").build();

        QuestionValidationException ex = assertThrows(QuestionValidationException.class, () -> filter.process(request));
        assertTrue(ex.getReason().contains("única pregunta"));
    }

    @Test
    @DisplayName("Elimina los espacios sobrantes del título y del enunciado")
    void normalizaEspacios() {
        QuestionRequest request = TestRequests.validMultipleChoice().toBuilder()
                .title("   Pregunta    SOLID  ")
                .content("  ¿Qué   representa la S en SOLID?  ")
                .build();

        QuestionRequest output = filter.process(request);

        assertEquals("Pregunta SOLID", output.getTitle());
        assertEquals("¿Qué representa la S en SOLID?", output.getContent());
    }
}
