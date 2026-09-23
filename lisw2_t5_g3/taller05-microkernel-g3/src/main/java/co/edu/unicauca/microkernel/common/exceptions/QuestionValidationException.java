package co.edu.unicauca.microkernel.common.exceptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Se lanza cuando una solicitud de pregunta no cumple una regla de validación.
 * Indica el filtro que la rechazó, el motivo y la traza de los filtros ejecutados.
 */
public class QuestionValidationException extends RuntimeException {

    private final String filterName;
    private final String reason;
    private final List<String> trace;

    public QuestionValidationException(String filterName, String reason) {
        this(filterName, reason, Collections.emptyList());
    }

    public QuestionValidationException(String filterName, String reason, List<String> trace) {
        super(filterName + ": " + reason);
        this.filterName = filterName;
        this.reason = reason;
        this.trace = trace == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(trace));
    }

    /** Nombre del filtro que rechazó la solicitud. */
    public String getFilterName() {
        return filterName;
    }

    /** Motivo del rechazo, sin el nombre del filtro. */
    public String getReason() {
        return reason;
    }

    /** Traza de filtros ejecutados hasta el rechazo. */
    public List<String> getTrace() {
        return trace;
    }
}
