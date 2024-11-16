package com.sublinks.sublinksapi.person.services;

import com.sublinks.sublinksapi.authorization.entities.Role;
import com.sublinks.sublinksapi.authorization.services.RoleService;
import com.sublinks.sublinksapi.comment.repositories.CommentHistoryRepository;
import com.sublinks.sublinksapi.comment.services.CommentService;
import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.instance.models.LocalInstanceContext;
import com.sublinks.sublinksapi.language.entities.Language;
import com.sublinks.sublinksapi.person.entities.LinkPersonInstance;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.entities.PersonAggregate;
import com.sublinks.sublinksapi.person.enums.PostListingMode;
import com.sublinks.sublinksapi.person.events.PersonCreatedPublisher;
import com.sublinks.sublinksapi.person.events.PersonDeletedPublisher;
import com.sublinks.sublinksapi.person.events.PersonUpdatedPublisher;
import com.sublinks.sublinksapi.person.repositories.PersonAggregateRepository;
import com.sublinks.sublinksapi.person.repositories.PersonRepository;
import com.sublinks.sublinksapi.post.repositories.PostHistoryRepository;
import com.sublinks.sublinksapi.post.services.PostService;
import com.sublinks.sublinksapi.privatemessages.services.PrivateMessageService;
import com.sublinks.sublinksapi.utils.BaseUrlUtil;
import com.sublinks.sublinksapi.utils.KeyGeneratorUtil;
import com.sublinks.sublinksapi.utils.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class provides operations related to managing person entities.
 */
@Component
@RequiredArgsConstructor
public class PersonService {

  private final KeyGeneratorUtil keyGeneratorUtil;
  private final BaseUrlUtil baseUrlUtil;
  private final PersonRepository personRepository;
  private final PersonAggregateRepository personAggregateRepository;
  private final LocalInstanceContext localInstanceContext;
  private final PersonCreatedPublisher personCreatedPublisher;
  private final PersonUpdatedPublisher personUpdatedPublisher;
  private final PostService postService;
  private final PostHistoryRepository postHistoryRepository;
  private final CommentService commentService;
  private final CommentHistoryRepository commentHistoryRepository;
  private final PrivateMessageService privateMessageService;
  private final PersonDeletedPublisher personDeletedPublisher;
  private final RoleService roleService;

  /**
   * Retrieves the default language for posting for a given person and community.
   *
   * @param person    The person for whom to retrieve the default post language.
   * @param community The community for which the default post language is being retrieved.
   * @return An Optional containing the default Language for posting if found, or an empty Optional
   * otherwise.
   */
  @Transactional
  public Optional<Language> getPersonDefaultPostLanguage(final Person person,
      final Community community)
  {

    for (Language language : person.getLanguages()) {
      if (community.getLanguages()
          .contains(language)) {
        return Optional.of(language);
      }
    }
    return Optional.empty();
  }

  /**
   * Checks if the provided password matches the hashed password of the given person.
   *
   * @param person   The person for whom to check the password.
   * @param password The password to be checked.
   * @return True if the password matches, false otherwise.
   */
  public boolean isValidPersonPassword(final Person person, final String password) {

    PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    return passwordEncoder.matches(password, person.getPassword());
  }

  /**
   * Creates a new person.
   *
   * @param person The person object to be created.
   */
  @Transactional
  public void createPerson(final Person person) {

    PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    person.setPassword(passwordEncoder.encode(person.getPassword()));

    final KeyStore keys = keyGeneratorUtil.generate();
    person.setPublicKey(keys.publicKey());
    person.setPrivateKey(keys.privateKey());
    person.setLocal(true);
    person.setEmail(person.getEmail());
    // @todo: add email verification and send verification email on registration
    person.setEmailVerified(localInstanceContext.instance()
        .getInstanceConfig() == null || !localInstanceContext.instance()
        .getInstanceConfig()
        .isRequireEmailVerification());

    Role role = localInstanceContext.instance()
        .getDomain()
        .isEmpty() ? roleService.getAdminRole(() -> new RuntimeException("No Admin role found."))
        : roleService.getDefaultRegisteredRole(
            () -> new RuntimeException("No Registered role found."));
    person.setRole(role);

    final String userActorId = baseUrlUtil.getBaseUrl() + "/u/" + person.getName();
    person.setActorId(userActorId);

    person.setLinkPersonInstance(LinkPersonInstance.builder()
        .instance(localInstanceContext.instance())
        .person(person)
        .build());

    final List<Language> languages = new ArrayList<>(localInstanceContext.instance()
        .getLanguages());
    person.setLanguages(languages);
    personRepository.save(person);

    final PersonAggregate personAggregate = PersonAggregate.builder()
        .person(person)
        .build();
    personAggregateRepository.save(personAggregate);
    person.setPersonAggregate(personAggregate);

    personCreatedPublisher.publish(person);
  }

  /**
   * Updates a Person object by saving it to the person repository and publishing a personUpdated
   * event.
   *
   * @param person The Person object to be updated.
   */
  @Transactional
  public void updatePerson(final Person person) {

    personRepository.save(person);
    personUpdatedPublisher.publish(person);
  }

  /**
   * Encodes the given password using a password encoder.
   *
   * @param password The password to be encoded.
   * @return The encoded password.
   */
  public String encodePassword(final String password) {

    PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    return passwordEncoder.encode(password);
  }

  /**
   * Creates a new instance of a Person object with default values.
   *
   * @param name The name of the new user.
   * @return A new Person object with default values.
   */
  public Person getDefaultNewUser(final String name) {

    return Person.builder()
        .name(name)
        //.instance(localInstanceContext.instance())
        .password("")
        .displayName("")
        .activityPubId("")
        .avatarImageUrl("")
        .bannerImageUrl("")
        .biography("")
        .isShowAvatars(true)
        .isShowScores(true)
        .isShowAvatars(true)
        .isShowReadPosts(true)
        .isShowNsfw(true)
        .postListingType(PostListingMode.List)
        .build();
  }

  /**
   * Deletes a user account.
   *
   * @param person        The Person object representing the user account to be deleted.
   * @param deleteContent A boolean value indicating whether to delete the user's content (posts,
   *                      comments, messages, etc.).
   */
  @Transactional
  public void deleteUserAccount(final Person person, final boolean deleteContent) {

    if (deleteContent) {
      // Log results ? @todo
      postService.deleteAllPostsByPerson(person);
      postHistoryRepository.deleteAllByCreator(person);
      commentService.deleteAllCommentsByPerson(person);
      commentHistoryRepository.deleteAllByCreator(person);
      privateMessageService.deleteAllPrivateMessagesByPerson(person);
    }

    person.setBiography("*Permanently Deleted*");
    person.setDisplayName("Deleted User-%s".formatted(person.getId()));

    person.setDeleted(true);
    person.setBannerImageUrl("");
    person.setAvatarImageUrl("");

    personDeletedPublisher.publish(personRepository.save(person), deleteContent);
  }

  public void updatePassword(Person person, String newPassword) {

    person.setPassword(encodePassword(newPassword));
    personRepository.save(person);

  }
}
