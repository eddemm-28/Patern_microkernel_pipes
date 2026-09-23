package co.edu.unicauca.microkernel.common.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Pregunta almacenada en el banco del núcleo (microkernel).
 * <p>
 * Conserva el diseño base propuesto en el taller (id, title, content, type) y lo
 * amplía con los datos que produce el pipeline de validación: clasificación,
 * competencia, nivel de dificultad, opciones, respuesta correcta y la traza de
 * los filtros por los que pasó la pregunta.
 */
public class Question {

    private final String id;
    private final String title;
    private final String content;
    private final String type;

    private final String classification;
    private final String competency;
    private final String category;
    private final DifficultyLevel difficulty;
    private final List<String> options;
    private final String correctAnswer;
    private final String context;
    private final String mediaType;
    private final String mediaUrl;
    private final String justification;
    private final String generatedBy;
    private final LocalDateTime createdAt;
    private final List<String> processingTrace;

    /**
     * Constructor propuesto en el enunciado del taller.
     */
    public Question(String id, String title, String content, String type) {
        this(new Builder(id, title, content, type));
    }

    private Question(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "El id de la pregunta es obligatorio");
        this.title = builder.title;
        this.content = builder.content;
        this.type = builder.type;
        this.classification = builder.classification;
        this.competency = builder.competency;
        this.category = builder.category;
        this.difficulty = builder.difficulty;
        this.options = builder.options == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(builder.options));
        this.correctAnswer = builder.correctAnswer;
        this.context = builder.context;
        this.mediaType = builder.mediaType;
        this.mediaUrl = builder.mediaUrl;
        this.justification = builder.justification;
        this.generatedBy = builder.generatedBy;
        this.createdAt = builder.createdAt == null ? LocalDateTime.now() : builder.createdAt;
        this.processingTrace = builder.processingTrace == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(builder.processingTrace));
    }

    public static Builder builder(String id, String title, String content, String type) {
        return new Builder(id, title, content, type);
    }

    public String getId() { return id; }

    public String getTitle() { return title; }

    public String getContent() { return content; }

    public String getType() { return type; }

    public String getClassification() { return classification; }

    public String getCompetency() { return competency; }

    public String getCategory() { return category; }

    public DifficultyLevel getDifficulty() { return difficulty; }

    /** Opciones de respuesta (lista inmutable, nunca nula). */
    public List<String> getOptions() { return options; }

    public String getCorrectAnswer() { return correctAnswer; }

    public String getContext() { return context; }

    public String getMediaType() { return mediaType; }

    public String getMediaUrl() { return mediaUrl; }

    public String getJustification() { return justification; }

    /** Nombre del plugin que generó la pregunta. */
    public String getGeneratedBy() { return generatedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    /** Traza de los filtros del pipeline por los que pasó la pregunta (lista inmutable). */
    public List<String> getProcessingTrace() { return processingTrace; }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Question)) {
            return false;
        }
        return id.equals(((Question) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Question{id=" + id + ", type=" + type + ", title=" + title + '}';
    }

    /**
     * Builder para crear preguntas con todos sus datos.
     */
    public static final class Builder {
        private final String id;
        private final String title;
        private final String content;
        private final String type;
        private String classification;
        private String competency;
        private String category;
        private DifficultyLevel difficulty;
        private List<String> options;
        private String correctAnswer;
        private String context;
        private String mediaType;
        private String mediaUrl;
        private String justification;
        private String generatedBy;
        private LocalDateTime createdAt;
        private List<String> processingTrace;

        public Builder(String id, String title, String content, String type) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.type = type;
        }

        public Builder classification(String classification) { this.classification = classification; return this; }

        public Builder competency(String competency) { this.competency = competency; return this; }

        public Builder category(String category) { this.category = category; return this; }

        public Builder difficulty(DifficultyLevel difficulty) { this.difficulty = difficulty; return this; }

        public Builder options(List<String> options) { this.options = options; return this; }

        public Builder correctAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; return this; }

        public Builder context(String context) { this.context = context; return this; }

        public Builder mediaType(String mediaType) { this.mediaType = mediaType; return this; }

        public Builder mediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; return this; }

        public Builder justification(String justification) { this.justification = justification; return this; }

        public Builder generatedBy(String generatedBy) { this.generatedBy = generatedBy; return this; }

        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Builder processingTrace(List<String> processingTrace) { this.processingTrace = processingTrace; return this; }

        public Question build() {
            return new Question(this);
        }
    }
}
