package co.edu.unicauca.microkernel.common.entities;

import co.edu.unicauca.microkernel.common.util.TextNormalizer;

import java.util.Optional;

/**
 * Nivel de dificultad de una pregunta del banco.
 */
public enum DifficultyLevel {

    BAJO("Bajo"),
    MEDIO("Medio"),
    ALTO("Alto");

    private final String label;

    DifficultyLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Interpreta un texto como nivel de dificultad. Acepta variaciones como
     * "bajo", "Baja", "fácil", "MEDIO", "alta" o "difícil".
     *
     * @param text texto a interpretar
     * @return el nivel reconocido o {@code Optional.empty()} si no se reconoce
     */
    public static Optional<DifficultyLevel> parse(String text) {
        switch (TextNormalizer.normalize(text)) {
            case "bajo":
            case "baja":
            case "facil":
                return Optional.of(BAJO);
            case "medio":
            case "media":
            case "intermedio":
            case "intermedia":
                return Optional.of(MEDIO);
            case "alto":
            case "alta":
            case "dificil":
                return Optional.of(ALTO);
            default:
                return Optional.empty();
        }
    }
}
