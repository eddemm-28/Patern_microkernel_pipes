package co.edu.unicauca.microkernel.pipeline.base;

import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Tubería (pipeline) que conecta los filtros y transporta la solicitud de un filtro
 * al siguiente, en el orden en que fueron agregados.
 * <p>
 * Si un filtro rechaza la solicitud, la tubería se detiene y los filtros restantes
 * no se ejecutan.
 */
public class QuestionPipeline {

    /** Etiqueta usada en la traza cuando un filtro se ejecuta correctamente. */
    public static final String OK_MARK = "[OK]";
    /** Etiqueta usada en la traza cuando un filtro rechaza la solicitud. */
    public static final String FAIL_MARK = "[FALLO]";

    private final List<QuestionFilter> filters = new ArrayList<>();

    public QuestionPipeline() {
    }

    public QuestionPipeline(List<QuestionFilter> filters) {
        filters.forEach(this::addFilter);
    }

    /**
     * Agrega un filtro al final de la tubería.
     *
     * @return la misma tubería, para encadenar llamadas
     */
    public QuestionPipeline addFilter(QuestionFilter filter) {
        filters.add(Objects.requireNonNull(filter, "El filtro no puede ser nulo"));
        return this;
    }

    /** Filtros de la tubería en orden de ejecución (lista inmutable). */
    public List<QuestionFilter> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    /**
     * Ejecuta la solicitud a través de todos los filtros.
     *
     * @param request solicitud de entrada
     * @return el resultado de la ejecución, con la solicitud procesada y la traza
     */
    public PipelineResult execute(QuestionRequest request) {
        List<String> trace = new ArrayList<>();
        if (request == null) {
            trace.add(FAIL_MARK + " Entrada: la solicitud es nula");
            return PipelineResult.failure(null, "Entrada", "La solicitud es nula.", trace);
        }

        QuestionRequest current = request;
        for (QuestionFilter filter : filters) {
            try {
                QuestionRequest output = filter.process(current);
                if (output == null) {
                    String message = "El filtro no produjo ninguna salida.";
                    trace.add(FAIL_MARK + " " + filter.getName() + ": " + message);
                    return PipelineResult.failure(current, filter.getName(), message, trace);
                }
                trace.add(OK_MARK + " " + filter.getName() + describe(filter));
                current = output;
            } catch (QuestionValidationException ex) {
                trace.add(FAIL_MARK + " " + filter.getName() + ": " + ex.getReason());
                return PipelineResult.failure(current, filter.getName(), ex.getReason(), trace);
            }
        }
        return PipelineResult.success(current, trace);
    }

    private static String describe(QuestionFilter filter) {
        String description = filter.getDescription();
        return description == null || description.isBlank() ? "" : " - " + description;
    }
}
