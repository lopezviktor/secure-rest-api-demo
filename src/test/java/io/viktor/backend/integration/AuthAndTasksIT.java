package io.viktor.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.viktor.backend.users.User;
import io.viktor.backend.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAndTasksIT {

    @Autowired private MockMvc mvc;

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        // Seed minimal users for tests (DataSeeder is dev-only)
        userRepository.save(new User("admin@test.com", passwordEncoder.encode("admin1234"), User.Role.ADMIN));
        userRepository.save(new User("user@test.com",  passwordEncoder.encode("user1234"),  User.Role.USER));
    }

    @Test
    void tasksEndpoint_requiresJwt_and_acceptsValidToken() throws Exception {
        // 1) Without token -> 401
        mvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());

        // 2) Login -> token
        String token = loginAndGetToken("admin@test.com", "admin1234");

        // 3) With token -> 200
        mvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String loginAndGetToken(String email, String password) throws Exception {

        String body = """
            {
              "email": "%s",
              "password": "%s"
            }
            """.formatted(email, password);

        String json = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);
        return node.get("token").asText();
    }
}