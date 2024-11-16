package com.sublinks.sublinksapi.person.services;

import com.sublinks.sublinksapi.common.interfaces.ILinkingService;
import com.sublinks.sublinksapi.person.entities.LinkPersonPost;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.enums.LinkPersonPostType;
import com.sublinks.sublinksapi.person.events.LinkPersonPostCreatedPublisher;
import com.sublinks.sublinksapi.person.events.LinkPersonPostDeletedPublisher;
import com.sublinks.sublinksapi.person.events.LinkPersonPostUpdatedPublisher;
import com.sublinks.sublinksapi.person.repositories.LinkPersonPostRepository;
import com.sublinks.sublinksapi.post.entities.Post;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LinkPersonPostService implements
    ILinkingService<LinkPersonPost, Post, Person, LinkPersonPostType> {

  private final LinkPersonPostRepository linkPersonPostRepository;
  private final LinkPersonPostCreatedPublisher linkPersonPostCreatedPublisher;
  private final LinkPersonPostDeletedPublisher linkPersonPostDeletedPublisher;
  private final LinkPersonPostUpdatedPublisher linkPersonPostUpdatedPublisher;

  @Override
  public boolean hasLink(Post post, Person person, LinkPersonPostType linkPersonPostType) {

    return linkPersonPostRepository.getLinkPersonPostByPostAndPersonAndLinkType(post, person,
            linkPersonPostType)
        .isPresent();
  }

  @Override
  public boolean hasAnyLink(Post post, Person person,
      List<LinkPersonPostType> linkPersonPostTypes) {

    return linkPersonPostRepository.getLinkPersonPostByPostAndPersonAndLinkTypeIn(post, person,
            linkPersonPostTypes)
        .isPresent();
  }

  @Transactional
  public LinkPersonPost createPostLink(final Post post, final Person person,
      final LinkPersonPostType type) {

    final LinkPersonPost link = LinkPersonPost.builder()
        .post(post)
        .person(person)
        .linkType(type)
        .build();

    this.createLink(link);
    return link;
  }

  @Transactional
  @Override
  public void createLink(LinkPersonPost link) {

    linkPersonPostRepository.saveAndFlush(link);

    linkPersonPostCreatedPublisher.publish(link);
  }

  @Transactional
  @Override
  public void createLinks(List<LinkPersonPost> linkPersonPosts) {

    linkPersonPostRepository.saveAllAndFlush(linkPersonPosts)
        .forEach((link) -> {

          this.linkPersonPostCreatedPublisher.publish(link);
        });
  }

  @Transactional
  @Override
  public void updateLink(LinkPersonPost link) {

    this.linkPersonPostRepository.saveAndFlush(link);

    this.linkPersonPostUpdatedPublisher.publish(link);
  }

  @Transactional
  @Override
  public void updateLinks(List<LinkPersonPost> linkPersonPosts) {

    this.linkPersonPostRepository.saveAllAndFlush(linkPersonPosts)
        .forEach((link) -> {

          linkPersonPostUpdatedPublisher.publish(link);
        });
  }

  @Transactional
  @Override
  public void deleteLink(LinkPersonPost link) {

    linkPersonPostRepository.delete(link);

    this.linkPersonPostDeletedPublisher.publish(link);
  }

  @Override
  public void deleteLink(Post post, Person person, LinkPersonPostType linkPersonPostType) {

    final Optional<LinkPersonPost> linkPersonPostOptional = linkPersonPostRepository.deleteLinkPersonPostByPostAndPersonAndLinkType(
        post, person, linkPersonPostType);

    if (linkPersonPostOptional.isPresent()) {
      final LinkPersonPost link = linkPersonPostOptional.get();


      this.linkPersonPostDeletedPublisher.publish(link);
    }
  }

  @Transactional
  @Override
  public void deleteLinks(List<LinkPersonPost> linkPersonPosts) {

    this.linkPersonPostRepository.deleteAll(linkPersonPosts);
    linkPersonPosts.forEach((link) -> {

      this.linkPersonPostDeletedPublisher.publish(link);
    });
  }

  @Override
  public Optional<LinkPersonPost> getLink(Post post, Person person,
      LinkPersonPostType linkPersonPostType) {

    return this.linkPersonPostRepository.getLinkPersonPostByPostAndPersonAndLinkType(post, person,
        linkPersonPostType);
  }

  @Override
  public List<LinkPersonPost> getLinks(Person person) {

    return this.linkPersonPostRepository.getLinkPersonPostByPerson(person);
  }

  @Override
  public List<LinkPersonPost> getLinks(Person person, LinkPersonPostType linkPersonPostType) {

    return this.linkPersonPostRepository.getLinkPersonPostByPersonAndLinkType(person,
        linkPersonPostType);
  }

  @Override
  public List<LinkPersonPost> getLinks(Person person,
      List<LinkPersonPostType> linkPersonPostTypes) {

    return this.linkPersonPostRepository.getLinkPersonPostByPersonAndLinkTypeIn(person,
        linkPersonPostTypes);
  }

  @Override
  public List<LinkPersonPost> getLinksByEntity(Post post, Person person) {

    return this.linkPersonPostRepository.getLinkPersonPostByPostAndPerson(post, person);
  }

  @Override
  public List<LinkPersonPost> getLinksByEntity(Post post,
      List<LinkPersonPostType> linkPersonPostTypes) {

    return this.linkPersonPostRepository.getLinkPersonPostByPostAndLinkTypeIn(post,
        linkPersonPostTypes);
  }

  @Override
  public List<LinkPersonPost> getLinksByEntity(Post post) {

    return this.linkPersonPostRepository.getLinkPersonPostByPost(post);
  }
}