package com.sublinks.sublinksapi.person.services;

import com.sublinks.sublinksapi.person.config.UserDataConfig;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.entities.PersonMetaData;
import com.sublinks.sublinksapi.person.events.UserDataCreatedEventPublisher;
import com.sublinks.sublinksapi.person.events.UserDataInvalidationEventPublisher;
import com.sublinks.sublinksapi.person.events.UserDataUpdatedPublisher;
import com.sublinks.sublinksapi.person.repositories.UserDataRepository;
import jakarta.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserDataService {

  private final UserDataRepository userDataRepository;
  private final UserDataConfig userDataConfig;
  private final UserDataCreatedEventPublisher userDataCreatedEventPublisher;
  private final UserDataUpdatedPublisher userDataUpdatedPublisher;
  private final UserDataInvalidationEventPublisher userDataInvalidationEventPublisher;


  public void invalidate(PersonMetaData personMetaData) {

    personMetaData.setActive(false);
    userDataRepository.saveAndFlush(personMetaData);
    userDataInvalidationEventPublisher.publish(personMetaData);
  }

  public void checkAndAddIpRelation(Person person, String ipAddress, String token,
      @Nullable String userAgent)
  {

    boolean saveUserData = userDataConfig.isSaveUserData();
    Optional<PersonMetaData> foundData = getActiveUserDataByPersonAndToken(person, token);

    if (foundData.isPresent()) {

      PersonMetaData personMetaData = foundData.get();
      if (saveUserData) {
        personMetaData.setIpAddress(ipAddress);
        if (userAgent != null) {
          personMetaData.setUserAgent(userAgent);
        }
      }
      personMetaData.setLastUsedAt(new Date());

      userDataUpdatedPublisher.publish(userDataRepository.saveAndFlush(personMetaData));
      return;
    }
    PersonMetaData personMetaData = PersonMetaData.builder()
        .person(person)
        .ipAddress(saveUserData ? ipAddress : null)
        .userAgent(saveUserData ? userAgent : null)
        .token(token)
        .active(true)
        .build();
    PersonMetaData createdPersonMetaData = userDataRepository.saveAndFlush(personMetaData);
    userDataCreatedEventPublisher.publish(createdPersonMetaData);
  }

  public Optional<PersonMetaData> getActiveUserDataByPersonAndToken(Person person, String token) {

    return userDataRepository.findFirstByPersonAndTokenAndActiveIsTrue(person, token);
  }

  private Optional<PersonMetaData> getActiveUserDataByPersonAndIpAddress(Person person,
      String token, String ipAddress, String userAgent)
  {

    return userDataRepository.findFirstByPersonAndTokenAndIpAddressAndUserAgentAndActiveIsTrue(
        person, token, ipAddress, userAgent);
  }

  @Transactional
  public void invalidateAllUserData(Person person) {

    userDataRepository.updateAllByPersonSetActiveToFalse(person);
    userDataRepository.flush();
    userDataInvalidationEventPublisher.publish(person);
  }

  public void invalidateUserData(PersonMetaData personMetaData) {

    invalidate(personMetaData);
  }

  public void clearUserDataBefore(Person person, Date lastUsedAt) {

    List<PersonMetaData> found = userDataRepository.findAllByPersonAndLastUsedAtBefore(person,
        lastUsedAt);
    found.forEach(this::invalidate);
  }

  public void clearUserDataBefore(Date lastUsedAt) {

    List<PersonMetaData> found = userDataRepository.findAllByLastUsedAtBefore(lastUsedAt);
    found.forEach(this::invalidate);
  }
}
