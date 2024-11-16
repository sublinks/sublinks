package com.sublinks.sublinksapi.api.lemmy.v3.modlog.controllers;

import com.sublinks.sublinksapi.api.lemmy.v3.admin.models.AdminPurgeCommentView;
import com.sublinks.sublinksapi.api.lemmy.v3.admin.models.AdminPurgeCommunityView;
import com.sublinks.sublinksapi.api.lemmy.v3.admin.models.AdminPurgePersonView;
import com.sublinks.sublinksapi.api.lemmy.v3.admin.models.AdminPurgePostView;
import com.sublinks.sublinksapi.api.lemmy.v3.authentication.JwtPerson;
import com.sublinks.sublinksapi.api.lemmy.v3.common.controllers.AbstractLemmyApiController;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.GetModLog;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.GetModlogResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModAddCommunityView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModAddView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModBanFromCommunityView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModBanView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModFeaturePostView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModHideCommunityView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModLockPostView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModRemoveCommentView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModRemoveCommunityView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModRemovePostView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModTransferCommunityView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.services.ModerationLogService;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionModLogTypes;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.moderation.entities.ModerationLog;
import com.sublinks.sublinksapi.person.entities.Person;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v3/modlog")
@Tag(name = "Miscellaneous")
public class ModerationLogController extends AbstractLemmyApiController {

  private final ModerationLogService moderationLogService;
  private final RolePermissionService rolePermissionService;

  @Operation(summary = "Get the modlog.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {
      @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GetModlogResponse.class))})})
  @GetMapping
  GetModlogResponse index(@Valid final GetModLog getModLogForm, final JwtPerson principal) {

    final Optional<Person> person = getOptionalPerson(principal);

    rolePermissionService.isPermitted(person.orElse(null),
        RolePermissionModLogTypes.READ_MODLOG,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    // Lemmy perPage is 20 per ModLog table and there are 15 tables
    final int limit = 300;

    final List<ModRemovePostView> removed_posts = new ArrayList<>();
    final List<ModLockPostView> locked_posts = new ArrayList<>();
    final List<ModFeaturePostView> featured_posts = new ArrayList<>();
    final List<ModRemoveCommentView> removed_comments = new ArrayList<>();
    final List<ModRemoveCommunityView> removed_communities = new ArrayList<>();
    final List<ModBanFromCommunityView> banned_from_community = new ArrayList<>();
    final List<ModBanView> banned = new ArrayList<>();
    final List<ModAddCommunityView> added_to_community = new ArrayList<>();
    final List<ModTransferCommunityView> transferred_to_community = new ArrayList<>();
    final List<ModAddView> added = new ArrayList<>();
    final List<AdminPurgePersonView> admin_purged_persons = new ArrayList<>();
    final List<AdminPurgeCommunityView> admin_purged_communities = new ArrayList<>();
    final List<AdminPurgePostView> admin_purged_posts = new ArrayList<>();
    final List<AdminPurgeCommentView> admin_purged_comments = new ArrayList<>();
    final List<ModHideCommunityView> hidden_communities = new ArrayList<>();

    final Page<ModerationLog> moderationLogs = moderationLogService.searchModerationLogs(
        getModLogForm.type_(), getModLogForm.community_id(), getModLogForm.mod_person_id(),
        getModLogForm.other_person_id(), getModLogForm.page(), limit,
        Sort.by("createdAt").descending());

    for (ModerationLog moderationLog : moderationLogs.getContent()) {
      switch (moderationLog.getActionType()) {
        case ModRemovePost -> removed_posts.add(
            moderationLogService.buildModRemovePostView(moderationLog));
        case ModLockPost -> locked_posts.add(
            moderationLogService.buildModLockPostView(moderationLog));
        case ModFeaturePost -> featured_posts.add(
            moderationLogService.buildModFeaturePostView(moderationLog));
        case ModRemoveComment -> removed_comments.add(
            moderationLogService.buildModRemoveCommentView(moderationLog));
        case ModRemoveCommunity -> removed_communities.add(
            moderationLogService.buildModRemoveCommunityView(moderationLog));
        case ModBanFromCommunity -> banned_from_community.add(
            moderationLogService.buildModBanFromCommunityView(moderationLog));
        case ModAddCommunity -> added_to_community.add(
            moderationLogService.buildModAddCommunityView(moderationLog));
        case ModTransferCommunity -> transferred_to_community.add(
            moderationLogService.buildModTransferCommunityView(moderationLog));
        case ModAdd -> added.add(moderationLogService.buildModAddView(moderationLog));
        case ModBan -> banned.add(moderationLogService.buildModBanView(moderationLog));
        case ModHideCommunity -> hidden_communities.add(
            moderationLogService.buildModHideCommunityView(moderationLog));
        case AdminPurgePerson -> admin_purged_persons.add(
            moderationLogService.buildAdminPurgePersonView(moderationLog));
        case AdminPurgeCommunity -> admin_purged_communities.add(
            moderationLogService.buildAdminPurgeCommunityView(moderationLog));
        case AdminPurgePost -> admin_purged_posts.add(
            moderationLogService.buildAdminPurgePostView(moderationLog));
        case AdminPurgeComment -> admin_purged_comments.add(
            moderationLogService.buildAdminPurgeCommentView(moderationLog));
        default -> {
          // Nothing is needed for All
        }
      }
    }

    return GetModlogResponse.builder()
        .removed_posts(removed_posts)
        .locked_posts(locked_posts)
        .featured_posts(featured_posts)
        .removed_comments(removed_comments)
        .removed_communities(removed_communities)
        .banned_from_community(banned_from_community)
        .banned(banned)
        .added_to_community(added_to_community)
        .transferred_to_community(transferred_to_community)
        .added(added)
        .admin_purged_persons(admin_purged_persons)
        .admin_purged_communities(admin_purged_communities)
        .admin_purged_posts(admin_purged_posts)
        .admin_purged_comments(admin_purged_comments)
        .hidden_communities(hidden_communities)
        .build();
  }
}
