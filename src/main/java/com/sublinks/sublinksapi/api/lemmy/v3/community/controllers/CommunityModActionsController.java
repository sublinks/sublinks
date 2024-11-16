package com.sublinks.sublinksapi.api.lemmy.v3.community.controllers;

import com.sublinks.sublinksapi.api.lemmy.v3.authentication.JwtPerson;
import com.sublinks.sublinksapi.api.lemmy.v3.common.controllers.AbstractLemmyApiController;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.AddModToCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.AddModToCommunityResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.BanFromCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.BanFromCommunityResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.CommunityModeratorView;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.CommunityResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.DeleteCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.GetCommunityResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.HideCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.RemoveCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.TransferCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.community.services.LemmyCommunityService;
import com.sublinks.sublinksapi.api.lemmy.v3.enums.ModlogActionType;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.services.ModerationLogService;
import com.sublinks.sublinksapi.api.lemmy.v3.user.services.LemmyPersonService;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionCommunityTypes;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.comment.services.CommentReportService;
import com.sublinks.sublinksapi.comment.services.CommentService;
import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.community.repositories.CommunityRepository;
import com.sublinks.sublinksapi.community.services.CommunityService;
import com.sublinks.sublinksapi.moderation.entities.ModerationLog;
import com.sublinks.sublinksapi.person.entities.LinkPersonCommunity;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.enums.LinkPersonCommunityType;
import com.sublinks.sublinksapi.person.repositories.PersonRepository;
import com.sublinks.sublinksapi.person.services.LinkPersonCommunityService;
import com.sublinks.sublinksapi.person.services.PersonService;
import com.sublinks.sublinksapi.post.services.PostReportService;
import com.sublinks.sublinksapi.post.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping(path = "/api/v3/community")
@Tag(name = "Community")
@RequiredArgsConstructor
public class CommunityModActionsController extends AbstractLemmyApiController {

  private final CommunityService communityService;
  private final CommunityRepository communityRepository;
  private final RolePermissionService rolePermissionService;
  private final LemmyCommunityService lemmyCommunityService;
  private final LinkPersonCommunityService linkPersonCommunityService;
  private final PersonRepository personRepository;
  private final LemmyPersonService lemmyPersonService;
  private final ConversionService conversionService;
  private final CommentService commentService;
  private final PostService postService;
  private final ModerationLogService moderationLogService;
  private final PostReportService postReportService;
  private final CommentReportService commentReportService;
  private final PersonService personService;

  @Operation(summary = "Hide a community from public / \"All\" view. Admins only.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = CommunityResponse.class))})})
  @PutMapping("hide")
  CommunityResponse hide(@Valid @RequestBody final HideCommunity hideCommunityForm,
      JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionCommunityTypes.ADMIN_REMOVE_COMMUNITY,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));
    final Community community = communityRepository.findById(hideCommunityForm.community_id())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));

    community.setLocal(hideCommunityForm.hidden());
    communityRepository.save(community);

    // Create Moderation Log
    ModerationLog moderationLog = ModerationLog.builder()
        .actionType(ModlogActionType.ModHideCommunity)
        .hidden(hideCommunityForm.hidden())
        .entityId(community.getId())
        .communityId(community.getId())
        .instance(community.getInstance())
        .adminPersonId(person.getId())
        .reason(hideCommunityForm.reason())
        .build();
    moderationLogService.createModerationLog(moderationLog);

    return CommunityResponse.builder()

        .community_view(lemmyCommunityService.communityViewFromCommunity(community))
        .build();
  }

  @Operation(summary = "Delete a community.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = CommunityResponse.class))})})
  @PostMapping("delete")
  CommunityResponse delete(@Valid final DeleteCommunity deleteCommunityForm, JwtPerson principal) {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionCommunityTypes.DELETE_COMMUNITY,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    RolePermissionService.isAdminElseThrow(person,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN));

    final Community community = communityRepository.findById(
            (long) deleteCommunityForm.community_id())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));

    community.setDeleted(deleteCommunityForm.deleted());
    communityRepository.save(community);

    // Create Moderation Log
    ModerationLog moderationLog = ModerationLog.builder()
        .actionType(ModlogActionType.ModRemoveCommunity)
        .removed(deleteCommunityForm.deleted())
        .entityId(community.getId())
        .communityId(community.getId())
        .instance(community.getInstance())
        .adminPersonId(person.getId())
        .build();
    moderationLogService.createModerationLog(moderationLog);

    return CommunityResponse.builder()

        .community_view(lemmyCommunityService.communityViewFromCommunity(community))
        .build();
  }

  @Operation(summary = "A moderator pin for a community.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = CommunityResponse.class))})})
  @PostMapping("remove")
  CommunityResponse remove(@Valid @RequestBody final RemoveCommunity removeCommunityForm,
      JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person,
        RolePermissionCommunityTypes.MODERATOR_REMOVE_COMMUNITY,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    final Community community = communityRepository.findById(
            (long) removeCommunityForm.community_id())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));

    if (!linkPersonCommunityService.hasAnyLink(person, community,
        List.of(LinkPersonCommunityType.moderator, LinkPersonCommunityType.owner))) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    community.setRemoved(removeCommunityForm.removed());
    communityRepository.save(community);

    // Create Moderation Log
    ModerationLog moderationLog = ModerationLog.builder()
        .actionType(ModlogActionType.ModRemoveCommunity)
        .removed(removeCommunityForm.removed())
        .entityId(community.getId())
        .communityId(community.getId())
        .instance(community.getInstance())
        .moderationPersonId(person.getId())
        .reason(removeCommunityForm.reason())
        .build();
    moderationLogService.createModerationLog(moderationLog);

    return CommunityResponse.builder()

        .community_view(lemmyCommunityService.communityViewFromCommunity(community))
        .build();
  }

  @Operation(summary = "Transfer your community to an existing moderator.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = GetCommunityResponse.class))})})
  @PostMapping("transfer")
  GetCommunityResponse transfer(@Valid @RequestBody final TransferCommunity transferCommunityForm,
      JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person,
        RolePermissionCommunityTypes.MODERATOR_TRANSFER_COMMUNITY,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    final Community community = communityRepository.findById(
            (long) transferCommunityForm.community_id())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));
    if (!linkPersonCommunityService.hasLinkOrAdmin(person, community,
        LinkPersonCommunityType.owner)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    final Person newOwner = personRepository.findById((long) transferCommunityForm.person_id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_not_found"));

    if (!linkPersonCommunityService.hasLink(community, newOwner,
        LinkPersonCommunityType.moderator)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "person_not_moderator");
    }

    final Person oldOwner = linkPersonCommunityService.getLinksByEntity(community,
            List.of(LinkPersonCommunityType.owner))
        .stream()
        .findFirst()

        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "owner_not_found"))
        .getPerson();
    linkPersonCommunityService.createLinkPersonCommunityLink(community, oldOwner,
        LinkPersonCommunityType.moderator);
    linkPersonCommunityService.getLink(community, oldOwner, LinkPersonCommunityType.owner)
        .ifPresent(linkPersonCommunityService::deleteLink);

    linkPersonCommunityService.createLinkPersonCommunityLink(community, newOwner,
        LinkPersonCommunityType.owner);
    linkPersonCommunityService.getLink(community, newOwner, LinkPersonCommunityType.moderator)
        .ifPresent(linkPersonCommunityService::deleteLink);

    // Create Moderation Log
    ModerationLog moderationLog = ModerationLog.builder()
        .actionType(ModlogActionType.ModTransferCommunity)
        .entityId(community.getId())
        .communityId(community.getId())
        .instance(community.getInstance())
        .moderationPersonId(person.getId())
        .otherPersonId(newOwner.getId())
        .build();
    moderationLogService.createModerationLog(moderationLog);

    return GetCommunityResponse.builder()

        .community_view(lemmyCommunityService.communityViewFromCommunity(community))
        .build();
  }

  @Operation(summary = "Ban a user from a community.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = BanFromCommunityResponse.class))})})
  @PostMapping("ban_user")
  BanFromCommunityResponse banUser(@Valid @RequestBody final BanFromCommunity banPersonForm,
      JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionCommunityTypes.MODERATOR_BAN_USER,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    final Community community = communityRepository.findById((long) banPersonForm.community_id())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));

    if (!linkPersonCommunityService.hasAnyLinkOrAdmin(person, community,
        List.of(LinkPersonCommunityType.moderator, LinkPersonCommunityType.owner))) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    final Person personToBan = personRepository.findById((long) banPersonForm.person_id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_not_found"));

    if (banPersonForm.ban()) {
      if (banPersonForm.remove_data()) {

        postReportService.resolveAllReportsByPersonAndCommunity(personToBan, community, person);
        commentReportService.resolveAllReportsByCommentCreatorAndCommunity(personToBan, community,
            person);

        commentService.removeAllCommentsFromCommunityAndUser(community, personToBan, true);
        postService.removeAllPostsFromCommunityAndUser(community, personToBan, true);
      }
      if (!linkPersonCommunityService.hasLink(community, personToBan,
          LinkPersonCommunityType.banned)) {
        linkPersonCommunityService.createLinkPersonCommunityLink(community, personToBan,
            LinkPersonCommunityType.banned,
            banPersonForm.expires() != null ? new Date(banPersonForm.expires() * 1000L) : null);
      }
    } else {
      commentService.removeAllCommentsFromCommunityAndUser(community, personToBan, false);
      postService.removeAllPostsFromCommunityAndUser(community, personToBan, false);

      linkPersonCommunityService.deleteLink(community, personToBan, LinkPersonCommunityType.banned);
    }

    // Create Moderation Log
    ModerationLog moderationLog = ModerationLog.builder()
        .actionType(ModlogActionType.ModBanFromCommunity)
        .banned(banPersonForm.ban())
        .entityId(community.getId())
        .communityId(community.getId())
        .instance(community.getInstance())
        .moderationPersonId(person.getId())
        .otherPersonId(personToBan.getId())
        .reason(banPersonForm.reason())
        .expires(banPersonForm.expires() == null ? null : new Date(banPersonForm.expires() * 1000L))
        .build();
    moderationLogService.createModerationLog(moderationLog);

    return BanFromCommunityResponse.builder()
        .banned(banPersonForm.ban())

        .person_view(lemmyPersonService.getPersonView(personToBan))
        .build();
  }

  @Operation(summary = "Add a moderator to your community.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AddModToCommunityResponse.class))})})
  @PostMapping("mod")
  AddModToCommunityResponse addMod(@Valid @RequestBody AddModToCommunity addModToCommunityForm,
      JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person,
        Set.of(RolePermissionCommunityTypes.MODERATOR_ADD_MODERATOR,
            RolePermissionCommunityTypes.ADMIN_ADD_COMMUNITY_MODERATOR),
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    final Community community = communityRepository.findById(
            (long) addModToCommunityForm.community_id())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));

    final boolean isAllowed = linkPersonCommunityService.hasLink(community, person,
        LinkPersonCommunityType.moderator) || linkPersonCommunityService.hasLink(community, person,
        LinkPersonCommunityType.owner);

    if (!isAllowed) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    final Person personToAdd = personRepository.findById((long) addModToCommunityForm.person_id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_not_found"));

    if (addModToCommunityForm.added()) {
      if (!linkPersonCommunityService.hasLink(community, personToAdd,
          LinkPersonCommunityType.moderator)) {
        linkPersonCommunityService.createLinkPersonCommunityLink(community, personToAdd,
            LinkPersonCommunityType.moderator);
      }
    } else {
      linkPersonCommunityService.deleteLink(community, personToAdd,
          LinkPersonCommunityType.moderator);
    }

    Collection<Person> moderators = linkPersonCommunityService.getLinksByEntity(community,
            List.of(LinkPersonCommunityType.moderator))
        .stream()
        .map(LinkPersonCommunity::getPerson)
        .toList();

    List<CommunityModeratorView> moderatorsView = moderators.stream()

        .map(moderator -> CommunityModeratorView.builder()
            .moderator(conversionService.convert(moderator,
                com.sublinks.sublinksapi.api.lemmy.v3.user.models.Person.class))
            .community(conversionService.convert(community,
                com.sublinks.sublinksapi.api.lemmy.v3.community.models.Community.class))
            .build())
        .toList();

    // Create Moderation Log
    ModerationLog moderationLog = ModerationLog.builder()
        .actionType(ModlogActionType.ModAddCommunity)
        .entityId(community.getId())
        .communityId(community.getId())
        .removed(!addModToCommunityForm.added())
        .instance(community.getInstance())
        .moderationPersonId(person.getId())
        .otherPersonId(personToAdd.getId())
        .build();
    moderationLogService.createModerationLog(moderationLog);

    return AddModToCommunityResponse.builder()
        .moderators(moderatorsView)
        .build();
  }
}
