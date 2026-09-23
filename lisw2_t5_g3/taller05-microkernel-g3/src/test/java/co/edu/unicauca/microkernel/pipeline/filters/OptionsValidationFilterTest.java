package co.edu.unicauca.microkernel.pipeline.filters;

import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.support.TestRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Filtro OptionsValidationFilter")
class OptionsValidationFilterTest {

    private final OptionsValidationFilter filter = new OptionsValidationFilter();

    private static QuestionRequest withOptions(List<String> options) {
        return TestRequests.validMultipleChoice().toBuilder().options(options).build();
    }

    private QuestionValidationException rejected(List<String> options) {
        return assertThrows(QuestionValidationException.class, () -> filter.process(withOptions(options)));
    }

    @Test
    @DisplayName("Acepta exactamente cuatro opciones válidas")
    void aceptaCuatroOpciones() {
        QuestionRequest output = filter.process(TestRequests.validMultipleChoice());
        assertEquals(TestRequests.SOLID_OPTIONS, output.getOptions());
    }

    @Test
    @DisplayName("Rechaza una pregunta sin opciones")
    void rechazaSinOpciones() {
        QuestionValidationException ex = assertThrows(QuestionValidationException.class,
                () -> filter.process(withOptions(null)));
        assertEquals("OptionsValidationFilter", ex.getFilterName());
    }

    @Test
    @DisplayName("Rechaza menos o más de cuatro opciones")
    void rechazaCantidadIncorrecta() {
        assertTrue(rejected(List.of("A1", "B1", "C1")).getReason().contains("exactamente 4"));
        assertTrue(rejected(List.of("A1", "B1", "C1", "D1", "E1")).getReason().contains("tiene 5"));
    }

    @Test
    @DisplayName("Rechaza opciones vacías o nulas")
    void rechazaOpcionVacia() {
        assertTrue(rejected(List.of("Uno", "  ", "Tres", "Cuatro")).getReason().contains("opción B"));
        assertTrue(rejected(Arrays.asList("Uno", "Dos", "Tres", null)).getReason().contains("opción D"));
    }

    @Test
    @DisplayName("Rechaza opciones duplicadas aunque cambien mayúsculas, tildes o espacios")
    void rechazaDuplicadas() {
        QuestionValidationException ex = rejected(List.of("Composición", "Herencia", " composicion ", "Realización"));
        assertTrue(ex.getReason().contains("A y C"));
    }

    @Test
    @DisplayName("Rechaza las expresiones 'Todas las anteriores' y 'Ninguna de las anteriores'")
    void rechazaExpresionesProhibidas() {
        rejected(List.of("Uno", "Dos", "Tres", "Todas las anteriores"));
        rejected(List.of("Uno", "Dos", "Tres", "NINGUNA DE LAS ANTERIORES"));
    }

    @Test
    @DisplayName("Rechaza opciones que superan la longitud máxima")
    void rechazaOpcionLarga() {
        String larga = "x".repeat(OptionsValidationFilter.MAX_OPTION_LENGTH + 1);
        rejected(List.of("Uno", "Dos", "Tres", larga));
    }

    @Test
    @DisplayName("Elimina los espacios sobrantes de cada opción")
    void normalizaOpciones() {
        QuestionRequest output = filter.process(withOptions(List.of("  Uno ", "Dos   opciones", "Tres", "Cuatro")));
        assertEquals(List.of("Uno", "Dos opciones", "Tres", "Cuatro"), output.getOptions());
    }
}
