package co.edu.unicauca.microkernel.common;

import co.edu.unicauca.microkernel.common.entities.DifficultyLevel;
import co.edu.unicauca.microkernel.common.entities.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Entidad Question")
class QuestionTest {

    @Test
    @DisplayName("El constructor de la guía asigna id, título, contenido y tipo")
    void constructorAsignaDatosBasicos() {
        Question question = new Question("1", "Pregunta SOLID", "¿Qué representa la S?", "MULTIPLE_CHOICE");

        assertEquals("1", question.getId());
        assertEquals("Pregunta SOLID", question.getTitle());
        assertEquals("¿Qué representa la S?", question.getContent());
        assertEquals("MULTIPLE_CHOICE", question.getType());
        assertTrue(question.getOptions().isEmpty());
        assertTrue(question.getProcessingTrace().isEmpty());
        assertNotNull(question.getCreatedAt());
    }

    @Test
    @DisplayName("El builder asigna todos los datos de la pregunta")
    void builderAsignaTodosLosDatos() {
        Question question = Question.builder("2", "Título", "Contenido", "CASE")
                .classification("Arquitectura de software")
                .competency("Diseño de software")
                .category("Específica")
                .difficulty(DifficultyLevel.ALTO)
                .options(List.of("A", "B", "C", "D"))
                .correctAnswer("A")
                .context("Caso")
                .mediaType("IMAGEN")
                .mediaUrl("imagen.png")
                .justification("Porque sí")
                .generatedBy("case-analysis")
                .processingTrace(List.of("[OK] Filtro"))
                .build();

        assertEquals("Arquitectura de software", question.getClassification());
        assertEquals("Diseño de software", question.getCompetency());
        assertEquals("Específica", question.getCategory());
        assertEquals(DifficultyLevel.ALTO, question.getDifficulty());
        assertEquals(4, question.getOptions().size());
        assertEquals("A", question.getCorrectAnswer());
        assertEquals("Caso", question.getContext());
        assertEquals("IMAGEN", question.getMediaType());
        assertEquals("imagen.png", question.getMediaUrl());
        assertEquals("Porque sí", question.getJustification());
        assertEquals("case-analysis", question.getGeneratedBy());
        assertEquals(1, question.getProcessingTrace().size());
    }

    @Test
    @DisplayName("Las opciones de la pregunta no se pueden modificar desde afuera")
    void opcionesSonInmutables() {
        List<String> options = new ArrayList<>(List.of("A", "B", "C", "D"));
        Question question = Question.builder("3", "T", "C", "MULTIPLE_CHOICE").options(options).build();

        options.add("E");

        assertEquals(4, question.getOptions().size());
        assertThrows(UnsupportedOperationException.class, () -> question.getOptions().add("F"));
    }

    @Test
    @DisplayName("El id es obligatorio")
    void idObligatorio() {
        assertThrows(NullPointerException.class, () -> new Question(null, "T", "C", "MULTIPLE_CHOICE"));
    }

    @Test
    @DisplayName("Dos preguntas son iguales si tienen el mismo id")
    void igualdadPorId() {
        Question first = new Question("10", "Uno", "Contenido uno", "MULTIPLE_CHOICE");
        Question same = new Question("10", "Otro", "Contenido otro", "CASE");
        Question other = new Question("11", "Uno", "Contenido uno", "MULTIPLE_CHOICE");

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, other);
    }
}
