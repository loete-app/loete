package ch.loete.backend.domain.service;

import ch.loete.backend.config.JwtTokenProvider;
import ch.loete.backend.domain.exception.AuthenticationException;
import ch.loete.backend.domain.exception.DuplicateResourceException;
import ch.loete.backend.domain.model.RefreshToken;
import ch.loete.backend.domain.model.User;
import ch.loete.backend.process.repository.RefreshTokenRepository;
import ch.loete.backend.process.repository.UserRepository;
import ch.loete.backend.web.dto.request.LoginRequest;
import ch.loete.backend.web.dto.request.RefreshRequest;
import ch.loete.backend.web.dto.request.RegisterRequest;
import ch.loete.backend.web.dto.response.AuthResponse;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthenticationManager authenticationManager;

  @Value("${app.jwt.refresh-expiration-ms}")
  private long refreshExpirationMs;

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())
        || userRepository.existsByUsername(request.username())) {
      throw new DuplicateResourceException("An account with this email or username already exists");
    }

    User user =
        User.builder()
            .email(request.email())
            .username(request.username())
            .passwordHash(passwordEncoder.encode(request.password()))
            .build();
    user = userRepository.save(user);

    return buildAuthResponse(user);
  }

  @Transactional
  public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password()));

    User user =
        userRepository
            .findByEmail(request.email())
            .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

    return buildAuthResponse(user);
  }

  @Transactional
  public AuthResponse refresh(RefreshRequest request) {
    RefreshToken refreshToken =
        refreshTokenRepository
            .findByToken(request.refreshToken())
            .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

    if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
      refreshTokenRepository.delete(refreshToken);
      throw new AuthenticationException("Refresh token expired");
    }

    User user = refreshToken.getUser();
    refreshTokenRepository.delete(refreshToken);

    return buildAuthResponse(user);
  }

  @Transactional
  public void logout(String refreshToken) {
    refreshTokenRepository.deleteByToken(refreshToken);
  }

  private AuthResponse buildAuthResponse(User user) {
    String accessToken =
        jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getEmail());

    RefreshToken refreshToken =
        RefreshToken.builder()
            .token(UUID.randomUUID().toString())
            .user(user)
            .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
            .build();
    refreshToken = refreshTokenRepository.save(refreshToken);

    return new AuthResponse(
        accessToken, refreshToken.getToken(), user.getId(), user.getUsername(), user.getEmail());
  }
}
