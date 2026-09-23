package co.edu.unicauca.microkernel.pipeline;

import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.pipeline.base.PipelineResult;
import co.edu.unicauca.microkernel.pipeline.base.QuestionFilter;
import co.edu.unicauca.microkernel.pipeline.base.QuestionPipeline;
import co.edu.unicauca.microkernel.pipeline.filters.ClassificationFilter;
import co.edu.unicauca.microkernel.pipeline.filters.ContentValidationFilter;
import co.edu.unicauca.microkernel.pipeline.filters.CorrectAnswerValidationFilter;
import co.edu.unicauca.microkernel.pipeline.filters.OptionsValidationFilter;
import co.edu.unicauca.microkernel.support.TestRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tubería QuestionPipeline")
class QuestionPipelineTest {

    /** Pipeline estándar del taller, con los cuatro filtros requeridos. */
    private static QuestionPipeline standardPipeline() {
        return new QuestionPipeline()
                .addFilter(new ContentValidationFilter())
                .addFilter(new OptionsValidationFilter())
                .addFilter(new ClassificationFilter())
                .addFilter(new CorrectAnswerValidationFilter());
    }

    @Test
    @DisplayName("Una solicitud válida pasa por los cuatro filtros y sale enriquecida")
    void solicitudValidaPasaTodosLosFiltros() {
        PipelineResult result = standardPipeline().execute(TestRequests.validMultipleChoice());

        assertTrue(result.isValid());
        assertNull(result.getFailedFilter());
        assertEquals(4, result.getTrace().size());
        assertEquals("Diseño de software", result.getOutput().getCompetency());
        assertNotNull(result.getOutput().getDifficulty());
    }

    @Test
    @DisplayName("El pipeline se detiene en el primer filtro que rechaza la solicitud")
    void seDetieneEnElPrimerFallo() {
        QuestionRequest request = TestRequests.validMultipleChoice().toBuilder()
                .options(List.of("Uno", "Dos", "Tres", "Todas las anteriores"))
                .build();

        PipelineResult result = standardPipeline().execute(request);

        assertFalse(result.isValid());
        assertEquals("OptionsValidationFilter", result.getFailedFilter());
        assertEquals(2, result.getTrace().size());
        assertTrue(result.getTrace().get(1).startsWith(QuestionPipeline.FAIL_MARK));
    }

    @Test
    @DisplayName("Los filtros se ejecutan en el orden en que se agregaron")
    void ejecutaLosFiltrosEnOrden() {
        List<String> order = new ArrayList<>();
        QuestionPipeline pipeline = new QuestionPipeline()
                .addFilter(request -> {
                    order.add("primero");
                    return request;
                })
                .addFilter(request -> {
                    order.add("segundo");
                    return request;
                });

        pipeline.execute(TestRequests.validMultipleChoice());

        assertEquals(List.of("primero", "segundo"), order);
        assertEquals(2, pipeline.getFilters().size());
    }

    @Test
    @DisplayName("La salida de un filtro es la entrada del siguiente")
    void transportaElResultadoDeUnFiltroAlSiguiente() {
        QuestionPipeline pipeline = new QuestionPipeline()
                .addFilter(request -> request.toBuilder().title("Título cambiado").build())
                .addFilter(request -> request.toBuilder().content(request.getTitle() + " | " + request.getContent()).build());

        PipelineResult result = pipeline.execute(TestRequests.validMultipleChoice());

        assertTrue(result.isValid());
        assertEquals("Título cambiado", result.getOutput().getTitle());
        assertTrue(result.getOutput().getContent().startsWith("Título cambiado | "));
    }

    @Test
    @DisplayName("Los filtros posteriores al fallo no se ejecutan")
    void noEjecutaLosFiltrosPosterioresAlFallo() {
        List<String> executed = new ArrayList<>();
        QuestionPipeline pipeline = new QuestionPipeline()
                .addFilter(request -> {
                    throw new QuestionValidationException("FiltroDePrueba", "rechazo simulado");
                })
                .addFilter(request -> {
                    executed.add("segundo");
                    return request;
                });

        PipelineResult result = pipeline.execute(TestRequests.validMultipleChoice());

        assertFalse(result.isValid());
        assertTrue(executed.isEmpty());
        assertEquals("rechazo simulado", result.getErrorMessage());
    }

    @Test
    @DisplayName("Un pipeline sin filtros devuelve la solicitud sin cambios")
    void pipelineVacio() {
        QuestionRequest request = TestRequests.validMultipleChoice();
        PipelineResult result = new QuestionPipeline().execute(request);

        assertTrue(result.isValid());
        assertEquals(request, result.getOutput());
        assertTrue(result.getTrace().isEmpty());
    }

    @Test
    @DisplayName("Una solicitud nula se reporta como error de entrada")
    void solicitudNula() {
        PipelineResult result = standardPipeline().execute(null);

        assertFalse(result.isValid());
        assertEquals("Entrada", result.getFailedFilter());
    }

    @Test
    @DisplayName("Un resultado fallido se convierte en excepción con filtro, motivo y traza")
    void resultadoFallidoSeConvierteEnExcepcion() {
        PipelineResult result = standardPipeline()
                .execute(TestRequests.validMultipleChoice().toBuilder().title("").build());

        QuestionValidationException ex = result.toException();

        assertEquals("ContentValidationFilter", ex.getFilterName());
        assertEquals(result.getErrorMessage(), ex.getReason());
        assertEquals(result.getTrace(), ex.getTrace());
    }

    @Test
    @DisplayName("No se aceptan filtros nulos")
    void noAceptaFiltrosNulos() {
        QuestionPipeline pipeline = new QuestionPipeline();
        assertThrows(NullPointerException.class, () -> pipeline.addFilter((QuestionFilter) null));
    }
}
