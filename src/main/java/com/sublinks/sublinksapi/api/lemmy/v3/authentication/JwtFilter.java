package com.sublinks.sublinksapi.api.lemmy.v3.authentication;

import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.repositories.PersonRepository;
import com.sublinks.sublinksapi.person.services.UserDataService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JwtFilter is a filter that performs authentication and authorization based on JWT (JSON Web
 * Token). It extracts the JWT from the request header or cookie, validates it, and sets the
 * authentication in the Spring Security context if it is valid. It also provides a method for
 * deciding whether to filter a request based on the servlet path.
 */
@Component
@RequiredArgsConstructor
@Order(1)
public class JwtFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final PersonRepository personRepository;
  private final UserDataService userDataService;

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
      @NonNull final HttpServletResponse response, @NonNull final FilterChain filterChain)
      throws ServletException, IOException
  {

    String authorizingToken = request.getHeader("Authorization");

    if (authorizingToken == null && request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (cookie.getName()
            .equals("jwt")) {
          authorizingToken = cookie.getValue();
          break;
        }
      }

    }

    String token = null;
    String userName = null;

    try {
      if (authorizingToken != null) {
        if (authorizingToken.startsWith("Bearer ")) {
          token = authorizingToken.substring(7);
        } else {
          token = authorizingToken;
        }
        userName = jwtUtil.extractUsername(token);
      }
    } catch (ExpiredJwtException | SignatureException ex) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "invalid_token");
    }

    if (userName != null && SecurityContextHolder.getContext()
        .getAuthentication() == null) {
      final Optional<Person> person = personRepository.findOneByNameIgnoreCase(userName);
      if (person.isEmpty()) {
        throw new UsernameNotFoundException("Invalid name");
      }

      if (jwtUtil.validateToken(token, person.get())) {
        // Add a check if token and ip was changed? To give like a "warning" to the user that he has a new ip logged into his account
        userDataService.checkAndAddIpRelation(person.get(), request.getRemoteAddr(), token,
            request.getHeader("User-Agent"));
        final JwtPerson authenticationToken = new JwtPerson(person.get(), person.get()
            .getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext()
            .setAuthentication(authenticationToken);
      } else {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "invalid_token");
      }
    }
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

    String servletPath = request.getServletPath();
    return !servletPath.startsWith("/api/v3") && !servletPath.startsWith("/pictrs");
  }
}
