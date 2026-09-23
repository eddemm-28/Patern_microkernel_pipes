package co.edu.unicauca.microkernel.pipeline.filters;

import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.common.util.TextNormalizer;
import co.edu.unicauca.microkernel.pipeline.base.QuestionFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Filtro 2 - Validar opciones.
 * <p>
 * Verifica la cantidad y calidad de las opciones de respuesta:
 * <ul>
 *   <li>Exactamente 4 opciones (formato Saber PRO, RF-10).</li>
 *   <li>Sin opciones vacías y con una longitud máxima (RF-13).</li>
 *   <li>Sin opciones duplicadas.</li>
 *   <li>Sin expresiones prohibidas como "Todas las anteriores" o
 *       "Ninguna de las anteriores" (RF-12).</li>
 * </ul>
 * Como transformación, elimina los espacios sobrantes de cada opción.
 */
public class OptionsValidationFilter implements QuestionFilter {

    public static final int REQUIRED_OPTIONS = 4;
    public static final int MAX_OPTION_LENGTH = 250;

    /** Expresiones no permitidas en las opciones (comparadas sin tildes ni mayúsculas). */
    public static final List<String> FORBIDDEN_EXPRESSIONS = List.of(
            "todas las anteriores",
            "ninguna de las anteriores",
            "todas las opciones anteriores",
            "ninguna de las opciones anteriores"
    );

    @Override
    public QuestionRequest process(QuestionRequest request) {
        List<String> options = request.getOptions();
        if (options == null || options.isEmpty()) {
            throw fail("La pregunta debe tener opciones de respuesta.");
        }
        if (options.size() != REQUIRED_OPTIONS) {
            throw fail("La pregunta debe tener exactamente " + REQUIRED_OPTIONS
                    + " opciones de respuesta (tiene " + options.size() + ").");
        }

        List<String> cleanOptions = new ArrayList<>();
        Map<String, Character> seen = new HashMap<>();
        for (int index = 0; index < options.size(); index++) {
            char letter = letterOf(index);
            String option = options.get(index);

            if (TextNormalizer.isBlank(option)) {
                throw fail("La opción " + letter + " está vacía.");
            }
            String clean = TextNormalizer.collapseSpaces(option).replaceAll("\\s+", " ");
            if (clean.length() > MAX_OPTION_LENGTH) {
                throw fail("La opción " + letter + " supera los " + MAX_OPTION_LENGTH + " caracteres.");
            }

            String normalized = TextNormalizer.normalize(clean);
            for (String forbidden : FORBIDDEN_EXPRESSIONS) {
                if (normalized.contains(forbidden)) {
                    throw fail("La opción " + letter + " usa la expresión no permitida \"" + clean + "\".");
                }
            }

            Character previous = seen.putIfAbsent(normalized, letter);
            if (previous != null) {
                throw fail("Las opciones " + previous + " y " + letter + " están duplicadas.");
            }
            cleanOptions.add(clean);
        }

        return request.toBuilder()
                .options(cleanOptions)
                .build();
    }

    @Override
    public String getDescription() {
        return "4 opciones completas, sin duplicados ni expresiones prohibidas";
    }

    /** Letra de la opción según su posición (0 = A, 1 = B, ...). */
    public static char letterOf(int index) {
        return (char) ('A' + index);
    }

    private QuestionValidationException fail(String reason) {
        return new QuestionValidationException(getName(), reason);
    }
}
