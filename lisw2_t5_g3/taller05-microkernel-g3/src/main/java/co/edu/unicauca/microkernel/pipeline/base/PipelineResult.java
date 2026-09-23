package co.edu.unicauca.microkernel.pipeline.base;

import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resultado de ejecutar un {@link QuestionPipeline}: indica si la solicitud superó
 * todos los filtros, la salida del último filtro ejecutado y la traza del recorrido.
 */
public final class PipelineResult {

    private final boolean valid;
    private final QuestionRequest output;
    private final String failedFilter;
    private final String errorMessage;
    private final List<String> trace;

    private PipelineResult(boolean valid, QuestionRequest output, String failedFilter,
                           String errorMessage, List<String> trace) {
        this.valid = valid;
        this.output = output;
        this.failedFilter = failedFilter;
        this.errorMessage = errorMessage;
        this.trace = Collections.unmodifiableList(new ArrayList<>(trace));
    }

    public static PipelineResult success(QuestionRequest output, List<String> trace) {
        return new PipelineResult(true, output, null, null, trace);
    }

    public static PipelineResult failure(QuestionRequest lastOutput, String failedFilter,
                                         String errorMessage, List<String> trace) {
        return new PipelineResult(false, lastOutput, failedFilter, errorMessage, trace);
    }

    /** {@code true} si la solicitud pasó por todos los filtros sin errores. */
    public boolean isValid() {
        return valid;
    }

    /** Solicitud procesada (si es válida) o la última salida correcta antes del fallo. */
    public QuestionRequest getOutput() {
        return output;
    }

    /** Nombre del filtro que rechazó la solicitud ({@code null} si fue válida). */
    public String getFailedFilter() {
        return failedFilter;
    }

    /** Motivo del rechazo ({@code null} si fue válida). */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** Traza de ejecución de los filtros. */
    public List<String> getTrace() {
        return trace;
    }

    /** Convierte un resultado fallido en la excepción que se reporta al núcleo. */
    public QuestionValidationException toException() {
        if (valid) {
            throw new IllegalStateException("Un resultado válido no se puede convertir en excepción");
        }
        return new QuestionValidationException(failedFilter, errorMessage, trace);
    }
}
