package com.sublinks.sublinksapi.api.lemmy.v3.common.controllers;

import com.sublinks.sublinksapi.api.lemmy.v3.authentication.JwtPerson;
import com.sublinks.sublinksapi.person.entities.Person;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Base controller for Lemmy api controllers. Contains repeated functions for example authentication
 * checks
 */
public abstract class AbstractLemmyApiController {

  /**
   * Get the person object or throw a 404 Bad Request exception.
   *
   * @param principal JwtPerson object that contains the person as it's principal
   * @return Person
   * @throws ResponseStatusException Exception thrown when Person not present
   */
  public Person getPersonOrThrowBadRequest(JwtPerson principal) throws ResponseStatusException {

    return Optional.ofNullable(principal).map(p -> (Person) p.getPrincipal())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
  }

  /**
   * Get the person object or throw a 400 Bad Request exception.
   *
   * @param principal JwtPerson object that contains the person as it's principal
   * @return Person
   * @throws ResponseStatusException Exception thrown when Person not present
   */
  public <X extends Throwable> Person getPersonOrThrow(JwtPerson principal,
                                                       Supplier<? extends X> exceptionSupplier) throws X {

    return Optional.ofNullable(principal).map(p -> (Person) p.getPrincipal())
        .orElseThrow(exceptionSupplier);
  }


  /**
   * Get the person object or throw a 401 Unauthorized exception.
   *
   * @param principal JwtPerson object that contains the person as it's principal
   * @return Person
   * @throws ResponseStatusException Exception thrown when Person not present
   */
  public Person getPersonOrThrowUnauthorized(JwtPerson principal) throws ResponseStatusException {

    return Optional.ofNullable(principal).map(p -> (Person) p.getPrincipal()).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)
    );
  }

  /**
   * Get the Person object or null.
   *
   * @param principal JwtPerson object that contains the person as its principal
   * @return Person object if present, otherwise null
   */
  public Person getPerson(JwtPerson principal) {

    return getOptionalPerson(principal).orElse(null);
  }

  /**
   * Get the optional Person object from the JwtPerson principal.
   *
   * @param principal the JwtPerson object that contains the Person as its principal
   * @return an Optional object that contains the Person if present, otherwise empty
   */
  public Optional<Person> getOptionalPerson(JwtPerson principal) {

    return Optional.ofNullable(principal).map(p -> (Person) p.getPrincipal());
  }
}
