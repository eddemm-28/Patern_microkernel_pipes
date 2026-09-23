package co.edu.unicauca.microkernel.common;

import co.edu.unicauca.microkernel.common.entities.DifficultyLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Enumeración DifficultyLevel")
class DifficultyLevelTest {

    @Test
    @DisplayName("Reconoce variaciones del nivel sin importar mayúsculas ni tildes")
    void reconoceVariaciones() {
        assertEquals(DifficultyLevel.BAJO, DifficultyLevel.parse("bajo").orElseThrow());
        assertEquals(DifficultyLevel.BAJO, DifficultyLevel.parse("Fácil").orElseThrow());
        assertEquals(DifficultyLevel.MEDIO, DifficultyLevel.parse("MEDIA").orElseThrow());
        assertEquals(DifficultyLevel.ALTO, DifficultyLevel.parse(" Alto ").orElseThrow());
        assertEquals(DifficultyLevel.ALTO, DifficultyLevel.parse("difícil").orElseThrow());
    }

    @Test
    @DisplayName("Devuelve vacío para textos que no son un nivel")
    void textoNoReconocido() {
        assertTrue(DifficultyLevel.parse("Automática").isEmpty());
        assertTrue(DifficultyLevel.parse(null).isEmpty());
        assertTrue(DifficultyLevel.parse("").isEmpty());
    }
}
