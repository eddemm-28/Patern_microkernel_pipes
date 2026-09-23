package co.edu.unicauca.microkernel.plugins;

import co.edu.unicauca.microkernel.pipeline.base.QuestionPipeline;
import co.edu.unicauca.microkernel.pipeline.filters.ClassificationFilter;
import co.edu.unicauca.microkernel.pipeline.filters.ContentValidationFilter;
import co.edu.unicauca.microkernel.pipeline.filters.CorrectAnswerValidationFilter;
import co.edu.unicauca.microkernel.pipeline.filters.MediaResourceValidationFilter;
import co.edu.unicauca.microkernel.pipeline.filters.OptionsValidationFilter;

/**
 * Plugin GeneradorPreguntaMultimedia: crea preguntas que incluyen una imagen,
 * un audio o un video.
 * <p>
 * Reutiliza los cuatro filtros del pipeline estándar y agrega
 * {@link MediaResourceValidationFilter} para validar el recurso multimedia.
 */
public class MultimediaQuestionPlugin extends AbstractPipelineQuestionPlugin {

    public static final String TYPE = "MULTIMEDIA";

    @Override
    public String getName() {
        return "multimedia";
    }

    @Override
    protected String supportedType() {
        return TYPE;
    }

    @Override
    protected QuestionPipeline createPipeline() {
        return new QuestionPipeline()
                .addFilter(new ContentValidationFilter())
                .addFilter(new MediaResourceValidationFilter())
                .addFilter(new OptionsValidationFilter())
                .addFilter(new ClassificationFilter())
                .addFilter(new CorrectAnswerValidationFilter());
    }
}
