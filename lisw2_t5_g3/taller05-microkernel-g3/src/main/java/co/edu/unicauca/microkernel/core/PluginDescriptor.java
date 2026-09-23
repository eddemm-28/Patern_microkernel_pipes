package co.edu.unicauca.microkernel.core;

import co.edu.unicauca.microkernel.common.interfaces.QuestionPlugin;

import java.time.LocalDateTime;

/**
 * Información que el núcleo mantiene de cada plugin registrado: clave de registro,
 * clase, instancia creada por reflexión, estado del ciclo de vida y estadísticas.
 */
public class PluginDescriptor {

    private final String key;
    private final String className;
    private final QuestionPlugin plugin;
    private final LocalDateTime registeredAt;
    private PluginState state;
    private String message;
    private int executions;

    private PluginDescriptor(String key, String className, QuestionPlugin plugin, PluginState state, String message) {
        this.key = key;
        this.className = className;
        this.plugin = plugin;
        this.state = state;
        this.message = message;
        this.registeredAt = LocalDateTime.now();
    }

    static PluginDescriptor loaded(String key, String className, QuestionPlugin plugin) {
        return new PluginDescriptor(key, className, plugin, PluginState.ACTIVO, "Cargado por reflexión");
    }

    static PluginDescriptor failed(String key, String className, String message) {
        return new PluginDescriptor(key, className, null, PluginState.ERROR, message);
    }

    public String getKey() { return key; }

    public String getClassName() { return className; }

    /** Instancia del plugin; {@code null} si el plugin está en estado ERROR. */
    public QuestionPlugin getPlugin() { return plugin; }

    /** Nombre declarado por el plugin, o "-" si no se pudo cargar. */
    public String getPluginName() { return plugin == null ? "-" : plugin.getName(); }

    public PluginState getState() { return state; }

    public String getMessage() { return message; }

    public int getExecutions() { return executions; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }

    public boolean isActive() { return state == PluginState.ACTIVO; }

    void changeState(PluginState newState, String newMessage) {
        this.state = newState;
        this.message = newMessage;
    }

    void registerExecution() {
        executions++;
    }

    void updateMessage(String newMessage) {
        this.message = newMessage;
    }

    @Override
    public String toString() {
        return key + " -> " + className + " [" + state + "]";
    }
}
