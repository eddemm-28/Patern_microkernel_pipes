package co.edu.unicauca.microkernel.pipeline.filters;

import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.support.TestRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Filtro MediaResourceValidationFilter")
class MediaResourceValidationFilterTest {

    private final MediaResourceValidationFilter filter = new MediaResourceValidationFilter();

    private static QuestionRequest media(String type, String url) {
        return TestRequests.validMultimedia().toBuilder().mediaType(type).mediaUrl(url).build();
    }

    @Test
    @DisplayName("Acepta una imagen publicada en una URL")
    void aceptaImagen() {
        QuestionRequest output = filter.process(media("imagen", "https://sitio.edu.co/uml/diagrama.PNG"));
        assertEquals("IMAGEN", output.getMediaType());
    }

    @Test
    @DisplayName("Acepta un audio almacenado en una ruta local")
    void aceptaAudioLocal() {
        assertEquals("AUDIO", filter.process(media("AUDIO", "recursos/listening-01.mp3")).getMediaType());
    }

    @Test
    @DisplayName("Acepta videos de YouTube aunque la URL no tenga extensión")
    void aceptaVideoYoutube() {
        assertEquals("VIDEO", filter.process(media("VIDEO", "https://www.youtube.com/watch?v=abc123")).getMediaType());
    }

    @Test
    @DisplayName("Rechaza un archivo cuya extensión no corresponde al tipo")
    void rechazaExtensionIncorrecta() {
        QuestionValidationException ex = assertThrows(QuestionValidationException.class,
                () -> filter.process(media("IMAGEN", "https://sitio.edu.co/audio.mp3")));
        assertTrue(ex.getReason().contains("extensiones permitidas"));
    }

    @Test
    @DisplayName("Rechaza un tipo de recurso no soportado")
    void rechazaTipoNoSoportado() {
        assertThrows(QuestionValidationException.class, () -> filter.process(media("PDF", "guia.pdf")));
    }

    @Test
    @DisplayName("Rechaza un recurso sin tipo o sin URL")
    void rechazaDatosFaltantes() {
        assertThrows(QuestionValidationException.class, () -> filter.process(media(null, "imagen.png")));
        assertThrows(QuestionValidationException.class, () -> filter.process(media("IMAGEN", " ")));
    }

    @Test
    @DisplayName("Rechaza una URL mal formada")
    void rechazaUrlMalFormada() {
        assertThrows(QuestionValidationException.class, () -> filter.process(media("IMAGEN", "http://")));
        assertThrows(QuestionValidationException.class, () -> filter.process(media("IMAGEN", "https://sitio con espacios/a.png")));
    }
}
