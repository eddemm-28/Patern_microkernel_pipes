package co.edu.unicauca.microkernel.pipeline.filters;

import co.edu.unicauca.microkernel.common.util.TextNormalizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Catálogo de áreas de conocimiento válidas para el banco de preguntas y la
 * competencia Saber PRO a la que pertenece cada una.
 * <p>
 * Las competencias específicas corresponden a los módulos que presentan los
 * estudiantes de Ingeniería de Sistemas; las genéricas son comunes a todos los
 * programas. El catálogo es ilustrativo y puede ampliarse sin tocar el núcleo.
 */
public final class CompetencyCatalog {

    public static final String SPECIFIC = "Específica";
    public static final String GENERIC = "Genérica";

    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();

    static {
        String design = "Diseño de software";
        add("Arquitectura de software", design, SPECIFIC);
        add("Ingeniería de software", design, SPECIFIC);
        add("Patrones de diseño", design, SPECIFIC);
        add("Programación orientada a objetos", design, SPECIFIC);
        add("Bases de datos", design, SPECIFIC);
        add("Estructuras de datos y algoritmos", design, SPECIFIC);
        add("Pruebas de software", design, SPECIFIC);
        add("Gestión de proyectos de software", "Formulación de proyectos de ingeniería", SPECIFIC);
        add("Matemáticas", "Pensamiento científico - Matemáticas y estadística", SPECIFIC);
        add("Estadística", "Pensamiento científico - Matemáticas y estadística", SPECIFIC);
        add("Lectura crítica", "Lectura crítica", GENERIC);
        add("Razonamiento cuantitativo", "Razonamiento cuantitativo", GENERIC);
        add("Competencias ciudadanas", "Competencias ciudadanas", GENERIC);
        add("Comunicación escrita", "Comunicación escrita", GENERIC);
        add("Inglés", "Inglés", GENERIC);
    }

    private CompetencyCatalog() {
    }

    private static void add(String area, String competency, String category) {
        ENTRIES.put(TextNormalizer.normalize(area), new Entry(area, competency, category));
    }

    /**
     * Busca un área en el catálogo sin distinguir mayúsculas, tildes ni espacios repetidos.
     */
    public static Optional<Entry> find(String area) {
        return Optional.ofNullable(ENTRIES.get(TextNormalizer.normalize(area)));
    }

    /** Nombres oficiales de todas las áreas del catálogo. */
    public static List<String> areas() {
        List<String> areas = new ArrayList<>();
        ENTRIES.values().forEach(entry -> areas.add(entry.getArea()));
        return Collections.unmodifiableList(areas);
    }

    /**
     * Entrada del catálogo: área, competencia Saber PRO y categoría (específica o genérica).
     */
    public static final class Entry {
        private final String area;
        private final String competency;
        private final String category;

        Entry(String area, String competency, String category) {
            this.area = area;
            this.competency = competency;
            this.category = category;
        }

        public String getArea() { return area; }

        public String getCompetency() { return competency; }

        public String getCategory() { return category; }
    }
}
