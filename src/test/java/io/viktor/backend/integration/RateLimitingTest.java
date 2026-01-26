package io.viktor.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateLimitingTest extends IntegrationTestBase {

    @Autowired private MockMvc mvc;

    @Test
    void loginRateLimit_blocksAfterTenRequestsPerMinute() throws Exception {

        String body = """
                {
                  "email": "admin@test.com",
                  "password": "wrong"
                }
                """;

        // First 10 attempts should go through to auth (invalid creds => 400)
        for (int i = 1; i <= 10; i++) {
            mvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().exists("X-Rate-Limit-Limit"))
                    .andExpect(header().exists("X-Rate-Limit-Remaining"));
        }

        // 11th attempt must be rate-limited
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }
}