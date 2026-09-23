package co.edu.unicauca.microkernel.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utilidades de texto compartidas por los filtros y el catálogo de competencias.
 * <p>
 * Permiten comparar textos sin tener en cuenta mayúsculas, tildes ni espacios
 * repetidos (por ejemplo "Arquitectura de Software" y "arquitectura  de software").
 */
public final class TextNormalizer {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern HORIZONTAL_SPACES = Pattern.compile("[ \\t\\x0B\\f]+");

    private TextNormalizer() {
    }

    /**
     * Normaliza un texto para comparaciones: minúsculas, sin tildes y con espacios simples.
     *
     * @param text texto original (puede ser nulo)
     * @return texto normalizado; cadena vacía si el texto es nulo
     */
    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String withoutMarks = DIACRITICS.matcher(Normalizer.normalize(text, Normalizer.Form.NFD)).replaceAll("");
        return WHITESPACE.matcher(withoutMarks.toLowerCase(Locale.ROOT)).replaceAll(" ").trim();
    }

    /**
     * Elimina espacios al inicio y al final y reemplaza espacios o tabulaciones repetidos
     * por un único espacio, conservando los saltos de línea.
     */
    public static String collapseSpaces(String text) {
        if (text == null) {
            return null;
        }
        return HORIZONTAL_SPACES.matcher(text.trim()).replaceAll(" ");
    }

    /** Indica si el texto es nulo o solo contiene espacios. */
    public static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    /** Cuenta las palabras de un texto (0 si es nulo o vacío). */
    public static int countWords(String text) {
        if (isBlank(text)) {
            return 0;
        }
        return WHITESPACE.split(text.trim()).length;
    }
}
