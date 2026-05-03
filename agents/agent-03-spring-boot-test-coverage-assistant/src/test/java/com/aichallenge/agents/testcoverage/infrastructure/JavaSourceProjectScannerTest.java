package com.aichallenge.agents.testcoverage.infrastructure;

import com.aichallenge.agents.testcoverage.domain.ComponentCategory;
import com.aichallenge.agents.testcoverage.domain.SpringComponent;
import com.aichallenge.agents.testcoverage.domain.TestInventory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaSourceProjectScannerTest {
    @TempDir
    Path projectPath;

    @Test
    void detectsSpringComponentsAndExistingWebMvcTests() throws IOException {
        write("src/main/java/com/example/users/UserController.java", """
            package com.example.users;

            import org.springframework.web.bind.annotation.GetMapping;
            import org.springframework.web.bind.annotation.RestController;

            @RestController
            class UserController {
                @GetMapping("/users/{id}")
                UserResponse findById() {
                    return new UserResponse("test@example.com");
                }
            }
            """);
        write("src/main/java/com/example/users/UserService.java", """
            package com.example.users;

            import org.springframework.stereotype.Service;

            @Service
            class UserService {
                String normalize(String email) {
                    if (email == null) {
                        throw new IllegalArgumentException("email is required");
                    }
                    return email.trim().toLowerCase();
                }
            }
            """);
        write("src/main/java/com/example/users/UserResponse.java", """
            package com.example.users;

            record UserResponse(String email) {
            }
            """);
        write("src/test/java/com/example/users/UserControllerTest.java", """
            package com.example.users;

            import org.junit.jupiter.api.Test;
            import org.springframework.beans.factory.annotation.Autowired;
            import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
            import org.springframework.test.web.servlet.MockMvc;

            @WebMvcTest(UserController.class)
            class UserControllerTest {
                @Autowired
                MockMvc mockMvc;

                @Test
                void findsUser() {
                }
            }
            """);

        JavaSourceProjectScanner scanner = new JavaSourceProjectScanner();
        List<SpringComponent> components = scanner.scanProductionComponents(projectPath, null);
        TestInventory testInventory = scanner.scanTests(projectPath, components);

        assertEquals(3, components.size());
        assertTrue(components.stream().anyMatch(component -> component.category() == ComponentCategory.CONTROLLER));
        assertTrue(components.stream().anyMatch(component -> component.className().equals("UserService") && component.hasRealLogic()));
        assertTrue(testInventory.hasWebMvcCoverage("UserController"));
    }

    private void write(String relativePath, String content) throws IOException {
        Path file = projectPath.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }
}
