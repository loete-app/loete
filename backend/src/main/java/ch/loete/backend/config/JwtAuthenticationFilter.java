package ch.loete.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet-Filter für die JWT-basierte Authentifizierung.
 *
 * <p>Extrahiert das Bearer-Token aus dem Authorization-Header, validiert es und setzt bei Erfolg
 * den SecurityContext mit den Benutzerdaten. Requests an {@code /auth/**} werden übersprungen.
 */
@Component
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("!test")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  /** Provider für JWT-Token-Operationen (Generierung, Validierung, Parsing). */
  private final JwtTokenProvider jwtTokenProvider;

  /** Service zum Laden der Benutzerdetails anhand der E-Mail-Adresse. */
  private final UserDetailsService userDetailsService;

  /**
   * Führt die JWT-Authentifizierung für jeden eingehenden Request durch.
   *
   * <p>Extrahiert das Token, validiert es und setzt bei Erfolg den Spring-Security-Kontext mit dem
   * authentifizierten Benutzer.
   *
   * @param request der eingehende HTTP-Request
   * @param response die HTTP-Response
   * @param filterChain die Filter-Kette
   * @throws ServletException bei Servlet-Fehlern
   * @throws IOException bei Ein-/Ausgabefehlern
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String token = extractToken(request);

    if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
      String email = jwtTokenProvider.getEmailFromToken(token);
      UserDetails userDetails = userDetailsService.loadUserByUsername(email);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Bestimmt, ob der Filter für den gegebenen Request übersprungen werden soll.
   *
   * @param request der eingehende HTTP-Request
   * @return {@code true} wenn der Pfad mit {@code /auth/} beginnt
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return request.getServletPath().startsWith("/auth/");
  }

  /**
   * Extrahiert das JWT-Token aus dem Authorization-Header.
   *
   * @param request der eingehende HTTP-Request
   * @return das Token ohne "Bearer "-Praefix, oder {@code null} falls nicht vorhanden
   */
  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    return null;
  }
}
