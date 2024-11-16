package com.sublinks.sublinksapi.api.sublinks.v1.person.services;

import com.sublinks.sublinksapi.api.lemmy.v3.enums.RegistrationMode;
import com.sublinks.sublinksapi.api.sublinks.v1.authentication.SublinksJwtUtil;
import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SortOrder;
import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SublinksListingType;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.models.moderation.IndexBannedPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.person.enums.RegistrationState;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.CreatePerson;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.DeletePerson;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.IndexPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.LoginPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.LoginResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonAggregateResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonIdentity;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonSessionData;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonSessionDataResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.UpdatePerson;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.moderation.BanPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.utils.PersonKeyUtils;
import com.sublinks.sublinksapi.authorization.entities.Role;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionInstanceTypes;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionPersonTypes;
import com.sublinks.sublinksapi.authorization.services.AclService;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.authorization.services.RoleService;
import com.sublinks.sublinksapi.comment.services.CommentReportService;
import com.sublinks.sublinksapi.email.entities.Email;
import com.sublinks.sublinksapi.email.enums.EmailTemplatesEnum;
import com.sublinks.sublinksapi.email.services.EmailService;
import com.sublinks.sublinksapi.instance.entities.InstanceConfig;
import com.sublinks.sublinksapi.instance.models.LocalInstanceContext;
import com.sublinks.sublinksapi.language.repositories.LanguageRepository;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.entities.PersonAggregate;
import com.sublinks.sublinksapi.person.entities.PersonEmailVerification;
import com.sublinks.sublinksapi.person.entities.PersonMetaData;
import com.sublinks.sublinksapi.person.entities.PersonRegistrationApplication;
import com.sublinks.sublinksapi.person.enums.PersonRegistrationApplicationStatus;
import com.sublinks.sublinksapi.person.repositories.PersonAggregateRepository;
import com.sublinks.sublinksapi.person.repositories.PersonRegistrationApplicationRepository;
import com.sublinks.sublinksapi.person.repositories.PersonRepository;
import com.sublinks.sublinksapi.person.repositories.UserDataRepository;
import com.sublinks.sublinksapi.person.services.PersonEmailVerificationService;
import com.sublinks.sublinksapi.person.services.PersonRegistrationApplicationService;
import com.sublinks.sublinksapi.person.services.PersonService;
import com.sublinks.sublinksapi.person.services.UserDataService;
import com.sublinks.sublinksapi.post.services.PostReportService;
import com.sublinks.sublinksapi.privatemessages.services.PrivateMessageReportService;
import com.sublinks.sublinksapi.utils.PaginationUtils;
import com.sublinks.sublinksapi.utils.UrlUtil;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.context.Context;

@AllArgsConstructor
@Service
public class SublinksPersonService {

  private final PersonService personService;
  private final SublinksJwtUtil sublinksJwtUtil;
  private final PersonRepository personRepository;
  private final LocalInstanceContext localInstanceContext;
  private final EmailService emailService;
  private final PersonEmailVerificationService personEmailVerificationService;
  private final PersonRegistrationApplicationService personRegistrationApplicationService;
  private final PersonRegistrationApplicationRepository personRegistrationApplicationRepository;
  private final UserDataService userDataService;
  private final ConversionService conversionService;
  private final LanguageRepository languageRepository;
  private final RoleService roleService;
  private final RolePermissionService rolePermissionService;
  private final PersonAggregateRepository personAggregateRepository;
  private final PostReportService postReportService;
  private final CommentReportService commentReportService;
  private final PrivateMessageReportService privateMessageReportService;
  private final UserDataRepository userDataRepository;
  private final PersonKeyUtils personKeyUtils;
  private final UrlUtil urlUtil;
  private final AclService aclService;

  /**
   * Retrieves the person identifiers from the given key.
   *
   * @param key the key used to retrieve the person identifiers
   * @return the person identity object containing the name and domain
   */
  public PersonIdentity getPersonIdentifiersFromKey(String key) {

    String name, domain;
    if (key.contains("@")) {
      return personKeyUtils.getPersonIdentity(key);
    } else {
      name = key;
      domain = urlUtil.cleanUrlProtocol(localInstanceContext.instance()
          .getDomain());
    }

    return PersonIdentity.builder()
        .name(name)
        .domain(domain)
        .build();
  }

  /**
   * Retrieves a list of {@link PersonResponse} objects based on the provided search criteria.
   *
   * @param indexPerson A {@link IndexPerson} object containing the search parameters.
   * @param person      A {@link Person} object representing the authenticated user.
   * @return A list of {@link PersonResponse} objects that match the search criteria.
   * @throws ResponseStatusException If the authenticated user does not have permission to perform
   *                                 the action.
   */
  public List<PersonResponse> index(final IndexPerson indexPerson, final Person person) {

    aclService.canPerson(person)
        .performTheAction(RolePermissionPersonTypes.READ_USER)
        .orThrowUnauthorized();

    final int page = PaginationUtils.getPage(indexPerson.page() == null ? 0 : indexPerson.page())
        - 1;
    final int perPage = PaginationUtils.getPerPage(
        indexPerson.perPage() == null ? 20 : indexPerson.perPage()) - 1;

    if (indexPerson.search() == null) {
      return handleNullSearch(indexPerson, page, perPage);
    }

    return handleNonNullSearch(indexPerson, page, perPage);
  }

  /**
   * Handles a null search for person records based on the specified index person, page number, and
   * items per page.
   *
   * @param indexPerson the index person used for determining the type of listing
   * @param page        the page number for pagination
   * @param perPage     the number of items per page
   * @return a list of {@code PersonResponse} objects based on the search query
   */
  private List<PersonResponse> handleNullSearch(IndexPerson indexPerson, int page, int perPage) {

    if (indexPerson.listingType() == SublinksListingType.Local) {
      return personRepository.findAllByIsLocal(true, PageRequest.of(page, perPage))
          .stream()
          .map(p -> conversionService.convert(p, PersonResponse.class))
          .toList();
    }
    return personRepository.findAll(PageRequest.of(page, perPage))
        .stream()
        .map(p -> conversionService.convert(p, PersonResponse.class))
        .toList();
  }

  /**
   * Handles a non-null search for persons in the index.
   *
   * @param indexPerson the IndexPerson object containing the search parameters
   * @param page        the page number for pagination
   * @param perPage     the number of results per page for pagination
   * @return a List of PersonResponse objects that match the search criteria
   */
  private List<PersonResponse> handleNonNullSearch(IndexPerson indexPerson, int page, int perPage) {

    if (indexPerson.listingType() == SublinksListingType.Local) {
      return personRepository.findAllByNameAndBiographyAndLocal(indexPerson.search(), true,
              PageRequest.of(page, perPage))
          .stream()
          .map(p -> conversionService.convert(p, PersonResponse.class))
          .toList();
    }
    return personRepository.findAllByNameAndBiography(indexPerson.search(),
            PageRequest.of(page, perPage))
        .stream()
        .map(p -> conversionService.convert(p, PersonResponse.class))
        .toList();
  }

  /**
   * Retrieves a PersonResponse object based on the provided key.
   *
   * @param key The key containing the person's information. If the key contains "@", it is split
   *            into name and domain using "@" as the separator. Otherwise, the name is set as the
   *            key and the domain is obtained from the local instance context.
   * @return The PersonResponse object representing the person information.
   * @throws ResponseStatusException if the person is not found.
   */
  public PersonResponse show(final String key, final Person person) {

    rolePermissionService.isPermitted(person, RolePermissionPersonTypes.READ_USER,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final PersonIdentity ids = getPersonIdentifiersFromKey(key);

    return personRepository.findOneByNameAndInstance_Domain(ids.name(), ids.domain())
        .map(p -> conversionService.convert(p, PersonResponse.class))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_not_found"));
  }

  /**
   * Registers a person with the given details.
   *
   * @param createPersonForm The form containing the details of the person to be registered.
   * @param ip               The IP address of the client making the registration request.
   * @param userAgent        The user agent string of the client making the registration request.
   * @return The registration response containing the token, registration status, and error (if
   * any).
   * @throws ResponseStatusException if the email is required but not provided, or if the email send
   *                                 failed.
   */
  public LoginResponse registerPerson(final CreatePerson createPersonForm, final String ip,
      final String userAgent)
  {

    final InstanceConfig instanceConfig = localInstanceContext.instance()
        .getInstanceConfig();

    if (instanceConfig != null && instanceConfig.isRequireEmailVerification()) {
      if (createPersonForm.email()
          .isEmpty()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email_required");
      }
    }

    // Password gets encrypted inside .createPerson
    final Person.PersonBuilder personBuilder = Person.builder()
        .password(createPersonForm.password())
        .name(createPersonForm.name())
        .displayName(createPersonForm.displayName())
        .avatarImageUrl(createPersonForm.avatarImageUrl())
        .bannerImageUrl(createPersonForm.bannerImageUrl())
        .biography(createPersonForm.bio())
        .matrixUserId(createPersonForm.matrixUserId());

    final Person person = personBuilder.build();

    personService.createPerson(person);

    String token = sublinksJwtUtil.generateToken(person);
    RegistrationState status = RegistrationState.CREATED;

    if (instanceConfig != null && instanceConfig.isRequireEmailVerification()) {
      token = null;

      PersonEmailVerification personEmailVerification = personEmailVerificationService.create(
          person, ip, userAgent);

      Map<String, Object> params = emailService.getDefaultEmailParameters();

      params.put("person", person);
      params.put("verificationUrl", localInstanceContext.instance()
          .getDomain() + "/verify_email/" + personEmailVerification.getToken());
      try {
        final String template_name = EmailTemplatesEnum.VERIFY_EMAIL.toString();
        emailService.saveToQueue(Email.builder()
            .personRecipients(List.of(person))
            .subject(emailService.getSubjects()
                .get(template_name)
                .getAsString())
            .htmlContent(emailService.formatTextEmailTemplate(template_name,
                new Context(Locale.getDefault(), params)))
            .textContent(emailService.formatEmailTemplate(template_name,
                new Context(Locale.getDefault(), params)))
            .build());
        status = RegistrationState.VERIFICATION_EMAIL_SENT;
      } catch (Exception e) {
        personRepository.delete(person);
        return LoginResponse.builder()
            .status(RegistrationState.NOT_CREATED)
            .error("email_send_failed")
            .build();
      }
    }
    if (instanceConfig != null
        && instanceConfig.getRegistrationMode() == RegistrationMode.RequireApplication) {
      token = null;

      personRegistrationApplicationService.createPersonRegistrationApplication(
          PersonRegistrationApplication.builder()
              .applicationStatus(instanceConfig.isRequireEmailVerification()
                  ? PersonRegistrationApplicationStatus.inactive
                  : PersonRegistrationApplicationStatus.pending)
              .person(person)
              .question(instanceConfig.getRegistrationQuestion())
              .answer(createPersonForm.answer())
              .build());
      if (!instanceConfig.isRequireEmailVerification()) {
        status = RegistrationState.APPLICATION_CREATED;
      }
    }

    if (token != null) {
      userDataService.checkAndAddIpRelation(person, ip, token, userAgent);
    }

    return LoginResponse.builder()
        .token(token)
        .status(status)
        .build();
  }

  /**
   * Logs in a person with the given credentials.
   *
   * @param loginPersonForm The login details of the person.
   * @param ip              The IP address of the client making the login request.
   * @param userAgent       The user agent string of the client making the login request.
   * @return The login response containing the token, registration status, and error (if any).
   * @throws ResponseStatusException if the person is not found, or if the person is deleted, or if
   *                                 the person's email is not verified, or if the person's
   *                                 registration application is not approved, or if the password is
   *                                 incorrect.
   */
  public LoginResponse login(final LoginPerson loginPersonForm, final String ip,
      final String userAgent)
  {

    final Person person = personRepository.findOneByNameIgnoreCase(loginPersonForm.username())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_not_found"));

    rolePermissionService.isPermitted(person, RolePermissionPersonTypes.USER_LOGIN,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    if (!rolePermissionService.isPermitted(person, RolePermissionPersonTypes.USER_LOGIN)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    if (person.isDeleted()) {
      return LoginResponse.builder()
          .status(RegistrationState.UNCHANGED)
          .error("person_deleted")
          .build();
    }

    if (!person.isEmailVerified()) {
      return LoginResponse.builder()
          .status(RegistrationState.UNCHANGED)
          .error("email_not_verified")
          .build();
    }

    Optional<PersonRegistrationApplication> application = personRegistrationApplicationRepository.findOneByPerson(
        person);

    if (application.isPresent() && application.get()
        .getApplicationStatus() != PersonRegistrationApplicationStatus.approved) {
      return LoginResponse.builder()
          .status(RegistrationState.UNCHANGED)
          .error("application_not_approved")
          .build();
    }

    if (!personService.isValidPersonPassword(person, loginPersonForm.password())) {
      return LoginResponse.builder()
          .status(RegistrationState.UNCHANGED)
          .error("password_incorrect")
          .build();
    }

    final String token = sublinksJwtUtil.generateToken(person);

    userDataService.checkAndAddIpRelation(person, ip, token, userAgent);

    return LoginResponse.builder()
        .token(token)
        .status(RegistrationState.UNCHANGED)
        .build();
  }

  /**
   * Updates a person's information.
   *
   * @param person           The person object to be updated.
   * @param updatePersonForm The form containing updated person information.
   * @return The updated person response.
   */
  public PersonResponse updatePerson(Person person, UpdatePerson updatePersonForm) {

    rolePermissionService.isPermitted(person, RolePermissionPersonTypes.UPDATE_USER_SETTINGS,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    if (person.isDeleted()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "person_deleted");
    }

    if (Optional.ofNullable(updatePersonForm.languagesKeys())
        .isPresent()) {
      person.setLanguages(languageRepository.findAllByCodeIsIn(updatePersonForm.languagesKeys()));
    }

    Optional.ofNullable(updatePersonForm.displayName())
        .ifPresent(person::setDisplayName);
    Optional.ofNullable(updatePersonForm.email())
        .ifPresent(person::setEmail);
    Optional.ofNullable(updatePersonForm.avatarImageUrl())
        .ifPresent(person::setAvatarImageUrl);
    Optional.ofNullable(updatePersonForm.bannerImageUrl())
        .ifPresent(person::setBannerImageUrl);
    Optional.ofNullable(updatePersonForm.bio())
        .ifPresent(person::setBiography);
    Optional.ofNullable(updatePersonForm.matrixUserId())
        .ifPresent(person::setMatrixUserId);

    if (Optional.ofNullable(updatePersonForm.password())
        .isPresent()) {
      Optional.ofNullable(updatePersonForm.oldPassword())
          .ifPresent(oldPassword -> {
            if (!personService.isValidPersonPassword(person, oldPassword)) {
              throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password_incorrect");
            }
          });
      if (!updatePersonForm.password()
          .equals(updatePersonForm.passwordConfirmation())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "passwords_do_not_match");
      }
      personService.updatePassword(person, updatePersonForm.password());
    }

    personRepository.save(person);

    return conversionService.convert(person, PersonResponse.class);
  }

  /**
   * Retrieves a list of banned persons based on the search criteria.
   *
   * @param indexBannedPersonForm The form containing the search criteria.
   * @return A list of Person objects representing the banned persons.
   * @throws ResponseStatusException if the banned role is not found.
   */
  public List<Person> indexBannedPersons(final IndexBannedPerson indexBannedPersonForm,
      final Person person)
  {

    rolePermissionService.isPermitted(person, RolePermissionInstanceTypes.INSTANCE_BAN_READ,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));
    Optional<Role> bannedRole = roleService.getBannedRole();

    if (bannedRole.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "banned_role_not_found");
    }

    return personRepository.findAllByNameAndBiographyAndRole(indexBannedPersonForm.search(),
        bannedRole.get()
            .getId(), PageRequest.of(indexBannedPersonForm.limit(), indexBannedPersonForm.page(),
            indexBannedPersonForm.sortOrder() == SortOrder.Asc ? Sort.by("name")
                .ascending() : Sort.by("name")
                .descending()));
  }

  /**
   * Bans a person based on the provided ban form and person details.
   *
   * @param banPersonForm The form containing the ban details.
   * @param person        The person to be banned.
   * @return The PersonResponse object representing the updated person information.
   */
  public PersonResponse banPerson(final BanPerson banPersonForm, final Person person) {

    PersonIdentity ids = getPersonIdentifiersFromKey(banPersonForm.key());

    rolePermissionService.isPermitted(person, RolePermissionInstanceTypes.INSTANCE_BAN_USER,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    Person bannedPerson = personRepository.findOneByNameAndInstance_Domain(ids.name(), ids.domain())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_not_found"));

    bannedPerson.setRole(banPersonForm.ban() ? roleService.getBannedRole(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "banned_role_not_found"))
        : roleService.getDefaultRegisteredRole(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "default_registered_role_not_found")));
    personService.updatePerson(person);
    // @todo: modlog

    return conversionService.convert(person, PersonResponse.class);
  }

  /**
   * Retrieves the aggregate information of a person.
   *
   * @param key    The key containing the person's information. If the key contains "@", it is split
   *               into name and domain using "@" as the separator. Otherwise, the name is set as
   *               the key and the domain is obtained from the local instance context.
   * @param person The Person object representing the person making the request.
   * @return The PersonAggregateResponse object containing the aggregate information of the person.
   * @throws ResponseStatusException If the person is not authorized to read the person aggregation
   *                                 or if the person is not found.
   */
  public PersonAggregateResponse showAggregate(final String key, final Person person) {

    rolePermissionService.isPermitted(person, RolePermissionPersonTypes.READ_PERSON_AGGREGATION,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final PersonIdentity personIdentity = getPersonIdentifiersFromKey(key);

    final Person foundPerson = personRepository.findOneByNameAndInstance_Domain(
            personIdentity.name(), personIdentity.domain())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_not_found"));

    final PersonAggregate personAggregate = personAggregateRepository.findByPerson(foundPerson);

    return conversionService.convert(personAggregate, PersonAggregateResponse.class);
  }

  /**
   * Sets the person deleted to true.
   *
   * @param key              The key containing the person's information. If the key contains "@",
   *                         it is split into name and domain using "@" as the separator. Otherwise,
   *                         the name is set as the key and the domain is obtained from the local
   *                         instance context.
   * @param deletePersonForm The form containing the reason for deleting the person.
   * @param person           The person object to be deleted.
   * @return The PersonResponse object representing the deleted person information.
   * @throws ResponseStatusException if the person is not authorized to delete a user, if the person
   *                                 is not found, or if there is an error while deleting the
   *                                 person.
   */
  public PersonResponse deletePerson(final String key, final DeletePerson deletePersonForm,
      final Person person)
  {

    rolePermissionService.isPermitted(person, RolePermissionPersonTypes.DELETE_USER, () -> {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    });

    final PersonIdentity ids = getPersonIdentifiersFromKey(key);

    final Person personToDelete = personRepository.findOneByNameAndInstance_Domain(ids.name(),
            ids.domain())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_not_found"));

    personToDelete.setDeleted(true);
    // @todo: modlog
    personRepository.save(personToDelete);

    // Resolve all reports by the person
    postReportService.resolveAllReportsByPerson(personToDelete, person);
    commentReportService.resolveAllReportsByCommentCreator(personToDelete, person);
    privateMessageReportService.resolveAllReportsByPerson(personToDelete, person);

    personService.deleteUserAccount(personToDelete, deletePersonForm.deleteContent());

    return conversionService.convert(personToDelete, PersonResponse.class);
  }


  /**
   * Retrieves the meta data of a person's sessions based on the provided target key and person.
   *
   * @param targetKey The key containing the target person's information.
   * @param person    The Person object representing the person making the request.
   * @return The PersonSessionDataResponse object containing the meta data of the target person's
   * sessions.
   * @throws ResponseStatusException If the person is not authorized to read the person's meta data
   *                                 or if the person does not have permission to read the target
   *                                 person's meta data or if the target person is not found.
   */
  public PersonSessionDataResponse getMetaData(final String targetKey, final Person person) {

    final PersonIdentity targetIds = getPersonIdentifiersFromKey(targetKey);

    final Person target = personRepository.findOneByNameAndInstance_Domain(targetIds.name(),
            targetIds.domain())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_not_found"));

    if (!(rolePermissionService.isPermitted(person,
        RolePermissionPersonTypes.READ_USER_OWN_METADATAS) && person.equals(target))
        && !rolePermissionService.isPermitted(person,
        RolePermissionPersonTypes.READ_USER_METADATAS)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    final List<PersonMetaData> personMetaData = userDataRepository.findAllByPerson(target);

    return PersonSessionDataResponse.builder()
        .personKey(targetIds.name() + "@" + targetIds.domain())
        .sessions(personMetaData.stream()
            .map(m -> conversionService.convert(m, PersonSessionData.class))
            .toList())
        .build();
  }

  /**
   * Retrieves the meta data of a single session belonging to a person based on the provided session
   * key and person.
   *
   * @param targetSessionKey The session key containing the session's information.
   * @param person           The Person object representing the person making the request.
   * @return The PersonSessionDataResponse object containing the meta data of the session.
   * @throws ResponseStatusException If the person is not authorized to read the person's meta data
   *                                 or if the person does not have permission to read the session's
   *                                 meta data or if the session is not found.
   */
  public PersonSessionDataResponse getOneMetaData(final String targetSessionKey,
      final Person person)
  {

    final PersonMetaData personMetaData = userDataRepository.findById(
            Long.parseLong(targetSessionKey))
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_metadata_not_found"));

    if (!(rolePermissionService.isPermitted(person,
        RolePermissionPersonTypes.READ_USER_OWN_METADATAS) && personMetaData.getPerson()
        .equals(person)) && !rolePermissionService.isPermitted(person,
        RolePermissionPersonTypes.READ_USER_METADATAS)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    return PersonSessionDataResponse.builder()
        .personKey(personMetaData.getPerson()
            .getName() + "@" + personMetaData.getPerson()
            .getInstance()
            .getDomain())
        .sessions(List.of(Objects.requireNonNull(
            conversionService.convert(personMetaData, PersonSessionData.class))))
        .build();
  }

  /**
   * Invalidates the data associated with a specific user.
   *
   * @param targetUserData The key containing the user's data. If the key contains "@", it is split
   *                       into name and domain using "@" as the separator. Otherwise, the key is
   *                       assumed to be the user ID.
   * @param person         The Person object representing the user performing the operation.
   * @throws ResponseStatusException If the user is not authorized to invalidate the user data or if
   *                                 the user data is not found.
   */
  public void invalidateUserData(String targetUserData, final Person person) {

    final PersonMetaData personMetaData = userDataRepository.findById(
            Long.parseLong(targetUserData))
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_metadata_not_found"));
    if (!(rolePermissionService.isPermitted(person,
        RolePermissionPersonTypes.INVALIDATE_USER_OWN_METADATA) && personMetaData.getPerson()
        .equals(person)) && !rolePermissionService.isPermitted(person,
        RolePermissionPersonTypes.INVALIDATE_USER_METADATA)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    userDataService.invalidate(personMetaData);
  }

  /**
   * Invalidates the data associated with all user data for a specific person.
   *
   * @param targetPersonKey The key containing the target person's information. If the key contains
   *                        "@", it is split into name and domain using "@" as the separator.
   *                        Otherwise, the key is assumed to be the user ID.
   * @param person          The Person object representing the user performing the operation.
   * @throws ResponseStatusException If the user is not authorized to invalidate the user data or if
   *                                 the person is not found.
   */
  public void invalidateAllUserData(final String targetPersonKey, final Person person) {

    final PersonIdentity personIdentity = personKeyUtils.getPersonIdentity(targetPersonKey);

    final Person target = personRepository.findOneByNameAndInstance_Domain(personIdentity.name(),
            personIdentity.domain())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_not_found"));

    if (!(rolePermissionService.isPermitted(person,
        RolePermissionPersonTypes.INVALIDATE_USER_OWN_METADATA) && target.equals(person))
        && !rolePermissionService.isPermitted(person,
        RolePermissionPersonTypes.INVALIDATE_USER_METADATA)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    userDataService.invalidateAllUserData(target);
  }

  /**
   * Deletes the user data for a given targetUserDataKey and person.
   *
   * @param targetUserDataKey The key of the user data to delete.
   * @param person            The person associated with the user data.
   * @throws ResponseStatusException If the user data is not found or the person is not authorized
   *                                 to delete it.
   */
  public void deleteUserData(final String targetUserDataKey, final Person person) {

    final PersonMetaData personMetaData = userDataRepository.findById(
            Long.parseLong(targetUserDataKey))
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_metadata_not_found"));
    if (rolePermissionService.isPermitted(person,
        RolePermissionPersonTypes.DELETE_USER_OWN_METADATA) && !personMetaData.getPerson()
        .equals(person) && !rolePermissionService.isPermitted(person,
        RolePermissionPersonTypes.DELETE_USER_METADATA)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    userDataRepository.delete(personMetaData);
  }

  /**
   * Deletes all user data for a given person.
   *
   * @param targetPersonKey The unique key of the target person.
   * @param person          The person performing the deletion.
   * @throws ResponseStatusException if the target person is not found or the person is not
   *                                 authorized to delete the data.
   */
  public void deleteAllUserData(final String targetPersonKey, final Person person)
  {

    final PersonIdentity targetPersonIdentity = personKeyUtils.getPersonIdentity(targetPersonKey);

    final Person target = personRepository.findOneByNameAndInstance_Domain(
            targetPersonIdentity.name(), targetPersonIdentity.domain())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_not_found"));
    if (!(rolePermissionService.isPermitted(person,
        RolePermissionPersonTypes.DELETE_USER_OWN_METADATA) && target.equals(person))
        && !rolePermissionService.isPermitted(person,
        RolePermissionPersonTypes.DELETE_USER_METADATA)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }
    userDataRepository.deleteAll(userDataRepository.findAllByPerson(target));
  }

  /**
   * Invalidates the user data associated with a specific token.
   *
   * @param token  The token associated with the user data.
   * @param person The person performing the operation.
   * @throws ResponseStatusException If the user data is not found or the person is not authorized
   *                                 to invalidate it.
   */
  public void invalidateUserDataByToken(final String token, final Person person) {

    final PersonMetaData personMetaData = userDataRepository.findByToken(token)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_metadata_not_found"));

    if (!(rolePermissionService.isPermitted(person,
        RolePermissionPersonTypes.INVALIDATE_USER_OWN_METADATA) && personMetaData.getPerson()
        .equals(person)) && !rolePermissionService.isPermitted(person,
        RolePermissionPersonTypes.INVALIDATE_USER_METADATA)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    userDataService.invalidate(personMetaData);
  }

}
