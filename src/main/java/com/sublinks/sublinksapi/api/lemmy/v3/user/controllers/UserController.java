package com.sublinks.sublinksapi.api.lemmy.v3.user.controllers;

import com.sublinks.sublinksapi.api.lemmy.v3.authentication.JwtPerson;
import com.sublinks.sublinksapi.api.lemmy.v3.authentication.models.LoginResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.comment.models.CommentReplyView;
import com.sublinks.sublinksapi.api.lemmy.v3.comment.models.GetReplies;
import com.sublinks.sublinksapi.api.lemmy.v3.comment.services.LemmyCommentReplyService;
import com.sublinks.sublinksapi.api.lemmy.v3.common.controllers.AbstractLemmyApiController;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.BannedPersonsResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.ExportSettingsResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.GetPersonDetails;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.GetPersonDetailsResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.GetPersonMentions;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.GetPersonMentionsResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.GetRepliesResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.GetUnreadCount;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.GetUnreadCountResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.ImportSettings;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.MarkPersonMentionAsRead;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.PersonMentionResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.PersonMentionView;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.PersonView;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.SaveUserSettings;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.SuccessResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.UserExportSettings;
import com.sublinks.sublinksapi.api.lemmy.v3.user.services.LemmyPersonMentionService;
import com.sublinks.sublinksapi.api.lemmy.v3.user.services.LemmyPersonService;
import com.sublinks.sublinksapi.api.lemmy.v3.utils.PaginationControllerUtils;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionInstanceTypes;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionPersonTypes;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionPrivateMessageTypes;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.authorization.services.RoleService;
import com.sublinks.sublinksapi.comment.entities.CommentReply;
import com.sublinks.sublinksapi.comment.repositories.CommentReplyRepository;
import com.sublinks.sublinksapi.instance.models.LocalInstanceContext;
import com.sublinks.sublinksapi.language.entities.Language;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.entities.PersonMention;
import com.sublinks.sublinksapi.person.enums.LinkPersonCommunityType;
import com.sublinks.sublinksapi.person.enums.LinkPersonPersonType;
import com.sublinks.sublinksapi.person.enums.ListingType;
import com.sublinks.sublinksapi.person.enums.SortType;
import com.sublinks.sublinksapi.person.models.PersonMentionSearchCriteria;
import com.sublinks.sublinksapi.person.repositories.PersonMentionRepository;
import com.sublinks.sublinksapi.person.repositories.PersonRepository;
import com.sublinks.sublinksapi.person.services.LinkPersonCommunityService;
import com.sublinks.sublinksapi.person.services.LinkPersonPersonService;
import com.sublinks.sublinksapi.person.services.PersonService;
import com.sublinks.sublinksapi.privatemessages.models.MarkAllAsReadResponse;
import com.sublinks.sublinksapi.privatemessages.repositories.PrivateMessageRepository;
import com.sublinks.sublinksapi.privatemessages.services.PrivateMessageService;
import com.sublinks.sublinksapi.slurfilter.exceptions.SlurFilterBlockedException;
import com.sublinks.sublinksapi.slurfilter.exceptions.SlurFilterReportException;
import com.sublinks.sublinksapi.slurfilter.services.SlurFilterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v3/user")
@Tag(name = "User")
public class UserController extends AbstractLemmyApiController {

  private final LemmyPersonService lemmyPersonService;
  private final PersonRepository personRepository;
  private final PersonMentionRepository personMentionRepository;
  private final ConversionService conversionService;
  private final PersonService personService;
  private final LocalInstanceContext localInstanceContext;
  private final LemmyPersonMentionService lemmyPersonMentionService;
  private final CommentReplyRepository commentReplyRepository;
  private final LemmyCommentReplyService lemmyCommentReplyService;
  private final PrivateMessageRepository privateMessageRepository;
  private final SlurFilterService slurFilterService;
  private final RolePermissionService rolePermissionService;
  private final LinkPersonCommunityService linkPersonCommunityService;
  private final PrivateMessageService privateMessageService;
  private final RoleService roleService;
  private final LinkPersonPersonService linkPersonPersonService;

  @Operation(summary = "Get the details for a person.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = GetPersonDetailsResponse.class))})})
  @GetMapping()
  GetPersonDetailsResponse show(@Valid final GetPersonDetails getPersonDetailsForm) {

    Long userId = null;
    Person person = null;

    if (getPersonDetailsForm.person_id() != null) {
      userId = (long) getPersonDetailsForm.person_id();
      person = personRepository.findById(userId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_not_found"));
    } else if (getPersonDetailsForm.username() != null) {
      person = personRepository.findOneByNameIgnoreCase(getPersonDetailsForm.username())
          .orElseThrow(
              () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_username_given"));
    } else {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no_id_given");
    }

    rolePermissionService.isPermitted(person, RolePermissionPersonTypes.READ_USER,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    return GetPersonDetailsResponse.builder()
        .person_view(lemmyPersonService.getPersonView(person))
        .posts(lemmyPersonService.getPersonPosts(person, getPersonDetailsForm))
        .moderates(lemmyPersonService.getPersonModerates(person))
        .comments(lemmyPersonService.getPersonComments(person, getPersonDetailsForm))
        .build();
  }

  @Operation(summary = "Get mentions for your user.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = GetPersonMentionsResponse.class))})})
  @GetMapping("mention")
  GetPersonMentionsResponse mention(@Valid final GetPersonMentions getPersonMentionsForm,
      final JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionPersonTypes.READ_MENTION_USER,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final int page = PaginationControllerUtils.getAbsoluteMinNumber(getPersonMentionsForm.page(),
        1);
    final int perPage = PaginationControllerUtils.getAbsoluteMinNumber(
        getPersonMentionsForm.limit(), 20);

    final PersonMentionSearchCriteria criteria = PersonMentionSearchCriteria.builder()
        .sort(getPersonMentionsForm.sort())
        .unreadOnly(getPersonMentionsForm.unread_only()
            .orElse(false))
        .page(page)
        .perPage(perPage)
        .build();

    final List<PersonMention> personMentions = personMentionRepository.allPersonMentionBySearchCriteria(
        criteria);

    final List<PersonMentionView> personMentionViews = new ArrayList<>();

    personMentions.forEach(personMention -> personMentionViews.add(
        lemmyPersonMentionService.getPersonMentionView(personMention)));

    return GetPersonMentionsResponse.builder()
        .mentions(personMentionViews)
        .build();
  }

  @Operation(summary = "Mark a person mention as read.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PersonMentionResponse.class))})})
  @PostMapping("mention/mark_as_read")
  PersonMentionResponse mentionMarkAsRead(
      @Valid final MarkPersonMentionAsRead markPersonMentionAsReadForm, final JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionPersonTypes.MARK_MENTION_AS_READ,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final PersonMention personMention = personMentionRepository.findById(
            (long) markPersonMentionAsReadForm.person_mention_id())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_mention_not_found"));

    personMention.setRead(markPersonMentionAsReadForm.read());

    personMentionRepository.save(personMention);

    final PersonMentionView personMentionView = conversionService.convert(personMention,
        PersonMentionView.class);

    return PersonMentionResponse.builder()
        .person_mention_view(personMentionView)
        .build();
  }

  @Operation(summary = "Get comment replies.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = GetRepliesResponse.class))})})
  @GetMapping("replies")
  GetRepliesResponse replies(@Valid final GetReplies getReplies, JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionPersonTypes.READ_REPLIES,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final int page = PaginationControllerUtils.getAbsoluteMinNumber(getReplies.page(), 1);
    final int perPage = PaginationControllerUtils.getAbsoluteMinNumber(getReplies.limit(), 20);

    final List<CommentReply> commentReplies = commentReplyRepository.allCommentReplysBySearchCriteria(
        com.sublinks.sublinksapi.comment.models.CommentReplySearchCriteria.builder()
            .sortType(getReplies.sort())
            .unreadOnly(getReplies.unread_only()
                .orElse(false))
            .page(page)
            .perPage(perPage)
            .build());

    final List<CommentReplyView> commentReplyViews = new ArrayList<>();

    commentReplies.forEach(commentReply -> commentReplyViews.add(
        lemmyCommentReplyService.createCommentReplyView(commentReply, person)));

    return GetRepliesResponse.builder()
        .replies(commentReplyViews)
        .build();
  }

  @Operation(summary = "Get a list of banned users.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = BannedPersonsResponse.class))})})
  @GetMapping("banned")
  BannedPersonsResponse bannedList(final JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionInstanceTypes.INSTANCE_BAN_READ,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final Collection<PersonView> bannedPersons = roleService.getBannedUsers()
        .stream()
        .map(lemmyPersonService::getPersonView)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    return BannedPersonsResponse.builder()
        .banned(bannedPersons)
        .build();
  }

  @Operation(summary = "Mark all replies as read.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = GetRepliesResponse.class))})})
  @PostMapping("mark_all_as_read")
  GetRepliesResponse markAllAsRead(final JwtPerson principal) {
    // @todo: Check if he has permission to read/update replies ( if not ignore
    //  )

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionPersonTypes.READ_REPLIES,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final MarkAllAsReadResponse readReplies = privateMessageService.markAllAsRead(person);

    final List<CommentReplyView> commentReplyViews = new ArrayList<>();
    readReplies.commentReplies()
        .forEach(commentReply -> commentReplyViews.add(
            lemmyCommentReplyService.createCommentReplyView(commentReply, person)));

    return GetRepliesResponse.builder()
        .replies(commentReplyViews)
        .build();
  }

  @Operation(summary = "Save your user settings.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = LoginResponse.class))})})
  @Transactional
  @PutMapping("save_user_settings")
  public LoginResponse saveUserSettings(@Valid @RequestBody SaveUserSettings saveUserSettingsForm,
      JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionPersonTypes.UPDATE_USER_SETTINGS,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN,
            "unauthorized"));

    // @todo expand form validation to check for email formatting, etc.
    if (saveUserSettingsForm.show_nsfw() != null) {
      person.setShowNsfw(saveUserSettingsForm.show_nsfw());
    }
    if (saveUserSettingsForm.blur_nsfw() != null) {
      person.setBlurNsfw(saveUserSettingsForm.blur_nsfw());
    }
    if (saveUserSettingsForm.auto_expand() != null) {
      person.setAutoExpanding(saveUserSettingsForm.auto_expand());
    }
    if (saveUserSettingsForm.collapse_bot_comments() != null) {
      person.setCollapseBotComments(saveUserSettingsForm.collapse_bot_comments());
    }
    if (saveUserSettingsForm.enable_keyboard_navigation() != null) {
      person.setKeyboardNavigation(saveUserSettingsForm.enable_keyboard_navigation());
    }
    if (saveUserSettingsForm.enable_animated_images() != null) {
      person.setAnimatedImages(saveUserSettingsForm.enable_animated_images());
    }
    if (saveUserSettingsForm.show_bot_accounts() != null) {
      person.setShowBotAccounts(saveUserSettingsForm.show_bot_accounts());
    }
    if (saveUserSettingsForm.show_scores() != null) {
      person.setShowScores(saveUserSettingsForm.show_scores());
    }
    if (saveUserSettingsForm.theme() != null) {
      person.setDefaultTheme(saveUserSettingsForm.theme());
    }
    if (saveUserSettingsForm.default_sort_type() != null) {
      person.setDefaultSortType(
          conversionService.convert(saveUserSettingsForm.default_sort_type(), SortType.class));
    }
    if (saveUserSettingsForm.default_listing_type() != null) {
      person.setDefaultListingType(
          conversionService.convert(saveUserSettingsForm.default_listing_type(),
              ListingType.class));
    }
    if (saveUserSettingsForm.interface_language() != null) {
      person.setInterfaceLanguage(saveUserSettingsForm.interface_language());
    }
    if (saveUserSettingsForm.show_read_posts() != null) {
      person.setShowReadPosts(saveUserSettingsForm.show_read_posts());
    }
    if (saveUserSettingsForm.email() != null) {
      person.setEmail(saveUserSettingsForm.email());
    }
    if (saveUserSettingsForm.open_links_in_new_tab() != null) {
      person.setOpenLinksInNewTab(saveUserSettingsForm.open_links_in_new_tab());
    }
    if (saveUserSettingsForm.show_avatars() != null) {
      person.setShowAvatars(saveUserSettingsForm.show_avatars());
    }
    if (saveUserSettingsForm.send_notifications_to_email() != null) {
      person.setSendNotificationsToEmail(saveUserSettingsForm.send_notifications_to_email());
    }
    if (saveUserSettingsForm.bot_account() != null) {
      person.setBotAccount(saveUserSettingsForm.bot_account());
    }
    if (saveUserSettingsForm.matrix_user_id() != null) {
      person.setMatrixUserId(saveUserSettingsForm.matrix_user_id());
    }
    if (saveUserSettingsForm.discussion_languages() != null) {
      final List<Language> languages = localInstanceContext.languageRepository()
          .findAllById(saveUserSettingsForm.discussion_languages()
              .stream()
              .map(Long::parseLong)
              .collect(Collectors.toList()));
      person.setLanguages(languages);
    }
    if (saveUserSettingsForm.banner() != null) {
      person.setBannerImageUrl(saveUserSettingsForm.banner());
    }
    if (saveUserSettingsForm.avatar() != null) {
      person.setAvatarImageUrl(saveUserSettingsForm.avatar());
    }

    try {
      String bioFiltered = saveUserSettingsForm.bio() != null ? slurFilterService.censorText(
          saveUserSettingsForm.bio()) : "";

      String displayNameFiltered =
          saveUserSettingsForm.display_name() != null ? slurFilterService.censorText(
              saveUserSettingsForm.display_name()) : "";

      if (!Objects.equals(bioFiltered,
          saveUserSettingsForm.bio() != null ? saveUserSettingsForm.bio() : "") || !Objects.equals(
          displayNameFiltered,
          saveUserSettingsForm.display_name() != null ? slurFilterService.censorText(
              saveUserSettingsForm.display_name()) : "")) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "person_blocked_by_slur_filter");
      }
      person.setDisplayName(displayNameFiltered);
      person.setBiography(bioFiltered);
    } catch (SlurFilterReportException | SlurFilterBlockedException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "person_blocked_by_slur_filter");
    }
    // Not used anymore??
    //person.setShowNewPostNotifications(saveUserSettingsForm.show_new_post_notifs() != null
    //    && saveUserSettingsForm.show_new_post_notifs());
    // @todo generate_totp_2fa

    personService.updatePerson(person);

    return LoginResponse.builder()
        .jwt(null)
        .registration_created(false)
        .verify_email_sent(false)
        .build();
  }

  @Operation(summary = "Get your unread counts.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = GetUnreadCountResponse.class))})})
  @GetMapping("unread_count")
  GetUnreadCountResponse unreadCount(@Valid final GetUnreadCount getUnreadCountForm,
      JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person,
        Set.of(RolePermissionPersonTypes.READ_MENTION_USER, RolePermissionPersonTypes.READ_REPLIES,
            RolePermissionPrivateMessageTypes.READ_PRIVATE_MESSAGES),
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    GetUnreadCountResponse.GetUnreadCountResponseBuilder builder = GetUnreadCountResponse.builder();

    if (rolePermissionService.isPermitted(person, RolePermissionPersonTypes.READ_MENTION_USER)) {
      builder.mentions((int) personMentionRepository.countByRecipientAndIsReadIsFalse(person));
    } else {
      builder.mentions(0);
    }
    if (rolePermissionService.isPermitted(person, RolePermissionPersonTypes.READ_REPLIES)) {
      builder.replies((int) commentReplyRepository.countByRecipientAndReadIsFalse(person));
    } else {
      builder.replies(0);
    }
    if (rolePermissionService.isPermitted(person,
        RolePermissionPrivateMessageTypes.READ_PRIVATE_MESSAGES)) {
      builder.private_messages(
          (int) privateMessageRepository.countByRecipientAndReadIsFalse(person));
    } else {
      builder.private_messages(0);
    }
    return builder.build();
  }

  @Operation(summary = "Import your User Settings.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = SuccessResponse.class))})})
  @PostMapping("import_settings")
  SuccessResponse import_settings(@Valid final ImportSettings importSettingsForm,
      JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionPersonTypes.UPDATE_USER_SETTINGS,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN,
            "unauthorized"));

    final SuccessResponse.SuccessResponseBuilder builder = SuccessResponse.builder();

    UserExportSettings settings = importSettingsForm.settings();
    try {
      if (settings.show_avatars() != null) {
        person.setShowAvatars(settings.show_avatars());
      }
      if (settings.show_nsfw() != null) {
        person.setShowNsfw(settings.show_nsfw());
      }
      if (settings.blur_nsfw() != null) {
        person.setBlurNsfw(settings.blur_nsfw());
      }
      if (settings.auto_expand() != null) {
        person.setAutoExpanding(settings.auto_expand());
      }
      if (settings.collapse_bot_comments() != null) {
        person.setCollapseBotComments(settings.collapse_bot_comments());
      }
      if (settings.enable_keyboard_navigation() != null) {
        person.setKeyboardNavigation(settings.enable_keyboard_navigation());
      }
      if (settings.enable_animated_images() != null) {
        person.setAnimatedImages(settings.enable_animated_images());
      }
      if (settings.show_bot_accounts() != null) {
        person.setShowBotAccounts(settings.show_bot_accounts());
      }
      if (settings.show_scores() != null) {
        person.setShowScores(settings.show_scores());
      }
      if (settings.theme() != null) {
        person.setDefaultTheme(settings.theme());
      }
      if (settings.default_sort_type() != null) {
        person.setDefaultSortType(
            conversionService.convert(settings.default_sort_type(), SortType.class));
      }
      if (settings.default_listing_type() != null) {
        person.setDefaultListingType(
            conversionService.convert(settings.default_listing_type(), ListingType.class));
      }
      if (settings.interface_language() != null) {
        person.setInterfaceLanguage(settings.interface_language());
      }
      if (settings.show_read_posts() != null) {
        person.setShowReadPosts(settings.show_read_posts());
      }
      if (settings.email() != null) {
        person.setEmail(settings.email());
      }
      if (settings.open_links_in_new_tab() != null) {
        person.setOpenLinksInNewTab(settings.open_links_in_new_tab());
      }
      if (settings.send_notifications_to_email() != null) {
        person.setSendNotificationsToEmail(settings.send_notifications_to_email());
      }
      if (settings.bot_account() != null) {
        person.setBotAccount(settings.bot_account());
      }
      builder.success(true);

      // @todo Add Blocklist for communities, User and Instnaces
    } catch (Exception e) {
      builder.success(false);
    }

    return builder.build();
  }

  @Operation(summary = "Get your user settings.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = SaveUserSettings.class))})})
  @GetMapping("export_settings")
  ExportSettingsResponse export_settings(final JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionPersonTypes.USER_EXPORT,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    ExportSettingsResponse.ExportSettingsResponseBuilder builder = ExportSettingsResponse.builder();

    UserExportSettings.UserExportSettingsBuilder settings_builder = UserExportSettings.builder();

    settings_builder.show_avatars(person.isShowAvatars());
    settings_builder.show_nsfw(person.isShowNsfw());
    settings_builder.blur_nsfw(person.isBlurNsfw());
    settings_builder.auto_expand(person.isAutoExpanding());
    settings_builder.collapse_bot_comments(person.isCollapseBotComments());
    settings_builder.enable_keyboard_navigation(person.isKeyboardNavigation());
    settings_builder.enable_animated_images(person.isAnimatedImages());
    settings_builder.show_bot_accounts(person.isShowBotAccounts());
    settings_builder.show_scores(person.isShowScores());
    settings_builder.theme(person.getDefaultTheme());
    settings_builder.default_sort_type(conversionService.convert(person.getDefaultSortType(),
        com.sublinks.sublinksapi.api.lemmy.v3.enums.SortType.class));
    settings_builder.default_listing_type(conversionService.convert(person.getDefaultListingType(),
        com.sublinks.sublinksapi.api.lemmy.v3.enums.ListingType.class));

    settings_builder.interface_language(person.getInterfaceLanguage());
    settings_builder.show_read_posts(person.isShowReadPosts());

    // Ignore email to not "leak" it
    settings_builder.email("");

    // @todo Add Blocklist for Instnaces

    List<String> blocked_community = new ArrayList<>();
    linkPersonCommunityService.getLinks(person, LinkPersonCommunityType.blocked)
        .forEach(linkPersonCommunity -> blocked_community.add(linkPersonCommunity.getCommunity()
            .getActivityPubId()));

    builder.blocked_communities(blocked_community);

    List<String> blocked_users = new ArrayList<>();
    linkPersonPersonService.getLinks(person, LinkPersonPersonType.blocked)
        .forEach(linkPersonPerson -> blocked_users.add(linkPersonPerson.getToPerson()
            .getActivityPubId()));

    builder.blocked_users(blocked_users);
    // @todo Add Blocklist for Instances
    builder.blocked_instances(new ArrayList<>());

    builder.settings(settings_builder.build());
    builder.banner(person.getBannerImageUrl());
    builder.avatar(person.getAvatarImageUrl());
    builder.bio(person.getBiography());
    builder.bot_account(person.isBotAccount());
    builder.display_name(person.getDisplayName());
    builder.matrix_id(person.getMatrixUserId());
    builder.saved_comments(new ArrayList<>());
    builder.saved_posts(new ArrayList<>());
    builder.followed_communities(new ArrayList<>());

    return builder.build();
  }
}

