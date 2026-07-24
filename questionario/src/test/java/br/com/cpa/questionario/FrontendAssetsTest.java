package br.com.cpa.questionario;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FrontendAssetsTest {

    @Test
    void templatesNaoUsamTailwindCdn() throws IOException {
        Path templates = Path.of("src/main/resources/templates");

        try (Stream<Path> files = Files.walk(templates)) {
            var templatesComCdn = files
                    .filter(path -> path.toString().endsWith(".html"))
                    .filter(path -> contemTailwindCdn(path))
                    .map(templates::relativize)
                    .map(Path::toString)
                    .toList();

            assertThat(templatesComCdn).isEmpty();
        }
    }

    private boolean contemTailwindCdn(Path path) {
        try {
            return Files.readString(path).contains("cdn.tailwindcss.com");
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel ler template: " + path, e);
        }
    }
}
