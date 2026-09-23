package co.edu.unicauca.microkernel.plugins;

import co.edu.unicauca.microkernel.common.entities.Question;
import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.interfaces.QuestionPlugin;
import co.edu.unicauca.microkernel.pipeline.base.PipelineResult;
import co.edu.unicauca.microkernel.pipeline.base.QuestionPipeline;

import java.util.List;
import java.util.UUID;

/**
 * Base común para los plugins que validan la solicitud con un pipeline antes de
 * generar la pregunta (patrón Método Plantilla).
 * <p>
 * El algoritmo de {@link #generate(QuestionRequest)} es siempre el mismo:
 * ejecutar el pipeline, detenerse si la solicitud es rechazada y construir la
 * pregunta con la salida del último filtro. Cada plugin concreto solo define su
 * tipo de pregunta, su nombre y los filtros de su pipeline.
 * <p>
 * Esta clase es abstracta: si se registrara en {@code plugins.properties}, el núcleo
 * la rechazaría al intentar instanciarla por reflexión.
 */
public abstract class AbstractPipelineQuestionPlugin implements QuestionPlugin {

    private QuestionPipeline pipeline;

    /** Tipo de pregunta que atiende el plugin (por ejemplo "MULTIPLE_CHOICE"). */
    protected abstract String supportedType();

    /** Construye el pipeline de validación del plugin. */
    protected abstract QuestionPipeline createPipeline();

    @Override
    public boolean supports(String type) {
        return supportedType().equalsIgnoreCase(type == null ? null : type.trim());
    }

    @Override
    public final Question generate(QuestionRequest request) {
        PipelineResult result = getPipeline().execute(request);
        if (!result.isValid()) {
            throw result.toException();
        }
        return buildQuestion(result.getOutput(), result.getTrace());
    }

    /** Pipeline del plugin (se crea una sola vez, la primera vez que se necesita). */
    public QuestionPipeline getPipeline() {
        if (pipeline == null) {
            pipeline = createPipeline();
        }
        return pipeline;
    }

    /** Construye la pregunta final con los datos ya validados y enriquecidos por el pipeline. */
    protected Question buildQuestion(QuestionRequest processed, List<String> trace) {
        return Question.builder(UUID.randomUUID().toString(), processed.getTitle(),
                        processed.getContent(), supportedType())
                .classification(processed.getClassification())
                .competency(processed.getCompetency())
                .category(processed.getCategory())
                .difficulty(processed.getDifficulty())
                .options(processed.getOptions())
                .correctAnswer(processed.getCorrectAnswer())
                .context(processed.getContext())
                .mediaType(processed.getMediaType())
                .mediaUrl(processed.getMediaUrl())
                .justification(processed.getJustification())
                .generatedBy(getName())
                .processingTrace(trace)
                .build();
    }
}
