package co.edu.unicauca.microkernel.core;

import co.edu.unicauca.microkernel.common.interfaces.QuestionPlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Carga de plugins por reflexión (PluginLoader)")
class PluginLoaderTest {

    private final PluginLoader loader = new PluginLoader();

    @Test
    @DisplayName("Crea el plugin a partir del nombre de la clase")
    void instanciaPluginPorReflexion() throws PluginLoadException {
        QuestionPlugin plugin = loader.instantiate("co.edu.unicauca.microkernel.plugins.MultipleChoiceQuestionPlugin");

        assertEquals("multiple-choice", plugin.getName());
        assertTrue(plugin.supports("MULTIPLE_CHOICE"));
    }

    @Test
    @DisplayName("Falla si la clase no existe en el classpath")
    void fallaSiLaClaseNoExiste() {
        PluginLoadException ex = assertThrows(PluginLoadException.class,
                () -> loader.instantiate("co.edu.unicauca.microkernel.plugins.NoExiste"));
        assertTrue(ex.getMessage().contains("No se encontró la clase"));
    }

    @Test
    @DisplayName("Falla si la clase no implementa el contrato QuestionPlugin")
    void fallaSiNoCumpleElContrato() {
        PluginLoadException ex = assertThrows(PluginLoadException.class, () -> loader.instantiate("java.lang.String"));
        assertTrue(ex.getMessage().contains("no implementa el contrato"));
    }

    @Test
    @DisplayName("Falla si la clase es abstracta")
    void fallaSiLaClaseEsAbstracta() {
        PluginLoadException ex = assertThrows(PluginLoadException.class,
                () -> loader.instantiate("co.edu.unicauca.microkernel.plugins.AbstractPipelineQuestionPlugin"));
        assertTrue(ex.getMessage().contains("abstracta"));
    }

    @Test
    @DisplayName("Falla si la clase no tiene constructor sin parámetros")
    void fallaSinConstructorVacio() {
        PluginLoadException ex = assertThrows(PluginLoadException.class,
                () -> loader.instantiate("co.edu.unicauca.microkernel.support.PluginWithoutDefaultConstructor"));
        assertTrue(ex.getMessage().contains("constructor"));
    }

    @Test
    @DisplayName("Falla si no se indica la clase")
    void fallaSinNombreDeClase() {
        assertThrows(PluginLoadException.class, () -> loader.instantiate("  "));
        assertThrows(PluginLoadException.class, () -> loader.instantiate(null));
    }

    @Test
    @DisplayName("Lee el archivo de configuración conservando el orden de las entradas")
    void leeLaConfiguracion() throws IOException {
        Map<String, String> entries = loader.readConfiguration("plugins-test.properties");

        assertTrue(entries.containsKey("plugin.multiple_choice"));
        assertEquals("co.edu.unicauca.microkernel.support.FakeTrueFalsePlugin", entries.get("plugin.true_false"));
        assertEquals("plugin.multiple_choice", entries.keySet().iterator().next());
    }

    @Test
    @DisplayName("Falla si el archivo de configuración no existe")
    void fallaSiNoExisteLaConfiguracion() {
        assertThrows(IOException.class, () -> loader.readConfiguration("no-existe.properties"));
    }
}
