package co.edu.unicauca.microkernel.core;

/**
 * Estados del ciclo de vida de un plugin dentro del núcleo.
 * <pre>
 *   registrar ──► ACTIVO ◄──► INACTIVO
 *       │            └──────────┴──► (descargar) se elimina del núcleo
 *       └──► ERROR  (la clase no se pudo cargar por reflexión)
 * </pre>
 */
public enum PluginState {

    /** Cargado y disponible para atender solicitudes. */
    ACTIVO("Activo"),
    /** Cargado, pero deshabilitado: el núcleo no lo usa. */
    INACTIVO("Inactivo"),
    /** No se pudo cargar (clase inexistente, sin contrato, abstracta, etc.). */
    ERROR("Error de carga");

    private final String label;

    PluginState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
