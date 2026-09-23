package co.edu.unicauca.microkernel.common.interfaces;

import co.edu.unicauca.microkernel.common.entities.Question;
import co.edu.unicauca.microkernel.common.entities.QuestionRequest;

/**
 * Contrato común que deben cumplir todos los plugins del microkernel.
 * <p>
 * El núcleo solo conoce esta interfaz: nunca referencia clases concretas de
 * plugins. Las implementaciones se registran en {@code plugins.properties} y se
 * instancian por reflexión.
 */
public interface QuestionPlugin {

    /** Nombre corto que identifica al plugin (por ejemplo "multiple-choice"). */
    String getName();

    /**
     * Indica si el plugin sabe generar preguntas del tipo indicado.
     *
     * @param type tipo de pregunta (por ejemplo "MULTIPLE_CHOICE")
     */
    boolean supports(String type);

    /**
     * Genera una pregunta a partir de la solicitud.
     *
     * @param request datos de la pregunta
     * @return la pregunta generada
     * @throws co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException
     *         si la solicitud no supera las validaciones del plugin
     */
    Question generate(QuestionRequest request);
}
