package ch.loete.backend.web.controller;

import ch.loete.backend.domain.service.AuthService;
import ch.loete.backend.web.dto.request.LoginRequest;
import ch.loete.backend.web.dto.request.RefreshRequest;
import ch.loete.backend.web.dto.request.RegisterRequest;
import ch.loete.backend.web.dto.response.AuthResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
    return ResponseEntity.ok(authService.refresh(request));
  }

  @PostMapping("/logout")
  public ResponseEntity<Map<String, String>> logout(@RequestBody RefreshRequest request) {
    authService.logout(request.refreshToken());
    return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
  }
}
