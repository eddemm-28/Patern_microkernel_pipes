package co.edu.unicauca.microkernel.plugins;

import co.edu.unicauca.microkernel.pipeline.base.QuestionPipeline;
import co.edu.unicauca.microkernel.pipeline.filters.CaseContextValidationFilter;
import co.edu.unicauca.microkernel.pipeline.filters.ClassificationFilter;
import co.edu.unicauca.microkernel.pipeline.filters.ContentValidationFilter;
import co.edu.unicauca.microkernel.pipeline.filters.CorrectAnswerValidationFilter;
import co.edu.unicauca.microkernel.pipeline.filters.OptionsValidationFilter;

/**
 * Plugin GeneradorPreguntaCaso: genera preguntas basadas en el análisis de un caso.
 * <p>
 * Reutiliza los cuatro filtros del pipeline estándar y agrega
 * {@link CaseContextValidationFilter} para exigir la descripción del caso.
 */
public class CaseQuestionPlugin extends AbstractPipelineQuestionPlugin {

    public static final String TYPE = "CASE";

    @Override
    public String getName() {
        return "case-analysis";
    }

    @Override
    protected String supportedType() {
        return TYPE;
    }

    @Override
    protected QuestionPipeline createPipeline() {
        return new QuestionPipeline()
                .addFilter(new ContentValidationFilter())
                .addFilter(new CaseContextValidationFilter())
                .addFilter(new OptionsValidationFilter())
                .addFilter(new ClassificationFilter())
                .addFilter(new CorrectAnswerValidationFilter());
    }
}
