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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAndTasksTest extends IntegrationTestBase{

    @Autowired private MockMvc mvc;

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Long adminId;
    private Long userId;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        adminId = userRepository
                .save(new User("admin@test.com", passwordEncoder.encode("admin1234"), User.Role.ADMIN))
                .getId();

        userId = userRepository
                .save(new User("user@test.com", passwordEncoder.encode("user1234"), User.Role.USER))
                .getId();
    }

    @Test
    void tasksEndpoint_requiresJwt_and_acceptsValidToken() throws Exception {
        // 1) Without token -> 401
        mvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isUnauthorized());

        // 2) Login -> token
        String token = loginAndGetToken("admin@test.com", "admin1234");

        // 3) With token -> 200
        mvc.perform(get("/api/v1/tasks")
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

        String json = mvc.perform(post("/api/v1/auth/login")
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

    @Test
    void taskCreation_respectsOwnership_and_roles() throws Exception {

        // Tokens
        String adminToken = loginAndGetToken("admin@test.com", "admin1234");
        String userToken  = loginAndGetToken("user@test.com", "user1234");

        // 1) USER tries to create for another user -> ownership is enforced (server ignores userId)
        mvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("""
                    {
                      "title": "illegal task",
                      "userId": %d
                    }
                    """).formatted(adminId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId));

        // 2) USER creates a task for themselves -> 201 OK
        mvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "own task"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId));

        // 3) ADMIN creates a task for any user -> 201 OK
        mvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("""
                        {
                          "title": "admin task",
                          "userId": %d
                        }
                        """).formatted(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    void createTask_rejectsInvalidUserId() throws Exception {

        String adminToken = loginAndGetToken("admin@test.com", "admin1234");

        mvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "invalid userId",
                          "userId": 0
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_rejectsInvalidCredentials() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "admin@test.com",
                          "password": "wrong-password"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void protectedEndpoint_rejectsInvalidToken() throws Exception {
        mvc.perform(get("/api/v1/tasks")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTask_rejectsEmptyTitle() throws Exception {
        String adminToken = loginAndGetToken("admin@test.com", "admin1234");

        mvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": ""
                        }
                        """))
                .andExpect(status().isBadRequest());
    }


}