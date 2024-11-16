package com.sublinks.sublinksapi.api.sublinks.v1.authentication;

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
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Order(1)
public class SublinksJwtFilter extends OncePerRequestFilter {

  private final SublinksJwtUtil sublinksJwtUtil;
  private final PersonRepository personRepository;
  private final UserDataService userDataService;

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
      final HttpServletResponse response, final FilterChain filterChain)
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
        userName = sublinksJwtUtil.extractUsername(token);
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

      if (sublinksJwtUtil.validateToken(token, person.get())) {
        // Add a check if token and ip was changed? To give like a "warning" to the user that he has a new ip logged into his account
        userDataService.checkAndAddIpRelation(person.get(), request.getRemoteAddr(), token,
            request.getHeader("User-Agent"));
        final SublinksJwtPerson authenticationToken = new SublinksJwtPerson(person.get(),
            person.get()
                .getAuthorities(), token);
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
    return !servletPath.startsWith("/api/v1") && !servletPath.startsWith("/pictrs");
  }
}
