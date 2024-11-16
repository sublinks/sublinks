package com.sublinks.sublinksapi.api.sublinks.v1.common.controllers;

import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtPerson;
import com.sublinks.sublinksapi.person.entities.Person;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract class AbstractSublinksApiController {

  /**
   * Get the person object or throw a 400 Bad Request exception.
   *
   * @param principal JwtPerson object that contains the person as it's principal
   * @return Person
   * @throws ResponseStatusException Exception thrown when Person not present
   */
  public Person getPersonOrThrowBadRequest(SublinksJwtPerson principal)
      throws ResponseStatusException
  {

    return Optional.ofNullable(principal)
        .map(p -> (Person) p.getPrincipal())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
  }

  /**
   * Get the person object or throw a 400 Bad Request exception.
   *
   * @param principal JwtPerson object that contains the person as it's principal
   * @return Person
   * @throws ResponseStatusException Exception thrown when Person not present
   */
  public <X extends Throwable> Person getPersonOrThrow(SublinksJwtPerson principal,
      Supplier<? extends X> exceptionSupplier) throws X
  {

    return Optional.ofNullable(principal)
        .map(p -> (Person) p.getPrincipal())
        .orElseThrow(
            exceptionSupplier);
  }


  /**
   * Get the person object or throw a 401 Unauthorized exception.
   *
   * @param principal JwtPerson object that contains the person as it's principal
   * @return Person
   * @throws ResponseStatusException Exception thrown when Person not present
   */
  public Person getPersonOrThrowUnauthorized(SublinksJwtPerson principal)
      throws ResponseStatusException
  {

    return Optional.ofNullable(principal)
        .map(p -> (Person) p.getPrincipal())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  }

  public Person getPerson(SublinksJwtPerson principal) {

    return getOptionalPerson(principal).orElse(null);
  }

  public Optional<Person> getOptionalPerson(SublinksJwtPerson principal) {

    return Optional.ofNullable(principal)
        .map(p -> (Person) p.getPrincipal());
  }
}
