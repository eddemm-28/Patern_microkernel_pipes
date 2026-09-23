package co.edu.unicauca.microkernel.presentation;

import co.edu.unicauca.microkernel.common.entities.Question;
import co.edu.unicauca.microkernel.pipeline.filters.OptionsValidationFilter;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Convierte una pregunta en texto legible para mostrarla en la interfaz o en consola.
 */
public final class QuestionFormatter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private QuestionFormatter() {
    }

    public static String format(Question question) {
        StringBuilder text = new StringBuilder();
        text.append("ID:           ").append(question.getId()).append('\n');
        text.append("Tipo:         ").append(question.getType()).append('\n');
        text.append("Plugin:       ").append(nullSafe(question.getGeneratedBy())).append('\n');
        text.append("Creada:       ").append(question.getCreatedAt().format(DATE_FORMAT)).append('\n');
        text.append("Título:       ").append(question.getTitle()).append('\n');
        text.append("Área:         ").append(nullSafe(question.getClassification())).append('\n');
        text.append("Competencia:  ").append(nullSafe(question.getCompetency()))
                .append(question.getCategory() == null ? "" : " (" + question.getCategory() + ")").append('\n');
        text.append("Dificultad:   ")
                .append(question.getDifficulty() == null ? "-" : question.getDifficulty().getLabel()).append('\n');
        if (question.getMediaUrl() != null) {
            text.append("Recurso:      ").append(question.getMediaType()).append(" - ")
                    .append(question.getMediaUrl()).append('\n');
        }
        if (question.getContext() != null) {
            text.append("\nCaso:\n").append(question.getContext()).append('\n');
        }
        text.append("\nEnunciado:\n").append(question.getContent()).append("\n\n");

        List<String> options = question.getOptions();
        for (int index = 0; index < options.size(); index++) {
            String option = options.get(index);
            boolean correct = option.equals(question.getCorrectAnswer());
            text.append(correct ? " (*) " : "     ")
                    .append(OptionsValidationFilter.letterOf(index)).append(". ").append(option).append('\n');
        }
        if (question.getJustification() != null && !question.getJustification().isBlank()) {
            text.append("\nJustificación:\n").append(question.getJustification()).append('\n');
        }
        if (!question.getProcessingTrace().isEmpty()) {
            text.append("\nTraza del pipeline:\n");
            question.getProcessingTrace().forEach(step -> text.append("  ").append(step).append('\n'));
        }
        return text.toString();
    }

    private static String nullSafe(String value) {
        return value == null ? "-" : value;
    }
}
