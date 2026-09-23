package co.edu.unicauca.microkernel.core;

/**
 * Error al cargar un plugin por reflexión.
 */
public class PluginLoadException extends Exception {

    public PluginLoadException(String message) {
        super(message);
    }

    public PluginLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
