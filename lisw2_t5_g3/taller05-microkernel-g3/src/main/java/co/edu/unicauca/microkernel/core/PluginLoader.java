package co.edu.unicauca.microkernel.core;

import co.edu.unicauca.microkernel.common.interfaces.QuestionPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Fábrica de plugins basada en REFLEXIÓN (misma idea del DeliveryPluginManager del
 * ejemplo de envío de paquetes visto en clase).
 * <p>
 * Lee el archivo de configuración y crea las instancias a partir del nombre de la
 * clase, sin que el núcleo conozca las clases concretas en tiempo de compilación.
 */
public class PluginLoader {

    /**
     * Lee un archivo de configuración de plugins desde el classpath.
     *
     * @param resourceName nombre del recurso, por ejemplo "plugins.properties"
     * @return las entradas del archivo (clave, clase), en el mismo orden en que aparecen
     * @throws IOException si el archivo no existe o no se puede leer
     */
    public Map<String, String> readConfiguration(String resourceName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = PluginLoader.class.getClassLoader();
        }
        try (InputStream input = classLoader.getResourceAsStream(resourceName)) {
            if (input == null) {
                throw new FileNotFoundException("No se encontró el archivo " + resourceName + " en el classpath.");
            }
            OrderedProperties properties = new OrderedProperties();
            try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
            return Collections.unmodifiableMap(properties.entriesInOrder);
        }
    }

    /**
     * Crea una instancia del plugin usando reflexión:
     * {@code Class.forName(className).getDeclaredConstructor().newInstance()}.
     *
     * @param className nombre completo de la clase del plugin
     * @return la instancia del plugin
     * @throws PluginLoadException si la clase no existe, no cumple el contrato
     *                             {@link QuestionPlugin} o no se puede instanciar
     */
    public QuestionPlugin instantiate(String className) throws PluginLoadException {
        if (className == null || className.isBlank()) {
            throw new PluginLoadException("No se indicó el nombre de la clase del plugin.");
        }
        String name = className.trim();
        try {
            // 1. Obtener una referencia a la clase a partir de su nombre.
            Class<?> pluginClass = Class.forName(name);

            // 2. Verificar que cumpla el contrato común de los plugins.
            if (!QuestionPlugin.class.isAssignableFrom(pluginClass)) {
                throw new PluginLoadException("La clase " + name + " no implementa el contrato QuestionPlugin.");
            }
            if (pluginClass.isInterface() || Modifier.isAbstract(pluginClass.getModifiers())) {
                throw new PluginLoadException("La clase " + name + " es abstracta o es una interfaz y no se puede instanciar.");
            }

            // 3. Crear el objeto con el constructor sin parámetros.
            Object instance = pluginClass.getDeclaredConstructor().newInstance();
            return QuestionPlugin.class.cast(instance);

        } catch (ClassNotFoundException ex) {
            throw new PluginLoadException("No se encontró la clase " + name + " en el classpath.", ex);
        } catch (NoSuchMethodException ex) {
            throw new PluginLoadException("La clase " + name + " no tiene un constructor sin parámetros.", ex);
        } catch (InvocationTargetException ex) {
            throw new PluginLoadException("El constructor de " + name + " lanzó un error: "
                    + ex.getCause(), ex.getCause());
        } catch (InstantiationException | IllegalAccessException | LinkageError ex) {
            throw new PluginLoadException("No fue posible crear una instancia de " + name + ": " + ex, ex);
        }
    }

    /**
     * {@link Properties} que recuerda el orden en que se leyeron las entradas del archivo.
     */
    private static final class OrderedProperties extends Properties {
        private final transient Map<String, String> entriesInOrder = new LinkedHashMap<>();

        @Override
        public synchronized Object put(Object key, Object value) {
            entriesInOrder.put(String.valueOf(key), String.valueOf(value));
            return super.put(key, value);
        }
    }
}
