package com.sublinks.sublinksapi.person.repositories;

import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.entities.PersonMetaData;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserDataRepository extends JpaRepository<PersonMetaData, Long> {

  List<PersonMetaData> findFirstByPersonAndIpAddress(Person person, String ipAddress);

  Optional<PersonMetaData> findFirstByPersonAndIpAddressAndUserAgentAndActiveIsTrue(Person person,
      String ipAddress, String userAgent);

  Optional<PersonMetaData> findFirstByPersonAndTokenAndIpAddressAndUserAgentAndActiveIsTrue(
      Person person, String token, String ipAddress, String userAgent);

  Optional<PersonMetaData> findFirstByPersonAndTokenAndIpAddressAndActiveIsTrue(Person person,
      String token, String ipAddress);

  Optional<PersonMetaData> findFirstByPersonAndTokenAndActiveIsTrue(Person person, String token);


  List<PersonMetaData> findAllByPersonAndLastUsedAtBefore(Person person, Date lastUsedAt);

  List<PersonMetaData> findAllByLastUsedAtBefore(Date lastUsedAt);

  List<PersonMetaData> findAllByPerson(Person person);

  @Modifying
  @Query("update PersonMetaData u set u.active = false where u.person = :person")
  void updateAllByPersonSetActiveToFalse(@Param(value = "person") Person person);

  @Modifying
  @Query(
      "update PersonMetaData u set u.active = false where u.token = :token")
  void updateAllByPersonAndIpAddressSetActiveToFalse(@Param(value = "token") String token);

  Optional<PersonMetaData> findByToken(String token);
}

