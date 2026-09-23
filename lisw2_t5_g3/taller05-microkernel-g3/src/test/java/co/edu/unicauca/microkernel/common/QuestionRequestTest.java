package co.edu.unicauca.microkernel.common;

import co.edu.unicauca.microkernel.common.entities.DifficultyLevel;
import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.support.TestRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Entidad QuestionRequest")
class QuestionRequestTest {

    @Test
    @DisplayName("El constructor de la guía asigna los seis datos de la solicitud")
    void constructorDeLaGuia() {
        QuestionRequest request = TestRequests.validMultipleChoice();

        assertEquals("Pregunta SOLID", request.getTitle());
        assertEquals("¿Qué representa la S en SOLID?", request.getContent());
        assertEquals("MULTIPLE_CHOICE", request.getType());
        assertEquals("Arquitectura de software", request.getClassification());
        assertEquals(TestRequests.SOLID_OPTIONS, request.getOptions());
        assertEquals("Single Responsibility", request.getCorrectAnswer());
        assertNull(request.getDifficulty());
    }

    @Test
    @DisplayName("toBuilder crea una copia modificada sin alterar la original")
    void toBuilderNoModificaLaOriginal() {
        QuestionRequest original = TestRequests.validMultipleChoice();

        QuestionRequest copy = original.toBuilder()
                .title("Otro título")
                .difficulty(DifficultyLevel.MEDIO)
                .build();

        assertEquals("Pregunta SOLID", original.getTitle());
        assertNull(original.getDifficulty());
        assertEquals("Otro título", copy.getTitle());
        assertEquals(DifficultyLevel.MEDIO, copy.getDifficulty());
        assertEquals(original.getOptions(), copy.getOptions());
    }

    @Test
    @DisplayName("La solicitud guarda una copia de las opciones y no permite modificarlas")
    void opcionesSonUnaCopiaInmutable() {
        List<String> options = new ArrayList<>(List.of("A", "B", "C", "D"));
        QuestionRequest request = QuestionRequest.builder().options(options).build();

        options.clear();

        assertEquals(4, request.getOptions().size());
        assertThrows(UnsupportedOperationException.class, () -> request.getOptions().remove(0));
    }

    @Test
    @DisplayName("Si no se envían opciones, la lista queda nula para que el filtro lo detecte")
    void opcionesNulas() {
        QuestionRequest request = new QuestionRequest("T", "C", "MULTIPLE_CHOICE", "Área", null, "");
        assertNull(request.getOptions());
    }
}
