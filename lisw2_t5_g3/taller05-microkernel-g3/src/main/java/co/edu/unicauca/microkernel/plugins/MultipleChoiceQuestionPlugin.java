package co.edu.unicauca.microkernel.plugins;

import co.edu.unicauca.microkernel.pipeline.base.QuestionPipeline;
import co.edu.unicauca.microkernel.pipeline.filters.ClassificationFilter;
import co.edu.unicauca.microkernel.pipeline.filters.ContentValidationFilter;
import co.edu.unicauca.microkernel.pipeline.filters.CorrectAnswerValidationFilter;
import co.edu.unicauca.microkernel.pipeline.filters.OptionsValidationFilter;

/**
 * Plugin GeneradorPreguntaSeleccionMultiple: genera preguntas de selección múltiple
 * con única respuesta.
 * <p>
 * Implementa el pipeline propuesto en el taller:
 * Entrada → ContentValidationFilter → OptionsValidationFilter →
 * ClassificationFilter → CorrectAnswerValidationFilter → Salida.
 */
public class MultipleChoiceQuestionPlugin extends AbstractPipelineQuestionPlugin {

    public static final String TYPE = "MULTIPLE_CHOICE";

    @Override
    public String getName() {
        return "multiple-choice";
    }

    @Override
    protected String supportedType() {
        return TYPE;
    }

    @Override
    protected QuestionPipeline createPipeline() {
        return new QuestionPipeline()
                .addFilter(new ContentValidationFilter())
                .addFilter(new OptionsValidationFilter())
                .addFilter(new ClassificationFilter())
                .addFilter(new CorrectAnswerValidationFilter());
    }
}
