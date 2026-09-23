package co.edu.unicauca.microkernel.pipeline.filters;

import co.edu.unicauca.microkernel.common.entities.DifficultyLevel;
import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.support.TestRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Filtro ClassificationFilter")
class ClassificationFilterTest {

    private final ClassificationFilter filter = new ClassificationFilter();

    private static QuestionRequest withArea(String area) {
        return TestRequests.validMultipleChoice().toBuilder().classification(area).build();
    }

    @Test
    @DisplayName("Asigna la competencia y la categoría de un área específica")
    void asignaCompetenciaEspecifica() {
        QuestionRequest output = filter.process(TestRequests.validMultipleChoice());

        assertEquals("Arquitectura de software", output.getClassification());
        assertEquals("Diseño de software", output.getCompetency());
        assertEquals(CompetencyCatalog.SPECIFIC, output.getCategory());
    }

    @Test
    @DisplayName("Reconoce el área sin importar mayúsculas, tildes ni espacios y la deja con su nombre oficial")
    void normalizaElArea() {
        QuestionRequest output = filter.process(withArea("  lectura   CRITICA "));

        assertEquals("Lectura crítica", output.getClassification());
        assertEquals("Lectura crítica", output.getCompetency());
        assertEquals(CompetencyCatalog.GENERIC, output.getCategory());
    }

    @Test
    @DisplayName("Rechaza un área que no pertenece al catálogo")
    void rechazaAreaDesconocida() {
        QuestionValidationException ex = assertThrows(QuestionValidationException.class,
                () -> filter.process(withArea("Astrología")));

        assertEquals("ClassificationFilter", ex.getFilterName());
        assertTrue(ex.getReason().contains("Astrología"));
    }

    @Test
    @DisplayName("Rechaza una clasificación vacía")
    void rechazaClasificacionVacia() {
        assertThrows(QuestionValidationException.class, () -> filter.process(withArea(" ")));
        assertThrows(QuestionValidationException.class, () -> filter.process(withArea(null)));
    }

    @Test
    @DisplayName("Respeta el nivel de dificultad indicado por el autor")
    void respetaDificultadIndicada() {
        QuestionRequest request = TestRequests.validMultipleChoice().toBuilder().difficulty(DifficultyLevel.ALTO).build();
        assertEquals(DifficultyLevel.ALTO, filter.process(request).getDifficulty());
    }

    @Test
    @DisplayName("Estima dificultad baja para una pregunta corta")
    void estimaDificultadBaja() {
        assertEquals(DifficultyLevel.BAJO, filter.process(TestRequests.validMultipleChoice()).getDifficulty());
    }

    @Test
    @DisplayName("Estima dificultad media y alta según la cantidad de texto a analizar")
    void estimaDificultadMediaYAlta() {
        String fiftyWords = "palabra ".repeat(50);
        String twoHundredWords = "palabra ".repeat(200);
        QuestionRequest medium = TestRequests.validMultipleChoice().toBuilder().context(fiftyWords).build();
        QuestionRequest high = TestRequests.validMultipleChoice().toBuilder().context(twoHundredWords).build();

        assertEquals(DifficultyLevel.MEDIO, ClassificationFilter.estimateDifficulty(medium));
        assertEquals(DifficultyLevel.ALTO, ClassificationFilter.estimateDifficulty(high));
    }

    @Test
    @DisplayName("El catálogo contiene las áreas usadas por la aplicación")
    void catalogoContieneAreas() {
        List<String> areas = CompetencyCatalog.areas();
        assertTrue(areas.contains("Arquitectura de software"));
        assertTrue(areas.contains("Inglés"));
        assertTrue(CompetencyCatalog.find("ARQUITECTURA DE SOFTWARE").isPresent());
    }
}
