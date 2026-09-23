package co.edu.unicauca.microkernel.pipeline.filters;

import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.common.util.TextNormalizer;
import co.edu.unicauca.microkernel.pipeline.base.QuestionFilter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Filtro adicional para preguntas con recursos multimedia.
 * <p>
 * Verifica que el tipo de recurso sea IMAGEN, AUDIO o VIDEO y que la URL o ruta
 * del recurso sea válida y tenga una extensión acorde con el tipo.
 * Los videos también se aceptan desde YouTube o Vimeo.
 */
public class MediaResourceValidationFilter implements QuestionFilter {

    public static final Map<String, List<String>> ALLOWED_EXTENSIONS = Map.of(
            "IMAGEN", List.of("png", "jpg", "jpeg", "gif", "svg", "webp"),
            "AUDIO", List.of("mp3", "wav", "ogg", "m4a"),
            "VIDEO", List.of("mp4", "webm", "avi", "mov", "mkv")
    );

    private static final List<String> VIDEO_HOSTS = List.of("youtube.com", "youtu.be", "vimeo.com");

    @Override
    public QuestionRequest process(QuestionRequest request) {
        if (TextNormalizer.isBlank(request.getMediaType())) {
            throw fail("Debe indicar el tipo de recurso multimedia (IMAGEN, AUDIO o VIDEO).");
        }
        String mediaType = request.getMediaType().trim().toUpperCase(Locale.ROOT);
        List<String> extensions = ALLOWED_EXTENSIONS.get(mediaType);
        if (extensions == null) {
            throw fail("Tipo de recurso multimedia no soportado: " + request.getMediaType().trim() + ".");
        }
        if (TextNormalizer.isBlank(request.getMediaUrl())) {
            throw fail("Debe indicar la URL o la ruta del recurso multimedia.");
        }

        String mediaUrl = request.getMediaUrl().trim();
        String path = mediaUrl;
        if (mediaUrl.matches("(?i)^https?://.*")) {
            URI uri = parse(mediaUrl);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            if ("VIDEO".equals(mediaType) && VIDEO_HOSTS.stream().anyMatch(host::endsWith)) {
                return withMedia(request, mediaType, mediaUrl);
            }
            path = uri.getPath() == null ? "" : uri.getPath();
        }

        String extension = extensionOf(path);
        if (!extensions.contains(extension)) {
            throw fail("El recurso \"" + mediaUrl + "\" no es un archivo de tipo " + mediaType
                    + " (extensiones permitidas: " + String.join(", ", extensions) + ").");
        }
        return withMedia(request, mediaType, mediaUrl);
    }

    @Override
    public String getDescription() {
        return "recurso multimedia con tipo y formato válidos";
    }

    private URI parse(String url) {
        try {
            URI uri = new URI(url);
            if (uri.getHost() == null) {
                throw fail("La URL del recurso no es válida: " + url);
            }
            return uri;
        } catch (URISyntaxException ex) {
            throw fail("La URL del recurso no es válida: " + url);
        }
    }

    private static String extensionOf(String path) {
        int dot = path.lastIndexOf('.');
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (dot < 0 || dot < slash || dot == path.length() - 1) {
            return "";
        }
        return path.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static QuestionRequest withMedia(QuestionRequest request, String mediaType, String mediaUrl) {
        return request.toBuilder()
                .mediaType(mediaType)
                .mediaUrl(mediaUrl)
                .build();
    }

    private QuestionValidationException fail(String reason) {
        return new QuestionValidationException(getName(), reason);
    }
}
