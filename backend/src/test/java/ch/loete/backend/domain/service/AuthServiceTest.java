package ch.loete.backend.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private AuthenticationManager authenticationManager;

  @InjectMocks private AuthService authService;

  private User testUser;

  @BeforeEach
  void setUp() throws Exception {
    testUser =
        User.builder()
            .id("user0001")
            .email("test@test.com")
            .username("testuser")
            .passwordHash("$2a$10$encodedpassword")
            .build();

    // Set refreshExpirationMs via reflection
    Field field = AuthService.class.getDeclaredField("refreshExpirationMs");
    field.setAccessible(true);
    field.set(authService, 604800000L); // 7 days
  }

  @Test
  void register_createsUserAndReturnsTokens() {
    RegisterRequest request = new RegisterRequest("test@test.com", "testuser", "password123");

    given(userRepository.existsByEmail("test@test.com")).willReturn(false);
    given(userRepository.existsByUsername("testuser")).willReturn(false);
    given(passwordEncoder.encode("password123")).willReturn("$2a$10$encodedpassword");
    given(userRepository.save(any(User.class))).willReturn(testUser);
    given(jwtTokenProvider.generateToken("user0001", "testuser", "test@test.com"))
        .willReturn("access-token");
    given(refreshTokenRepository.save(any(RefreshToken.class)))
        .willAnswer(
            invocation -> {
              RefreshToken token = invocation.getArgument(0);
              return token;
            });

    AuthResponse result = authService.register(request);

    assertThat(result).isNotNull();
    assertThat(result.accessToken()).isEqualTo("access-token");
    assertThat(result.refreshToken()).isNotNull();
    assertThat(result.userId()).isEqualTo("user0001");
    assertThat(result.username()).isEqualTo("testuser");
    assertThat(result.email()).isEqualTo("test@test.com");
  }

  @Test
  void register_throwsDuplicateWhenEmailExists() {
    RegisterRequest request = new RegisterRequest("test@test.com", "testuser", "password123");

    given(userRepository.existsByEmail("test@test.com")).willReturn(true);

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void register_throwsDuplicateWhenUsernameExists() {
    RegisterRequest request = new RegisterRequest("test@test.com", "testuser", "password123");

    given(userRepository.existsByEmail("test@test.com")).willReturn(false);
    given(userRepository.existsByUsername("testuser")).willReturn(true);

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void login_authenticatesAndReturnsTokens() {
    LoginRequest request = new LoginRequest("test@test.com", "password123");

    given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .willReturn(new UsernamePasswordAuthenticationToken("test@test.com", null));
    given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(testUser));
    given(jwtTokenProvider.generateToken("user0001", "testuser", "test@test.com"))
        .willReturn("access-token");
    given(refreshTokenRepository.save(any(RefreshToken.class)))
        .willAnswer(
            invocation -> {
              RefreshToken token = invocation.getArgument(0);
              return token;
            });

    AuthResponse result = authService.login(request);

    assertThat(result).isNotNull();
    assertThat(result.accessToken()).isEqualTo("access-token");
    assertThat(result.refreshToken()).isNotNull();
    assertThat(result.userId()).isEqualTo("user0001");
  }

  @Test
  void login_throwsOnBadCredentials() {
    LoginRequest request = new LoginRequest("test@test.com", "wrong");

    given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .willThrow(new BadCredentialsException("Bad credentials"));

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void refresh_returnsNewTokens() {
    RefreshRequest request = new RefreshRequest("old-refresh-token");
    RefreshToken existingToken =
        RefreshToken.builder()
            .id(1L)
            .token("old-refresh-token")
            .user(testUser)
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

    given(refreshTokenRepository.findByToken("old-refresh-token"))
        .willReturn(Optional.of(existingToken));
    given(jwtTokenProvider.generateToken("user0001", "testuser", "test@test.com"))
        .willReturn("new-access-token");
    given(refreshTokenRepository.save(any(RefreshToken.class)))
        .willAnswer(
            invocation -> {
              RefreshToken token = invocation.getArgument(0);
              return token;
            });

    AuthResponse result = authService.refresh(request);

    assertThat(result).isNotNull();
    assertThat(result.accessToken()).isEqualTo("new-access-token");
    assertThat(result.refreshToken()).isNotNull();
    then(refreshTokenRepository).should().delete(existingToken);
  }

  @Test
  void refresh_throwsWhenTokenNotFound() {
    RefreshRequest request = new RefreshRequest("nonexistent");

    given(refreshTokenRepository.findByToken("nonexistent")).willReturn(Optional.empty());

    assertThatThrownBy(() -> authService.refresh(request))
        .isInstanceOf(AuthenticationException.class);
  }

  @Test
  void refresh_throwsWhenTokenExpired() {
    RefreshRequest request = new RefreshRequest("expired-token");
    RefreshToken expiredToken =
        RefreshToken.builder()
            .id(1L)
            .token("expired-token")
            .user(testUser)
            .expiresAt(Instant.now().minusSeconds(3600))
            .build();

    given(refreshTokenRepository.findByToken("expired-token"))
        .willReturn(Optional.of(expiredToken));

    assertThatThrownBy(() -> authService.refresh(request))
        .isInstanceOf(AuthenticationException.class);
    then(refreshTokenRepository).should().delete(expiredToken);
  }

  @Test
  void logout_deletesRefreshToken() {
    authService.logout("refresh-token");

    then(refreshTokenRepository).should().deleteByToken("refresh-token");
  }
}
