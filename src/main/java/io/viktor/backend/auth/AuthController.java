package io.viktor.backend.auth;

import io.viktor.backend.auth.dto.AuthResponse;
import io.viktor.backend.auth.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.viktor.backend.common.web.ApiPaths;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }
}
