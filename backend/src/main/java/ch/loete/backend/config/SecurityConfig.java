package ch.loete.backend.config;

import ch.loete.backend.process.repository.UserRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Zentrale Sicherheitskonfiguration der Anwendung.
 *
 * <p>Konfiguriert Spring Security mit zwei Filter-Ketten:
 *
 * <ul>
 *   <li>{@code /internal/**} — OIDC-geschützt für Cloud-Scheduler-Aufrufe (nur Prod-Profil)
 *   <li>Alle übrigen Pfade — JWT-basierte, zustandsloses App-Authentifizierung mit CORS
 * </ul>
 */
@Configuration
@EnableWebSecurity
@org.springframework.context.annotation.Profile("!test")
public class SecurityConfig {

  /** Google OIDC Issuer für Cloud-Scheduler-Tokens. */
  private static final String GOOGLE_ISSUER = "https://accounts.google.com";

  /** Googles JWKS-Endpunkt zur Validierung der OIDC-Signaturen. */
  private static final String GOOGLE_JWKS_URI = "https://www.googleapis.com/oauth2/v3/certs";

  /** Kommaseparierte Liste der erlaubten CORS-Origins aus der Konfiguration. */
  @Value("${app.cors.allowed-origins}")
  private String allowedOrigins;

  /** Erwartete Audience im OIDC-Token von Cloud Scheduler. */
  @Value("${app.scheduler.audience}")
  private String schedulerAudience;

  /** JWT-Authentifizierungsfilter, der vor dem Standard-Authentifizierungsfilter eingefügt wird. */
  @org.springframework.context.annotation.Lazy
  @org.springframework.beans.factory.annotation.Autowired
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  /**
   * Filter-Kette für {@code /internal/**}: Validiert Google-OIDC-Tokens (Issuer und Audience). Wird
   * nur im Prod-Profil aktiviert; in Dev/Test fallen Requests durch die Haupt-Kette und sind dort
   * offen (permitAll), damit lokale Tests die Endpunkte direkt aufrufen können.
   *
   * @param http das HttpSecurity-Konfigurationsobjekt
   * @return die konfigurierte SecurityFilterChain
   * @throws Exception bei Konfigurationsfehlern
   */
  @Bean
  @Order(1)
  @org.springframework.context.annotation.Profile("prod")
  public SecurityFilterChain internalFilterChain(HttpSecurity http) throws Exception {
    return http.securityMatcher("/internal/**")
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(googleOidcJwtDecoder())))
        .build();
  }

  /**
   * Erstellt einen Spring-{@link JwtDecoder}, der Google-OIDC-Tokens validiert: Signatur über
   * Googles JWKS, Issuer gleich {@code https://accounts.google.com}, Audience gleich {@code
   * app.scheduler.audience}.
   *
   * @return der konfigurierte JWT-Decoder
   */
  private JwtDecoder googleOidcJwtDecoder() {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWKS_URI).build();
    decoder.setJwtValidator(
        JwtValidators.createDefaultWithValidators(
            new org.springframework.security.oauth2.jwt.JwtIssuerValidator(GOOGLE_ISSUER),
            new org.springframework.security.oauth2.core.OAuth2TokenValidator<>() {
              @Override
              public org.springframework.security.oauth2.core.OAuth2TokenValidatorResult validate(
                  org.springframework.security.oauth2.jwt.Jwt jwt) {
                if (jwt.getAudience() != null && jwt.getAudience().contains(schedulerAudience)) {
                  return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
                      .success();
                }
                return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                    new org.springframework.security.oauth2.core.OAuth2Error(
                        "invalid_token", "Required audience missing", null));
              }
            }));
    return decoder;
  }

  /**
   * Haupt-Filter-Kette mit JWT-Authentifizierung und Zugriffsregeln.
   *
   * <p>Öffentliche Endpunkte: Auth, Events (GET), Kategorien (GET), Locations (GET), Suche (POST),
   * Actuator-Health, Swagger-UI. In Nicht-Prod-Profilen sind auch {@code /internal/**} offen
   * (permitAll), damit Entwickler die Job-Endpunkte ohne OIDC-Token aufrufen können.
   *
   * @param http das HttpSecurity-Konfigurationsobjekt
   * @return die konfigurierte SecurityFilterChain
   * @throws Exception bei Konfigurationsfehlern
   */
  @Bean
  @Order(2)
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/auth/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/events/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/categories/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/locations/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/search/**")
                    .permitAll()
                    .requestMatchers("/internal/**")
                    .permitAll()
                    .requestMatchers("/actuator/health", "/actuator/info")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  /**
   * Erstellt den BCrypt-PasswordEncoder für die Passwort-Hashung.
   *
   * @return eine BCryptPasswordEncoder-Instanz
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Stellt den AuthenticationManager für die Login-Authentifizierung bereit.
   *
   * @param config die AuthenticationConfiguration
   * @return der konfigurierte AuthenticationManager
   * @throws Exception bei Konfigurationsfehlern
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Erstellt einen UserDetailsService, der Benutzer anhand ihrer E-Mail-Adresse lädt.
   *
   * @param userRepository das Repository für den Datenbankzugriff auf Benutzer
   * @return der konfigurierte UserDetailsService
   */
  @Bean
  public UserDetailsService userDetailsService(UserRepository userRepository) {
    return email -> {
      ch.loete.backend.domain.model.User user =
          userRepository
              .findByEmail(email)
              .orElseThrow(
                  () -> new UsernameNotFoundException("User not found with email: " + email));
      return User.builder()
          .username(user.getEmail())
          .password(user.getPasswordHash())
          .authorities(Collections.emptyList())
          .build();
    };
  }

  /**
   * Erstellt die CORS-Konfiguration basierend auf den erlaubten Origins.
   *
   * @return die konfigurierte CorsConfigurationSource
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    List<String> origins =
        Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    config.setAllowedOriginPatterns(origins);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("Location"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
