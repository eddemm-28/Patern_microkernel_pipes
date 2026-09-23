package co.edu.unicauca.microkernel.presentation;

/**
 * Bitácora donde la interfaz registra lo que hacen el núcleo y el pipeline.
 */
@FunctionalInterface
public interface EventLog {

    void log(String message);
}
