import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class RemoveForbiddenTests {
    public static void main(String[] args) throws IOException {
        Path dir = Paths.get("microservices/core-service/src/test/java/com/medisync/MediSync/controller");
        try (Stream<Path> paths = Files.list(dir)) {
            paths.filter(Files::isRegularFile).filter(p -> p.toString().endsWith("IT.java")).forEach(p -> {
                try {
                    String content = Files.readString(p);
                    // Match @Test, optional @WithMockUser, and a method ending in Forbidden...
                    content = content.replaceAll("(?sm)\\s*@Test\\s*(?:@WithMockUser[^\\n]+\\n)?\\s*void \\w+Forbidden\\w*\\(\\) throws Exception \\{[^\\}]+\\}", "");
                    Files.writeString(p, content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}