package co.edu.unicauca.microkernel.pipeline.filters;

import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.common.util.TextNormalizer;
import co.edu.unicauca.microkernel.pipeline.base.QuestionFilter;

import java.util.List;

/**
 * Filtro 4 - Validar respuesta.
 * <p>
 * Verifica que exista una respuesta correcta y que sea válida:
 * <ul>
 *   <li>La respuesta correcta existe (no está vacía).</li>
 *   <li>Pertenece a las opciones de respuesta.</li>
 *   <li>Consistencia de datos: coincide con una única opción (RF-11).</li>
 * </ul>
 * Como transformación, deja la respuesta escrita exactamente igual que la opción.
 */
public class CorrectAnswerValidationFilter implements QuestionFilter {

    @Override
    public QuestionRequest process(QuestionRequest request) {
        if (TextNormalizer.isBlank(request.getCorrectAnswer())) {
            throw fail("Debe indicar la respuesta correcta.");
        }
        List<String> options = request.getOptions();
        if (options == null || options.isEmpty()) {
            throw fail("No hay opciones contra las cuales validar la respuesta correcta.");
        }

        String answer = TextNormalizer.normalize(request.getCorrectAnswer());
        int matchIndex = -1;
        int matches = 0;
        for (int index = 0; index < options.size(); index++) {
            if (TextNormalizer.normalize(options.get(index)).equals(answer)) {
                matches++;
                matchIndex = index;
            }
        }

        if (matches == 0) {
            throw fail("La respuesta correcta \"" + request.getCorrectAnswer().trim()
                    + "\" no corresponde a ninguna de las opciones.");
        }
        if (matches > 1) {
            throw fail("La respuesta correcta coincide con más de una opción; debe existir una única respuesta correcta.");
        }

        return request.toBuilder()
                .correctAnswer(options.get(matchIndex))
                .build();
    }

    @Override
    public String getDescription() {
        return "respuesta correcta única y presente en las opciones";
    }

    private QuestionValidationException fail(String reason) {
        return new QuestionValidationException(getName(), reason);
    }
}
