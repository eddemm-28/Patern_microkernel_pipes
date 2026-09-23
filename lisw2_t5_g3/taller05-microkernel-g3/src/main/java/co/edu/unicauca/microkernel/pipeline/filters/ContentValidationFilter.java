package co.edu.unicauca.microkernel.pipeline.filters;

import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.common.util.TextNormalizer;
import co.edu.unicauca.microkernel.pipeline.base.QuestionFilter;

/**
 * Filtro 1 - Validar contenido.
 * <p>
 * Verifica que la pregunta tenga texto válido y cumpla reglas básicas:
 * <ul>
 *   <li>Texto no vacío: título y enunciado obligatorios.</li>
 *   <li>Longitud mínima y máxima de título y enunciado.</li>
 *   <li>Formato correcto: el enunciado contiene letras y una única pregunta directa (RF-09).</li>
 * </ul>
 * Como transformación, elimina los espacios sobrantes del título y del enunciado.
 */
public class ContentValidationFilter implements QuestionFilter {

    public static final int MIN_TITLE_LENGTH = 5;
    public static final int MAX_TITLE_LENGTH = 150;
    public static final int MIN_CONTENT_LENGTH = 15;
    public static final int MAX_CONTENT_LENGTH = 1500;

    @Override
    public QuestionRequest process(QuestionRequest request) {
        if (TextNormalizer.isBlank(request.getTitle())) {
            throw fail("El título de la pregunta es obligatorio.");
        }
        if (TextNormalizer.isBlank(request.getContent())) {
            throw fail("El enunciado de la pregunta es obligatorio.");
        }

        String title = TextNormalizer.collapseSpaces(request.getTitle()).replaceAll("\\s+", " ");
        String content = TextNormalizer.collapseSpaces(request.getContent());

        if (title.length() < MIN_TITLE_LENGTH) {
            throw fail("El título debe tener al menos " + MIN_TITLE_LENGTH + " caracteres.");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw fail("El título no puede superar " + MAX_TITLE_LENGTH + " caracteres.");
        }
        if (content.length() < MIN_CONTENT_LENGTH) {
            throw fail("El enunciado debe tener al menos " + MIN_CONTENT_LENGTH + " caracteres.");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw fail("El enunciado no puede superar " + MAX_CONTENT_LENGTH + " caracteres.");
        }
        if (!content.matches("(?s).*\\p{L}.*")) {
            throw fail("El enunciado debe contener texto, no solo números o símbolos.");
        }
        long questionMarks = content.chars().filter(c -> c == '?').count();
        if (questionMarks > 1) {
            throw fail("El enunciado debe contener una única pregunta directa (tiene " + questionMarks + ").");
        }

        return request.toBuilder()
                .title(title)
                .content(content)
                .build();
    }

    @Override
    public String getDescription() {
        return "título y enunciado válidos";
    }

    private QuestionValidationException fail(String reason) {
        return new QuestionValidationException(getName(), reason);
    }
}
