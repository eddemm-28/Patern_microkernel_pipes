package co.edu.unicauca.microkernel.core;

import co.edu.unicauca.microkernel.common.entities.Question;
import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.support.TestRequests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Núcleo QuestionMicrokernel")
class QuestionMicrokernelTest {

    private QuestionMicrokernel microkernel;

    @BeforeEach
    void setUp() {
        microkernel = new QuestionMicrokernel();
    }

    // ----------------------- Registro de plugins -----------------------

    @Test
    @DisplayName("Carga por reflexión los tres plugins registrados en plugins.properties")
    void cargaLosPluginsDeLaConfiguracion() {
        List<PluginDescriptor> descriptors = microkernel.getPluginDescriptors();

        assertEquals(3, descriptors.size());
        assertTrue(descriptors.stream().allMatch(PluginDescriptor::isActive));
        assertEquals(List.of("multiple-choice", "case-analysis", "multimedia"),
                descriptors.stream().map(PluginDescriptor::getPluginName).collect(java.util.stream.Collectors.toList()));
        assertEquals(3, microkernel.getActivePlugins().size());
    }

    @Test
    @DisplayName("Encuentra el plugin que soporta cada tipo de pregunta")
    void encuentraElPluginPorTipo() {
        assertEquals("multiple-choice", microkernel.findPlugin("MULTIPLE_CHOICE").orElseThrow().getName());
        assertEquals("case-analysis", microkernel.findPlugin("CASE").orElseThrow().getName());
        assertEquals("multimedia", microkernel.findPlugin("MULTIMEDIA").orElseThrow().getName());
        assertTrue(microkernel.findPlugin("TRUE_FALSE").isEmpty());
    }

    @Test
    @DisplayName("Registrar un plugin nuevo no obliga a modificar el núcleo")
    void registraUnPluginNuevoEnCaliente() {
        PluginDescriptor descriptor = microkernel.registerPlugin("true_false",
                "co.edu.unicauca.microkernel.support.FakeTrueFalsePlugin");

        assertEquals(PluginState.ACTIVO, descriptor.getState());
        assertEquals("true-false", descriptor.getPluginName());

        Question question = microkernel.executePlugin("TRUE_FALSE", TestRequests.validMultipleChoice());
        assertEquals("TRUE_FALSE", question.getType());
        assertEquals(1, microkernel.getQuestionCount());
    }

    @Test
    @DisplayName("Un plugin que no se puede cargar queda en estado ERROR y el núcleo sigue funcionando")
    void pluginConErrorNoDetieneElNucleo() {
        PluginDescriptor noExiste = microkernel.registerPlugin("malo", "co.edu.unicauca.NoExiste");
        PluginDescriptor sinContrato = microkernel.registerPlugin("sin_contrato", "java.lang.String");

        assertEquals(PluginState.ERROR, noExiste.getState());
        assertEquals(PluginState.ERROR, sinContrato.getState());
        assertEquals(3, microkernel.getActivePlugins().size());
        assertNotNull(microkernel.executePlugin("MULTIPLE_CHOICE", TestRequests.validMultipleChoice()));
    }

    @Test
    @DisplayName("Usa el archivo de configuración indicado y reporta las entradas inválidas")
    void usaOtroArchivoDeConfiguracion() {
        QuestionMicrokernel kernel = new QuestionMicrokernel("plugins-test.properties");

        assertEquals(4, kernel.getPluginDescriptors().size());
        assertEquals(2, kernel.getActivePlugins().size());
        assertTrue(kernel.getPluginDescriptor("inexistente").orElseThrow().getState() == PluginState.ERROR);
        assertTrue(kernel.findPlugin("TRUE_FALSE").isPresent());
    }

    @Test
    @DisplayName("Si no existe el archivo de configuración el núcleo arranca sin plugins")
    void configuracionInexistente() {
        QuestionMicrokernel kernel = new QuestionMicrokernel("no-existe.properties");

        assertTrue(kernel.getPluginDescriptors().isEmpty());
        assertNotNull(kernel.getConfigurationError());
    }

    // ----------------------- Ciclo de vida -----------------------

    @Test
    @DisplayName("Un plugin desactivado deja de atender solicitudes y puede reactivarse")
    void desactivarYActivarPlugin() {
        microkernel.deactivatePlugin("multiple_choice");

        assertEquals(PluginState.INACTIVO, microkernel.getPluginDescriptor("multiple_choice").orElseThrow().getState());
        assertThrows(IllegalArgumentException.class,
                () -> microkernel.executePlugin("MULTIPLE_CHOICE", TestRequests.validMultipleChoice()));

        microkernel.activatePlugin("multiple_choice");

        assertNotNull(microkernel.executePlugin("MULTIPLE_CHOICE", TestRequests.validMultipleChoice()));
    }

    @Test
    @DisplayName("Descargar un plugin lo elimina del núcleo y recargar la configuración lo restaura")
    void descargarYRecargarPlugins() {
        assertTrue(microkernel.unregisterPlugin("case"));
        assertEquals(2, microkernel.getPluginDescriptors().size());
        assertTrue(microkernel.findPlugin("CASE").isEmpty());

        microkernel.reloadPlugins();

        assertEquals(3, microkernel.getPluginDescriptors().size());
        assertTrue(microkernel.findPlugin("CASE").isPresent());
    }

    @Test
    @DisplayName("Las operaciones de ciclo de vida validan la clave y el estado del plugin")
    void validaOperacionesDeCicloDeVida() {
        assertFalse(microkernel.unregisterPlugin("no_existe"));
        assertThrows(IllegalArgumentException.class, () -> microkernel.activatePlugin("no_existe"));
        assertThrows(IllegalArgumentException.class, () -> microkernel.registerPlugin(" ", "java.lang.String"));

        microkernel.registerPlugin("malo", "co.edu.unicauca.NoExiste");
        assertThrows(IllegalStateException.class, () -> microkernel.activatePlugin("malo"));
    }

    @Test
    @DisplayName("El núcleo cuenta las ejecuciones de cada plugin")
    void cuentaLasEjecuciones() {
        microkernel.executePlugin("MULTIPLE_CHOICE", TestRequests.validMultipleChoice());
        microkernel.executePlugin("MULTIPLE_CHOICE", TestRequests.validMultipleChoice());

        assertEquals(2, microkernel.getPluginDescriptor("multiple_choice").orElseThrow().getExecutions());
        assertEquals(0, microkernel.getPluginDescriptor("case").orElseThrow().getExecutions());
    }

    @Test
    @DisplayName("Aísla los errores inesperados de un plugin")
    void aislaErroresDelPlugin() {
        microkernel.registerPlugin("failing", "co.edu.unicauca.microkernel.support.FailingPlugin");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> microkernel.executePlugin("FAILING", TestRequests.validMultipleChoice()));

        assertTrue(ex.getMessage().contains("failing"));
        assertEquals(0, microkernel.getQuestionCount());
    }

    // ----------------------- Banco de preguntas -----------------------

    @Test
    @DisplayName("Almacena en el banco la pregunta generada por el plugin")
    void almacenaLaPreguntaGenerada() {
        Question question = microkernel.executePlugin("MULTIPLE_CHOICE", TestRequests.validMultipleChoice());

        assertEquals(1, microkernel.getQuestionCount());
        assertEquals(question, microkernel.getQuestion(question.getId()).orElseThrow());
        assertEquals("Diseño de software", question.getCompetency());
    }

    @Test
    @DisplayName("Toma el tipo de la propia solicitud cuando no se indica")
    void ejecutaUsandoElTipoDeLaSolicitud() {
        Question question = microkernel.executePlugin(TestRequests.validCase());
        assertEquals("CASE", question.getType());
    }

    @Test
    @DisplayName("Una pregunta rechazada por el pipeline no se almacena")
    void noAlmacenaPreguntasInvalidas() {
        QuestionRequest invalida = TestRequests.validMultipleChoice().toBuilder().correctAnswer("Otra cosa").build();

        QuestionValidationException ex = assertThrows(QuestionValidationException.class,
                () -> microkernel.executePlugin("MULTIPLE_CHOICE", invalida));

        assertEquals("CorrectAnswerValidationFilter", ex.getFilterName());
        assertEquals(0, microkernel.getQuestionCount());
    }

    @Test
    @DisplayName("Rechaza tipos de pregunta sin plugin registrado")
    void rechazaTipoSinPlugin() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> microkernel.executePlugin("ENSAYO", TestRequests.validMultipleChoice()));

        assertTrue(ex.getMessage().contains("ENSAYO"));
        assertThrows(IllegalArgumentException.class,
                () -> microkernel.executePlugin("  ", TestRequests.validMultipleChoice()));
    }

    @Test
    @DisplayName("Permite consultar el banco por tipo de pregunta")
    void consultaPorTipo() {
        microkernel.executePlugin(TestRequests.validMultipleChoice());
        microkernel.executePlugin(TestRequests.validCase());
        microkernel.executePlugin(TestRequests.validMultimedia());

        assertEquals(3, microkernel.getQuestionCount());
        assertEquals(1, microkernel.findQuestionsByType("CASE").size());
        assertEquals(1, microkernel.findQuestionsByType("multimedia").size());
        assertTrue(microkernel.findQuestionsByType("ENSAYO").isEmpty());
    }

    @Test
    @DisplayName("El banco de preguntas no se puede modificar desde afuera")
    void bancoDePreguntasInmutable() {
        microkernel.executePlugin(TestRequests.validMultipleChoice());
        assertThrows(UnsupportedOperationException.class, () -> microkernel.getQuestions().clear());
    }
}
