package co.edu.unicauca.microkernel.core;

import co.edu.unicauca.microkernel.common.entities.Question;
import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.common.interfaces.QuestionPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Núcleo (microkernel) del Banco de Preguntas Saber PRO.
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Almacenar el banco de preguntas en un {@code Map<String, Question>}.</li>
 *   <li>Registrar plugins leyendo {@code plugins.properties}.</li>
 *   <li>Cargar los plugins por reflexión (a través de {@link PluginLoader}).</li>
 *   <li>Ejecutar el plugin que soporta el tipo de pregunta solicitado.</li>
 *   <li>Gestionar el ciclo de vida de los plugins: registrar, activar, desactivar,
 *       descargar y recargar.</li>
 * </ul>
 * El núcleo solo depende del contrato {@link QuestionPlugin}; nunca referencia
 * clases concretas de plugins. Agregar un nuevo tipo de pregunta no requiere
 * modificar esta clase.
 */
public class QuestionMicrokernel {

    /** Archivo de configuración por defecto. */
    public static final String DEFAULT_CONFIGURATION = "plugins.properties";
    /** Prefijo de las claves de plugins dentro del archivo de configuración. */
    public static final String PLUGIN_PREFIX = "plugin.";

    private static final Logger LOGGER = Logger.getLogger(QuestionMicrokernel.class.getName());

    private final Map<String, Question> questions = new LinkedHashMap<>();
    private final Map<String, PluginDescriptor> plugins = new LinkedHashMap<>();
    private final PluginLoader loader;
    private final String configurationResource;
    private String configurationError;

    /** Crea el núcleo y carga los plugins registrados en {@code plugins.properties}. */
    public QuestionMicrokernel() {
        this(DEFAULT_CONFIGURATION);
    }

    /**
     * Crea el núcleo y carga los plugins del archivo de configuración indicado.
     *
     * @param configurationResource nombre del archivo de configuración en el classpath
     */
    public QuestionMicrokernel(String configurationResource) {
        this(configurationResource, new PluginLoader());
    }

    QuestionMicrokernel(String configurationResource, PluginLoader loader) {
        this.configurationResource = Objects.requireNonNull(configurationResource);
        this.loader = Objects.requireNonNull(loader);
        reloadPlugins();
    }

    // ------------------------------------------------------------------
    // Registro y ciclo de vida de plugins
    // ------------------------------------------------------------------

    /**
     * Descarga todos los plugins y vuelve a registrar los del archivo de configuración.
     * El banco de preguntas se conserva.
     *
     * @return los plugins registrados
     */
    public final List<PluginDescriptor> reloadPlugins() {
        plugins.clear();
        configurationError = null;
        Map<String, String> entries;
        try {
            entries = loader.readConfiguration(configurationResource);
        } catch (IOException ex) {
            configurationError = ex.getMessage();
            LOGGER.log(Level.WARNING, "No fue posible leer la configuración de plugins: {0}", ex.getMessage());
            return getPluginDescriptors();
        }

        // Los plugins se registran en el mismo orden en que aparecen en el archivo.
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            if (entry.getKey().startsWith(PLUGIN_PREFIX)) {
                String key = entry.getKey().substring(PLUGIN_PREFIX.length());
                registerPlugin(key, entry.getValue());
            }
        }
        return getPluginDescriptors();
    }

    /**
     * Registra un plugin en tiempo de ejecución creando la instancia por reflexión.
     * Si ya existía un plugin con la misma clave, se reemplaza.
     * <p>
     * Si la clase no se puede cargar, el plugin queda registrado en estado
     * {@link PluginState#ERROR} con la causa, y el núcleo sigue funcionando.
     *
     * @param key       clave del plugin (por ejemplo "multiple_choice")
     * @param className nombre completo de la clase
     * @return el descriptor del plugin registrado
     */
    public PluginDescriptor registerPlugin(String key, String className) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("La clave del plugin es obligatoria.");
        }
        String cleanKey = key.trim();
        String cleanClass = className == null ? "" : className.trim();

        PluginDescriptor descriptor;
        try {
            QuestionPlugin plugin = loader.instantiate(cleanClass);
            descriptor = PluginDescriptor.loaded(cleanKey, cleanClass, plugin);
            LOGGER.log(Level.FINE, "Plugin ''{0}'' cargado ({1})", new Object[]{plugin.getName(), cleanClass});
        } catch (PluginLoadException ex) {
            descriptor = PluginDescriptor.failed(cleanKey, cleanClass, ex.getMessage());
            LOGGER.log(Level.WARNING, "Plugin ''{0}'' no cargado: {1}", new Object[]{cleanKey, ex.getMessage()});
        }
        plugins.put(cleanKey, descriptor);
        return descriptor;
    }

    /** Activa un plugin previamente desactivado. */
    public void activatePlugin(String key) {
        PluginDescriptor descriptor = requireDescriptor(key);
        if (descriptor.getState() == PluginState.ERROR) {
            throw new IllegalStateException("El plugin '" + key + "' no se pudo cargar y no se puede activar.");
        }
        descriptor.changeState(PluginState.ACTIVO, "Activado");
    }

    /** Desactiva un plugin: sigue registrado, pero el núcleo deja de usarlo. */
    public void deactivatePlugin(String key) {
        PluginDescriptor descriptor = requireDescriptor(key);
        if (descriptor.getState() == PluginState.ERROR) {
            throw new IllegalStateException("El plugin '" + key + "' no se pudo cargar y no se puede desactivar.");
        }
        descriptor.changeState(PluginState.INACTIVO, "Desactivado");
    }

    /**
     * Descarga (elimina) un plugin del núcleo.
     *
     * @return {@code true} si el plugin existía
     */
    public boolean unregisterPlugin(String key) {
        return key != null && plugins.remove(key.trim()) != null;
    }

    /** Todos los plugins registrados, con su estado (lista inmutable). */
    public List<PluginDescriptor> getPluginDescriptors() {
        return Collections.unmodifiableList(new ArrayList<>(plugins.values()));
    }

    /** Descriptor de un plugin por su clave. */
    public Optional<PluginDescriptor> getPluginDescriptor(String key) {
        return key == null ? Optional.empty() : Optional.ofNullable(plugins.get(key.trim()));
    }

    /** Plugins activos, en orden de registro. */
    public List<QuestionPlugin> getActivePlugins() {
        return plugins.values().stream()
                .filter(PluginDescriptor::isActive)
                .map(PluginDescriptor::getPlugin)
                .collect(Collectors.toUnmodifiableList());
    }

    /** Plugin activo que soporta el tipo indicado, si existe. */
    public Optional<QuestionPlugin> findPlugin(String type) {
        return findActiveDescriptor(type).map(PluginDescriptor::getPlugin);
    }

    /** Mensaje de error al leer el archivo de configuración, o {@code null} si se leyó bien. */
    public String getConfigurationError() {
        return configurationError;
    }

    public String getConfigurationResource() {
        return configurationResource;
    }

    // ------------------------------------------------------------------
    // Ejecución de plugins
    // ------------------------------------------------------------------

    /**
     * Ejecuta el plugin que soporta el tipo de la solicitud.
     *
     * @see #executePlugin(String, QuestionRequest)
     */
    public Question executePlugin(QuestionRequest request) {
        Objects.requireNonNull(request, "La solicitud no puede ser nula");
        return executePlugin(request.getType(), request);
    }

    /**
     * Busca el plugin activo que soporta el tipo indicado, le pide generar la
     * pregunta y la agrega al banco.
     *
     * @param type    tipo de pregunta
     * @param request datos de la pregunta
     * @return la pregunta generada y almacenada
     * @throws IllegalArgumentException    si ningún plugin activo soporta el tipo
     * @throws QuestionValidationException si el plugin rechaza la solicitud (pipeline)
     * @throws IllegalStateException       si el plugin falla o no genera la pregunta
     */
    public Question executePlugin(String type, QuestionRequest request) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el tipo de pregunta.");
        }
        PluginDescriptor descriptor = findActiveDescriptor(type)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No hay ningún plugin registrado que soporte el tipo: " + type.trim()));

        QuestionPlugin plugin = descriptor.getPlugin();
        descriptor.registerExecution();
        Question question;
        try {
            question = plugin.generate(request);
        } catch (QuestionValidationException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            // Aislamiento de fallos: un error inesperado del plugin no detiene el núcleo.
            descriptor.updateMessage("Último error: " + ex);
            throw new IllegalStateException("El plugin '" + plugin.getName() + "' falló: " + ex.getMessage(), ex);
        }
        if (question == null) {
            throw new IllegalStateException("El plugin '" + plugin.getName() + "' no generó ninguna pregunta.");
        }

        questions.put(question.getId(), question);
        LOGGER.log(Level.FINE, "Pregunta agregada al banco con ID: {0}", question.getId());
        return question;
    }

    // ------------------------------------------------------------------
    // Banco de preguntas
    // ------------------------------------------------------------------

    /** Banco de preguntas (vista de solo lectura, en orden de creación). */
    public Map<String, Question> getQuestions() {
        return Collections.unmodifiableMap(questions);
    }

    /** Busca una pregunta por su identificador. */
    public Optional<Question> getQuestion(String id) {
        return Optional.ofNullable(questions.get(id));
    }

    /** Preguntas del tipo indicado (sin distinguir mayúsculas). */
    public List<Question> findQuestionsByType(String type) {
        return questions.values().stream()
                .filter(question -> question.getType() != null && question.getType().equalsIgnoreCase(type))
                .collect(Collectors.toUnmodifiableList());
    }

    /** Número de preguntas en el banco. */
    public int getQuestionCount() {
        return questions.size();
    }

    // ------------------------------------------------------------------

    private Optional<PluginDescriptor> findActiveDescriptor(String type) {
        return plugins.values().stream()
                .filter(PluginDescriptor::isActive)
                .filter(descriptor -> descriptor.getPlugin().supports(type))
                .findFirst();
    }

    private PluginDescriptor requireDescriptor(String key) {
        return getPluginDescriptor(key)
                .orElseThrow(() -> new IllegalArgumentException("No existe un plugin registrado con la clave: " + key));
    }
}
