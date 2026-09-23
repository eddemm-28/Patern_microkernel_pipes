package co.edu.unicauca.microkernel.pipeline.filters;

import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.common.util.TextNormalizer;
import co.edu.unicauca.microkernel.pipeline.base.QuestionFilter;

/**
 * Filtro adicional para preguntas de análisis de caso.
 * <p>
 * Verifica que exista la descripción del caso (contexto, RF-08), que tenga una
 * extensión suficiente y que el enunciado no sea una copia del caso.
 */
public class CaseContextValidationFilter implements QuestionFilter {

    public static final int MIN_CONTEXT_LENGTH = 80;
    public static final int MAX_CONTEXT_LENGTH = 3000;

    @Override
    public QuestionRequest process(QuestionRequest request) {
        if (TextNormalizer.isBlank(request.getContext())) {
            throw fail("Las preguntas de análisis de caso requieren la descripción del caso (contexto).");
        }
        String context = TextNormalizer.collapseSpaces(request.getContext());
        if (context.length() < MIN_CONTEXT_LENGTH) {
            throw fail("El caso debe tener al menos " + MIN_CONTEXT_LENGTH
                    + " caracteres para dar información suficiente (tiene " + context.length() + ").");
        }
        if (context.length() > MAX_CONTEXT_LENGTH) {
            throw fail("El caso no puede superar " + MAX_CONTEXT_LENGTH + " caracteres.");
        }
        if (TextNormalizer.normalize(context).equals(TextNormalizer.normalize(request.getContent()))) {
            throw fail("El enunciado no puede ser igual al caso: debe formular una pregunta sobre él.");
        }
        return request.toBuilder()
                .context(context)
                .build();
    }

    @Override
    public String getDescription() {
        return "caso presente y con información suficiente";
    }

    private QuestionValidationException fail(String reason) {
        return new QuestionValidationException(getName(), reason);
    }
}
