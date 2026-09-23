package co.edu.unicauca.microkernel.support;

import co.edu.unicauca.microkernel.common.entities.Question;
import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.interfaces.QuestionPlugin;

/**
 * Plugin de prueba sin constructor vacío: la reflexión no puede instanciarlo.
 */
public class PluginWithoutDefaultConstructor implements QuestionPlugin {

    private final String name;

    public PluginWithoutDefaultConstructor(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean supports(String type) {
        return false;
    }

    @Override
    public Question generate(QuestionRequest request) {
        return null;
    }
}
