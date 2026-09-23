package co.edu.unicauca.microkernel.support;

import co.edu.unicauca.microkernel.common.entities.Question;
import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.interfaces.QuestionPlugin;

/**
 * Plugin de prueba que falla al generar: sirve para verificar que el núcleo aísla
 * los errores inesperados de un plugin.
 */
public class FailingPlugin implements QuestionPlugin {

    @Override
    public String getName() {
        return "failing";
    }

    @Override
    public boolean supports(String type) {
        return "FAILING".equalsIgnoreCase(type);
    }

    @Override
    public Question generate(QuestionRequest request) {
        throw new UnsupportedOperationException("error simulado");
    }
}
