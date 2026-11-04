package ch.loete.backend.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.loete.backend.config.JwtTokenProvider;
import ch.loete.backend.domain.service.AuthService;
import ch.loete.backend.process.repository.UserRepository;
import ch.loete.backend.web.dto.request.LoginRequest;
import ch.loete.backend.web.dto.request.RefreshRequest;
import ch.loete.backend.web.dto.request.RegisterRequest;
import ch.loete.backend.web.dto.response.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AuthService authService;

  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @MockitoBean private UserRepository userRepository;

  @Test
  void register_returns201() throws Exception {
    AuthResponse authResponse =
        new AuthResponse("access-token", "refresh-token", "user0001", "testuser", "test@test.com");

    when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"test@test.com","username":"testuser","password":"password123"}
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
        .andExpect(jsonPath("$.userId").value("user0001"))
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@test.com"));
  }

  @Test
  void register_returns400ForInvalidEmail() throws Exception {
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"not-an-email","username":"testuser","password":"password123"}
                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void register_returns400ForShortUsername() throws Exception {
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"test@test.com","username":"ab","password":"password123"}
                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void register_returns400ForShortPassword() throws Exception {
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"test@test.com","username":"testuser","password":"12345"}
                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_returns200() throws Exception {
    AuthResponse authResponse =
        new AuthResponse("access-token", "refresh-token", "user0001", "testuser", "test@test.com");

    when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"test@test.com","password":"password123"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
  }

  @Test
  void login_returns400ForBlankEmail() throws Exception {
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"","password":"password123"}
                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void refresh_returns200() throws Exception {
    AuthResponse authResponse =
        new AuthResponse(
            "new-access-token", "new-refresh-token", "user0001", "testuser", "test@test.com");

    when(authService.refresh(any(RefreshRequest.class))).thenReturn(authResponse);

    mockMvc
        .perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"refreshToken":"old-refresh-token"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("new-access-token"))
        .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
  }

  @Test
  void logout_returns200() throws Exception {
    doNothing().when(authService).logout("refresh-token");

    mockMvc
        .perform(
            post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"refreshToken":"refresh-token"}
                    """))
        .andExpect(status().isOk());
  }
}
