package io.viktor.backend.auth;

import io.viktor.backend.auth.dto.AuthResponse;
import io.viktor.backend.auth.dto.LoginRequest;
import io.viktor.backend.security.JwtService;
import io.viktor.backend.users.User;
import io.viktor.backend.users.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Timer loginTimer;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            MeterRegistry meterRegistry)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;

        this.loginSuccessCounter = Counter.builder("auth_login_success_total")
                .description("Total successful login attempts")
                .register(meterRegistry);

        this.loginFailureCounter = Counter.builder("auth_login_failure_total")
                .description("Total failed login attempts")
                .register(meterRegistry);

        this.loginTimer = Timer.builder("auth_login_duration")
                .description("Time spent processing login requests")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    public AuthResponse login(LoginRequest req) {
        return loginTimer.record(() -> {
            User user = userRepository.findByEmail(req.email())
                    .orElseThrow(() -> {
                        loginFailureCounter.increment();
                        return new IllegalArgumentException("Invalid credentials");
                    });

            if (!passwordEncoder.matches(req.password(), user.getPassword())) {
                loginFailureCounter.increment();
                throw new IllegalArgumentException("Invalid credentials");
            }

            loginSuccessCounter.increment();
            return new AuthResponse(jwtService.generateToken(user));
        });
    }
}
