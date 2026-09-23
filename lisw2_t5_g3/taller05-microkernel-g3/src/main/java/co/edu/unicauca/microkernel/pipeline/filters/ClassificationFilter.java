package co.edu.unicauca.microkernel.pipeline.filters;

import co.edu.unicauca.microkernel.common.entities.DifficultyLevel;
import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.common.util.TextNormalizer;
import co.edu.unicauca.microkernel.pipeline.base.QuestionFilter;

/**
 * Filtro 3 - Clasificar competencia.
 * <p>
 * Valida que la clasificación (área de conocimiento) de la pregunta sea válida,
 * por ejemplo "Arquitectura de software", y enriquece la solicitud:
 * <ul>
 *   <li>Determina la competencia Saber PRO del área.</li>
 *   <li>Establece la categoría (competencia específica o genérica).</li>
 *   <li>Asigna el nivel de dificultad: respeta el indicado por el autor o, si no se
 *       indicó, lo estima según la extensión de la pregunta.</li>
 * </ul>
 */
public class ClassificationFilter implements QuestionFilter {

    /** Hasta este número de palabras la pregunta se considera de dificultad baja. */
    public static final int MAX_WORDS_LOW = 40;
    /** Hasta este número de palabras la pregunta se considera de dificultad media. */
    public static final int MAX_WORDS_MEDIUM = 100;

    @Override
    public QuestionRequest process(QuestionRequest request) {
        if (TextNormalizer.isBlank(request.getClassification())) {
            throw new QuestionValidationException(getName(),
                    "La clasificación (área de conocimiento) de la pregunta es obligatoria.");
        }

        CompetencyCatalog.Entry entry = CompetencyCatalog.find(request.getClassification())
                .orElseThrow(() -> new QuestionValidationException(getName(),
                        "El área \"" + request.getClassification().trim()
                                + "\" no pertenece al catálogo de competencias Saber PRO."));

        DifficultyLevel difficulty = request.getDifficulty() != null
                ? request.getDifficulty()
                : estimateDifficulty(request);

        return request.toBuilder()
                .classification(entry.getArea())
                .competency(entry.getCompetency())
                .category(entry.getCategory())
                .difficulty(difficulty)
                .build();
    }

    @Override
    public String getDescription() {
        return "competencia, categoría y nivel de dificultad asignados";
    }

    /**
     * Estima la dificultad según el número de palabras del caso, el enunciado y las opciones:
     * a mayor cantidad de información que el estudiante debe analizar, mayor dificultad.
     */
    public static DifficultyLevel estimateDifficulty(QuestionRequest request) {
        int words = TextNormalizer.countWords(request.getContext())
                + TextNormalizer.countWords(request.getContent());
        if (request.getOptions() != null) {
            for (String option : request.getOptions()) {
                words += TextNormalizer.countWords(option);
            }
        }
        if (words <= MAX_WORDS_LOW) {
            return DifficultyLevel.BAJO;
        }
        if (words <= MAX_WORDS_MEDIUM) {
            return DifficultyLevel.MEDIO;
        }
        return DifficultyLevel.ALTO;
    }
}
