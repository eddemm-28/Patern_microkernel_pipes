package co.edu.unicauca.microkernel.pipeline.base;

import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;

/**
 * Filtro del patrón Tuberías y Filtros.
 * <p>
 * Igual que en el ejemplo visto en clase ({@code Filter<T>: T process(T input)}),
 * cada filtro recibe un dato, lo procesa y entrega el resultado al siguiente filtro.
 * Aquí el dato es una {@link QuestionRequest}: el filtro la valida y devuelve una
 * versión normalizada o enriquecida. Si la solicitud no cumple sus reglas, el filtro
 * lanza {@link QuestionValidationException} y la tubería se detiene.
 */
@FunctionalInterface
public interface QuestionFilter {

    /**
     * Procesa la solicitud.
     *
     * @param request solicitud recibida del filtro anterior
     * @return solicitud (validada y posiblemente transformada) para el siguiente filtro
     * @throws QuestionValidationException si la solicitud no cumple las reglas del filtro
     */
    QuestionRequest process(QuestionRequest request);

    /** Nombre del filtro, usado en la traza del pipeline. */
    default String getName() {
        return getClass().getSimpleName();
    }

    /** Descripción corta de la responsabilidad del filtro. */
    default String getDescription() {
        return "";
    }
}
