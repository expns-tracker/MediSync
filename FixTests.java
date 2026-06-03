import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class FixTests {
    public static void main(String[] args) throws IOException {
        Path dir = Paths.get("microservices/core-service/src/test/java/com/medisync/MediSync/controller");
        try (Stream<Path> paths = Files.list(dir)) {
            paths.filter(Files::isRegularFile).filter(p -> p.toString().endsWith("IT.java")).forEach(p -> {
                try {
                    String content = Files.readString(p);
                    content = content.replace("org.springframework.boot.test.mock.mockito.MockBean", "org.springframework.test.context.bean.override.mockito.MockitoBean");
                    content = content.replace("org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc", "org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc");
                    content = content.replace("newPass456", "NewPass456!");
                    content = content.replace("\"123\"", "\"NewPass123!\"");
                    content = content.replace("\"password123\"", "\"Password123!\"");
                    content = content.replace("\"wrongPassword\"", "\"WrongPass123!\"");
                    
                    if (!content.contains("addFilters = false")) {
                        content = content.replace("@AutoConfigureMockMvc", "@AutoConfigureMockMvc(addFilters = false)");
                    }
                    
                    if (!content.contains("import org.springframework.context.annotation.Import;")) {
                        content = content.replace("@SpringBootTest", "@SpringBootTest\n@org.springframework.context.annotation.Import(com.medisync.MediSync.config.TestSecurityConfig.class)");
                    }

                    Files.writeString(p, content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        
        Path appTest = Paths.get("microservices/core-service/src/test/java/com/medisync/MediSync/MediSyncApplicationTests.java");
        if (Files.exists(appTest)) {
            String content = Files.readString(appTest);
            content = content.replace("org.springframework.boot.test.mock.mockito.MockBean", "org.springframework.test.context.bean.override.mockito.MockitoBean");
            Files.writeString(appTest, content);
        }
    }
}