package com.sublinks.sublinksapi.person.services;

import com.sublinks.sublinksapi.common.interfaces.ILinkingService;
import com.sublinks.sublinksapi.person.entities.LinkPersonPerson;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.enums.LinkPersonPersonType;
import com.sublinks.sublinksapi.person.events.LinkPersonPersonCreatedPublisher;
import com.sublinks.sublinksapi.person.events.LinkPersonPersonDeletedPublisher;
import com.sublinks.sublinksapi.person.events.LinkPersonPersonUpdatedPublisher;
import com.sublinks.sublinksapi.person.repositories.LinkPersonPersonRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LinkPersonPersonService implements
    ILinkingService<LinkPersonPerson, Person, Person, LinkPersonPersonType> {


  private final LinkPersonPersonRepository linkPersonPersonRepository;
  private final LinkPersonPersonCreatedPublisher linkPersonPersonCreatedPublisher;
  private final LinkPersonPersonUpdatedPublisher linkPersonPersonUpdatedPublisher;
  private final LinkPersonPersonDeletedPublisher linkPersonPersonDeletedPublisher;

  @Override
  public Optional<LinkPersonPerson> getLink(Person fromPerson, Person toPerson,
      LinkPersonPersonType linkType) {

    if (toPerson != null && toPerson.isAdmin() && linkType.equals(LinkPersonPersonType.blocked)) {
      return Optional.empty();
    }
    return linkPersonPersonRepository.getLinkPersonPersonByFromPersonAndToPersonAndLinkType(
        fromPerson, toPerson, linkType);
  }

  @Override
  public List<LinkPersonPerson> getLinks(Person person) {

    return linkPersonPersonRepository.getLinkPersonPeopleByFromPerson(person);
  }

  @Override
  public List<LinkPersonPerson> getLinks(Person person, LinkPersonPersonType linkPersonPersonType) {

    return linkPersonPersonRepository.getLinkPersonPersonByFromPersonAndLinkType(person,
        linkPersonPersonType);
  }

  @Override
  public List<LinkPersonPerson> getLinks(Person person,
      List<LinkPersonPersonType> linkPersonPersonTypes) {

    return linkPersonPersonRepository.getLinkPersonPeopleByFromPersonAndLinkTypeIn(person,
        linkPersonPersonTypes);
  }

  @Override
  public List<LinkPersonPerson> getLinksByEntity(Person fromPerson, Person toPerson) {

    return this.linkPersonPersonRepository.getLinkPersonPeopleByFromPersonAndToPerson(fromPerson,
        toPerson);
  }

  @Override
  public List<LinkPersonPerson> getLinksByEntity(Person fromPerson,
      List<LinkPersonPersonType> linkPersonPersonTypes) {

    return linkPersonPersonRepository.getLinkPersonPeopleByToPersonAndLinkTypeIn(fromPerson,
        linkPersonPersonTypes);
  }

  @Override
  public List<LinkPersonPerson> getLinksByEntity(Person person) {

    return linkPersonPersonRepository.getLinkPersonPeopleByToPerson(person);
  }

  @Override
  public boolean hasLink(Person fromPerson, Person toPerson, LinkPersonPersonType linkType) {

    return getLink(fromPerson, toPerson, linkType).isPresent();
  }

  @Override
  public boolean hasAnyLink(Person fromPerson, Person toPerson,
      List<LinkPersonPersonType> linkPersonPersonTypes) {

    return this.linkPersonPersonRepository.getLinkPersonPeopleByFromPersonAndToPersonAndLinkTypeIn(
            fromPerson, toPerson, linkPersonPersonTypes)
        .isPresent();
  }

  public LinkPersonPerson createPersonLink(final Person fromPerson, final Person toPerson,
      final LinkPersonPersonType type) {

    final LinkPersonPerson link = LinkPersonPerson.builder()
        .fromPerson(fromPerson)
        .toPerson(toPerson)
        .linkType(type)
        .build();

    this.createLink(link);
    return link;
  }

  @Override
  public void createLink(LinkPersonPerson link) {

    linkPersonPersonRepository.saveAndFlush(link);
    linkPersonPersonCreatedPublisher.publish(link);
  }

  @Override
  public void createLinks(List<LinkPersonPerson> links) {

    linkPersonPersonRepository.saveAllAndFlush(links)
        .forEach(linkPersonPersonCreatedPublisher::publish);
  }

  @Override
  public void updateLink(LinkPersonPerson link) {

    this.linkPersonPersonRepository.saveAndFlush(link);

    this.linkPersonPersonUpdatedPublisher.publish(link);

  }

  @Override
  public void updateLinks(List<LinkPersonPerson> links) {

    this.linkPersonPersonRepository.saveAllAndFlush(links)
        .forEach(linkPersonPersonUpdatedPublisher::publish);
  }

  @Override
  public void deleteLink(LinkPersonPerson link) {

    linkPersonPersonRepository.delete(link);
    linkPersonPersonDeletedPublisher.publish(link);
  }

  @Override
  public void deleteLink(Person fromPerson, Person toPerson,
      LinkPersonPersonType linkPersonPersonType) {

    final Optional<LinkPersonPerson> linkPersonPersonOptional = linkPersonPersonRepository.deleteLinkPersonPersonByFromPersonAndToPersonAndLinkType(
        fromPerson, toPerson, linkPersonPersonType);
    linkPersonPersonOptional.ifPresent(linkPersonPersonDeletedPublisher::publish);
  }

  @Override
  public void deleteLinks(List<LinkPersonPerson> linkPersonPeople) {

    linkPersonPeople.forEach(link -> {

      linkPersonPersonRepository.delete(link);

      linkPersonPersonDeletedPublisher.publish(link);
    });

  }
}
