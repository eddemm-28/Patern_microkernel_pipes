package co.edu.unicauca.microkernel.support;

import co.edu.unicauca.microkernel.common.entities.Question;
import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.interfaces.QuestionPlugin;

import java.util.UUID;

/**
 * Plugin de prueba para un tipo nuevo (TRUE_FALSE). Demuestra que el núcleo acepta
 * nuevos tipos de pregunta registrándolos en la configuración, sin modificar su código.
 */
public class FakeTrueFalsePlugin implements QuestionPlugin {

    @Override
    public String getName() {
        return "true-false";
    }

    @Override
    public boolean supports(String type) {
        return "TRUE_FALSE".equalsIgnoreCase(type);
    }

    @Override
    public Question generate(QuestionRequest request) {
        return new Question(UUID.randomUUID().toString(), request.getTitle(), request.getContent(), "TRUE_FALSE");
    }
}
