package com.sublinks.sublinksapi.api.lemmy.v3.modlog.services;

import com.sublinks.sublinksapi.api.lemmy.v3.admin.models.AdminPurgeComment;
import com.sublinks.sublinksapi.api.lemmy.v3.admin.models.AdminPurgeCommentView;
import com.sublinks.sublinksapi.api.lemmy.v3.admin.models.AdminPurgeCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.admin.models.AdminPurgeCommunityView;
import com.sublinks.sublinksapi.api.lemmy.v3.admin.models.AdminPurgePerson;
import com.sublinks.sublinksapi.api.lemmy.v3.admin.models.AdminPurgePersonView;
import com.sublinks.sublinksapi.api.lemmy.v3.admin.models.AdminPurgePost;
import com.sublinks.sublinksapi.api.lemmy.v3.admin.models.AdminPurgePostView;
import com.sublinks.sublinksapi.api.lemmy.v3.comment.models.Comment;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.Community;
import com.sublinks.sublinksapi.api.lemmy.v3.enums.ModlogActionType;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModAdd;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModAddCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModAddCommunityView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModAddView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModBan;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModBanFromCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModBanFromCommunityView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModBanView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModFeaturePost;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModFeaturePostView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModHideCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModHideCommunityView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModLockPost;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModLockPostView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModRemoveComment;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModRemoveCommentView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModRemoveCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModRemoveCommunityView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModRemovePost;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModRemovePostView;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModTransferCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.modlog.models.ModTransferCommunityView;
import com.sublinks.sublinksapi.api.lemmy.v3.post.models.Post;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.Person;
import com.sublinks.sublinksapi.comment.repositories.CommentRepository;
import com.sublinks.sublinksapi.community.repositories.CommunityRepository;
import com.sublinks.sublinksapi.moderation.entities.ModerationLog;
import com.sublinks.sublinksapi.moderation.events.ModerationLogCreatedPublisher;
import com.sublinks.sublinksapi.moderation.repositories.ModerationLogRepository;
import com.sublinks.sublinksapi.person.repositories.PersonRepository;
import com.sublinks.sublinksapi.post.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Moderation Log Service
 */
@Component
@RequiredArgsConstructor
public class ModerationLogService {

  private final ModerationLogRepository moderationLogRepository;
  private final ModerationLogCreatedPublisher moderationLogCreatedPublisher;
  private final CommunityRepository communityRepository;
  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final PersonRepository personRepository;
  private final ConversionService conversionService;

  /**
   * Search moderation logs and return a page of results
   *
   * @param actionType         Moderation Log Action Type
   * @param communityId        Community Id
   * @param moderationPersonId Moderation Person Id
   * @param otherPersonId      Other Person Id
   * @param page               the page number
   * @param pageSize           the size perPage of a page
   * @param sort               the sort option
   * @return a Page of moderation logs
   */
  public Page<ModerationLog> searchModerationLogs(ModlogActionType actionType,
      final Long communityId,
      final Long moderationPersonId,
      final Long otherPersonId,
      final int page,
      final int pageSize,
      final Sort sort) {

    return moderationLogRepository.searchAllByActionTypeAndPersonIds(actionType,
        communityId,
        moderationPersonId,
        otherPersonId,
        PageRequest.of(page - 1, pageSize, sort));
  }

  /**
   * Saves a new Moderation Log
   *
   * @param moderationLog a new Moderation Log
   */
  @Transactional
  public void createModerationLog(final ModerationLog moderationLog) {

    moderationLogRepository.save(moderationLog);

    moderationLogCreatedPublisher.publish(moderationLog);
  }

  /**
   * Builds a ModRemovePostView using information from the database for the moderation log
   *
   * @param moderationLog a moderation log
   * @return a moderation log view
   */
  public ModRemovePostView buildModRemovePostView(ModerationLog moderationLog) {

    return ModRemovePostView.builder()
        .mod_remove_post(conversionService.convert(moderationLog, ModRemovePost.class))
        .community(conversionService.convert(
            communityRepository.findById(moderationLog.getCommunityId())
                .orElse(null), Community.class))
        .post(conversionService.convert(
            postRepository.findById(moderationLog.getPostId())
                .orElse(null), Post.class))
        .moderator(conversionService.convert(
            personRepository.findById(moderationLog.getModerationPersonId())
                .orElse(null), Person.class))
        .build();
  }

  /**
   * Builds a ModLockPostView using information from the database for the moderation log
   *
   * @param moderationLog a moderation log
   * @return a moderation log view
   */
  public ModLockPostView buildModLockPostView(ModerationLog moderationLog) {

    return ModLockPostView.builder()
        .mod_lock_post(conversionService.convert(moderationLog, ModLockPost.class))
        .community(conversionService.convert(
            communityRepository.findById(moderationLog.getCommunityId())
                .orElse(null), Community.class))
        .post(conversionService.convert(
            postRepository.findById(moderationLog.getPostId())
                .orElse(null), Post.class))
        .moderator(conversionService.convert(
            personRepository.findById(moderationLog.getModerationPersonId())
                .orElse(null), Person.class))
        .build();
  }

  /**
   * Builds a ModFeaturePostView using information from the database for the moderation log
   *
   * @param moderationLog a moderation log
   * @return a moderation log view
   */
  public ModFeaturePostView buildModFeaturePostView(ModerationLog moderationLog) {

    return ModFeaturePostView.builder()
        .mod_feature_post(conversionService.convert(moderationLog, ModFeaturePost.class))
        .community(conversionService.convert(
            communityRepository.findById(moderationLog.getCommunityId())
                .orElse(null), Community.class))
        .post(conversionService.convert(
            postRepository.findById(moderationLog.getPostId())
                .orElse(null), Post.class))
        .moderator(conversionService.convert(
            personRepository.findById(moderationLog.getModerationPersonId())
                .orElse(null), Person.class))
        .build();
  }

  /**
   * Builds a ModRemoveCommentView using information from the database for the moderation log
   *
   * @param moderationLog a moderation log
   * @return a moderation log view
   */
  public ModRemoveCommentView buildModRemoveCommentView(ModerationLog moderationLog) {

    return ModRemoveCommentView.builder()
        .mod_remove_comment(conversionService.convert(moderationLog, ModRemoveComment.class))
        .community(conversionService.convert(
            communityRepository.findById(moderationLog.getCommunityId())
                .orElse(null), Community.class))
        .post(conversionService.convert(
            postRepository.findById(moderationLog.getPostId())
                .orElse(null), Post.class))
        .comment(conversionService.convert(
            commentRepository.findById(moderationLog.getCommunityId())
                .orElse(null), Comment.class))
        .moderator(conversionService.convert(
            personRepository.findById(moderationLog.getModerationPersonId())
                .orElse(null), Person.class))
        .commenter(conversionService.convert(
            personRepository.findById(moderationLog.getOtherPersonId())
                .orElse(null), Person.class))
        .build();
  }

  /**
   * Builds a ModRemoveCommunityView using information from the database for the moderation log
   *
   * @param moderationLog a moderation log
   * @return a moderation log view
   */
  public ModRemoveCommunityView buildModRemoveCommunityView(ModerationLog moderationLog) {

    return ModRemoveCommunityView.builder()
        .mod_remove_community(conversionService.convert(moderationLog, ModRemoveCommunity.class))
        .community(conversionService.convert(
            communityRepository.findById(moderationLog.getCommunityId())
                .orElse(null), Community.class))
        .moderator(conversionService.convert(
            personRepository.findById(moderationLog.getModerationPersonId())
                .orElse(null), Person.class))
        .build();
  }

  /**
   * Builds a ModBanFromCommunityView using information from the database for the moderation log
   *
   * @param moderationLog a moderation log
   * @return a moderation log view
   */
  public ModBanFromCommunityView buildModBanFromCommunityView(ModerationLog moderationLog) {

    return ModBanFromCommunityView.builder()
        .mod_ban_from_community(conversionService.convert(moderationLog, ModBanFromCommunity.class))
        .community(conversionService.convert(
            communityRepository.findById(moderationLog.getCommunityId())
                .orElse(null), Community.class))
        .banned_person(conversionService.convert(
            personRepository.findById(moderationLog.getOtherPersonId())
                .orElse(null), Person.class))
        .moderator(conversionService.convert(
            personRepository.findById(moderationLog.getModerationPersonId())
                .orElse(null), Person.class))
        .build();
  }

  /**
   * Builds a ModBanView using information from the database for the moderation log
   *
   * @param moderationLog a moderation log
   * @return a moderation log view
   */
  public ModBanView buildModBanView(ModerationLog moderationLog) {

    return ModBanView.builder()
        .mod_ban(conversionService.convert(moderationLog, ModBan.class))
        .banned_person(conversionService.convert(
            personRepository.findById(moderationLog.getOtherPersonId())
                .orElse(null), Person.class))
        .moderator(conversionService.convert(
            personRepository.findById(moderationLog.getModerationPersonId())
                .orElse(null), Person.class))
        .build();
  }

  /**
   * Builds a ModAddCommunityView using information from the database for the moderation log
   *
   * @param moderationLog a moderation log
   * @return a moderation log view
   */
  public ModAddCommunityView buildModAddCommunityView(ModerationLog moderationLog) {

    return ModAddCommunityView.builder()
        .mod_add_community(conversionService.convert(moderationLog, ModAddCommunity.class))
        .community(conversionService.convert(
            communityRepository.findById(moderationLog.getCommunityId())
                .orElse(null), Community.class))
        .modded_person(conversionService.convert(
            personRepository.findById(moderationLog.getOtherPersonId())
                .orElse(null), Person.class))
        .moderator(conversionService.convert(
            personRepository.findById(moderationLog.getModerationPersonId())
                .orElse(null), Person.class))
        .build();
  }

  /**
   * Builds a ModTransferCommunityView using information from the database for the moderation log
   *
   * @param moderationLog a moderation log
   * @return a moderation log view
   */
  public ModTransferCommunityView buildModTransferCommunityView(ModerationLog moderationLog) {

    return ModTransferCommunityView.builder()
        .mod_transfer_community(
            conversionService.convert(moderationLog, ModTransferCommunity.class))
        .community(conversionService.convert(
            communityRepository.findById(moderationLog.getCommunityId())
                .orElse(null), Community.class))
        .modded_person(conversionService.convert(
            personRepository.findById(moderationLog.getOtherPersonId())
                .orElse(null), Person.class))
        .moderator(conversionService.convert(
            personRepository.findById(moderationLog.getModerationPersonId())
                .orElse(null), Person.class))
        .build();
  }

  /**
   * Builds a ModAddView using information from the database for the moderation log
   *
   * @param moderationLog a moderation log
   * @return a moderation log view
   */
  public ModAddView buildModAddView(ModerationLog moderationLog) {

    return ModAddView.builder()
        .mod_add(conversionService.convert(moderationLog, ModAdd.class))
        .modded_person(conversionService.convert(
            personRepository.findById(moderationLog.getOtherPersonId())
                .orElse(null), Person.class))
        .moderator(conversionService.convert(
            personRepository.findById(moderationLog.getModerationPersonId())
                .orElse(null), Person.class))
        .build();
  }

  /**
   * Builds a AdminPurgePersonView using information from the database for the moderation log
   *
   * @param moderationLog a moderation log
   * @return a moderation log view
   */
  public AdminPurgePersonView buildAdminPurgePersonView(ModerationLog moderationLog) {

    return AdminPurgePersonView.builder()
        .admin_purge_person(conversionService.convert(moderationLog, AdminPurgePerson.class))
        .admin(conversionService.convert(
            personRepository.findById(moderationLog.getAdminPersonId())
                .orElse(null), Person.class))
        .build();
  }

  /**
   * Builds a AdminPurgeCommunityView using information from the database for the moderation log
   *
   * @param moderationLog a moderation log
   * @return a moderation log view
   */
  public AdminPurgeCommunityView buildAdminPurgeCommunityView(ModerationLog moderationLog) {

    return AdminPurgeCommunityView.builder()
        .admin_purge_community(conversionService.convert(moderationLog, AdminPurgeCommunity.class))
        .admin(conversionService.convert(
            personRepository.findById(moderationLog.getAdminPersonId())
                .orElse(null), Person.class))
        .build();
  }

  /**
   * Builds a AdminPurgePostView using information from the database for the moderation log
   *
   * @param moderationLog a moderation log
   * @return a moderation log view
   */
  public AdminPurgePostView buildAdminPurgePostView(ModerationLog moderationLog) {

    return AdminPurgePostView.builder()
        .admin_purge_post(conversionService.convert(moderationLog, AdminPurgePost.class))
        .community(conversionService.convert(
            communityRepository.findById(moderationLog.getCommunityId())
                .orElse(null), Community.class))
        .admin(conversionService.convert(
            personRepository.findById(moderationLog.getAdminPersonId())
                .orElse(null), Person.class))
        .build();
  }

  /**
   * Builds a AdminPurgeCommentView using information from the database for the moderation log
   *
   * @param moderationLog a moderation log
   * @return a moderation log view
   */
  public AdminPurgeCommentView buildAdminPurgeCommentView(ModerationLog moderationLog) {

    return AdminPurgeCommentView.builder()
        .admin_purge_comment(conversionService.convert(moderationLog, AdminPurgeComment.class))
        .post(conversionService.convert(
            postRepository.findById(moderationLog.getPostId())
                .orElse(null), Post.class))
        .admin(conversionService.convert(
            personRepository.findById(moderationLog.getAdminPersonId())
                .orElse(null), Person.class))
        .build();
  }

  /**
   * Builds a ModHideCommunityView using information from the database for the moderation log
   *
   * @param moderationLog a moderation log
   * @return a moderation log view
   */
  public ModHideCommunityView buildModHideCommunityView(ModerationLog moderationLog) {

    return ModHideCommunityView.builder()
        .mod_hide_community(conversionService.convert(moderationLog, ModHideCommunity.class))
        .community(conversionService.convert(
            communityRepository.findById(moderationLog.getCommunityId())
                .orElse(null), Community.class))
        .admin(conversionService.convert(
            personRepository.findById(moderationLog.getAdminPersonId())
                .orElse(null), Person.class))
        .build();
  }
}