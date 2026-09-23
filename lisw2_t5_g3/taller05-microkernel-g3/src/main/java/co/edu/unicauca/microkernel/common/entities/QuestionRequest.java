package co.edu.unicauca.microkernel.common.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Solicitud de creación de una pregunta. Es el dato que viaja por la tubería
 * (pipeline): cada filtro la recibe, la valida y entrega al siguiente filtro una
 * versión normalizada o enriquecida (por ejemplo, con la competencia asignada).
 * <p>
 * La clase es inmutable: los filtros no modifican la solicitud recibida, sino que
 * construyen una nueva con {@link #toBuilder()}.
 */
public class QuestionRequest {

    private final String title;
    private final String content;
    private final String type;
    private final String classification;
    private final List<String> options;
    private final String correctAnswer;

    // Datos adicionales usados por algunos plugins
    private final String context;
    private final String mediaType;
    private final String mediaUrl;
    private final String justification;

    // Datos asignados por el ClassificationFilter
    private final String competency;
    private final String category;
    private final DifficultyLevel difficulty;

    /**
     * Constructor propuesto en la guía del taller.
     */
    public QuestionRequest(String title, String content, String type, String classification,
                           List<String> options, String correctAnswer) {
        this(builder()
                .title(title)
                .content(content)
                .type(type)
                .classification(classification)
                .options(options)
                .correctAnswer(correctAnswer));
    }

    private QuestionRequest(Builder builder) {
        this.title = builder.title;
        this.content = builder.content;
        this.type = builder.type;
        this.classification = builder.classification;
        this.options = builder.options == null ? null : Collections.unmodifiableList(new ArrayList<>(builder.options));
        this.correctAnswer = builder.correctAnswer;
        this.context = builder.context;
        this.mediaType = builder.mediaType;
        this.mediaUrl = builder.mediaUrl;
        this.justification = builder.justification;
        this.competency = builder.competency;
        this.category = builder.category;
        this.difficulty = builder.difficulty;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Crea un builder con una copia de todos los datos de esta solicitud. */
    public Builder toBuilder() {
        return new Builder()
                .title(title)
                .content(content)
                .type(type)
                .classification(classification)
                .options(options)
                .correctAnswer(correctAnswer)
                .context(context)
                .mediaType(mediaType)
                .mediaUrl(mediaUrl)
                .justification(justification)
                .competency(competency)
                .category(category)
                .difficulty(difficulty);
    }

    public String getTitle() { return title; }

    public String getContent() { return content; }

    public String getType() { return type; }

    public String getClassification() { return classification; }

    /** Opciones de respuesta (lista inmutable) o {@code null} si no se enviaron. */
    public List<String> getOptions() { return options; }

    public String getCorrectAnswer() { return correctAnswer; }

    public String getContext() { return context; }

    public String getMediaType() { return mediaType; }

    public String getMediaUrl() { return mediaUrl; }

    public String getJustification() { return justification; }

    public String getCompetency() { return competency; }

    public String getCategory() { return category; }

    public DifficultyLevel getDifficulty() { return difficulty; }

    @Override
    public String toString() {
        return "QuestionRequest{type=" + type + ", title=" + title + ", classification=" + classification + '}';
    }

    /**
     * Builder para construir solicitudes con los campos opcionales.
     */
    public static final class Builder {
        private String title;
        private String content;
        private String type;
        private String classification;
        private List<String> options;
        private String correctAnswer;
        private String context;
        private String mediaType;
        private String mediaUrl;
        private String justification;
        private String competency;
        private String category;
        private DifficultyLevel difficulty;

        private Builder() {
        }

        public Builder title(String title) { this.title = title; return this; }

        public Builder content(String content) { this.content = content; return this; }

        public Builder type(String type) { this.type = type; return this; }

        public Builder classification(String classification) { this.classification = classification; return this; }

        public Builder options(List<String> options) { this.options = options; return this; }

        public Builder correctAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; return this; }

        public Builder context(String context) { this.context = context; return this; }

        public Builder mediaType(String mediaType) { this.mediaType = mediaType; return this; }

        public Builder mediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; return this; }

        public Builder justification(String justification) { this.justification = justification; return this; }

        public Builder competency(String competency) { this.competency = competency; return this; }

        public Builder category(String category) { this.category = category; return this; }

        public Builder difficulty(DifficultyLevel difficulty) { this.difficulty = difficulty; return this; }

        public QuestionRequest build() {
            return new QuestionRequest(this);
        }
    }
}
