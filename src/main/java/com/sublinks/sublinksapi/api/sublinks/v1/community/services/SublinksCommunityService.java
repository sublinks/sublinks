package com.sublinks.sublinksapi.api.sublinks.v1.community.services;

import com.sublinks.sublinksapi.api.lemmy.v3.enums.ListingType;
import com.sublinks.sublinksapi.api.lemmy.v3.enums.SortType;
import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SublinksListingType;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.CommunityAggregateResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.CommunityResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.CreateCommunity;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.DeleteCommunity;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.IndexCommunity;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.UpdateCommunity;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.moderation.CommunityBanPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.moderation.RemoveCommunity;
import com.sublinks.sublinksapi.api.sublinks.v1.utils.ActorIdUtils;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionCommunityTypes;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.community.models.CommunitySearchCriteria;
import com.sublinks.sublinksapi.community.repositories.CommunityRepository;
import com.sublinks.sublinksapi.community.services.CommunityService;
import com.sublinks.sublinksapi.instance.models.LocalInstanceContext;
import com.sublinks.sublinksapi.person.entities.LinkPersonCommunity;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.enums.LinkPersonCommunityType;
import com.sublinks.sublinksapi.person.repositories.PersonRepository;
import com.sublinks.sublinksapi.person.services.LinkPersonCommunityService;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@Service
public class SublinksCommunityService {

  private final CommunityRepository communityRepository;
  private final ConversionService conversionService;
  private final CommunityService communityService;
  private final LocalInstanceContext localInstanceContext;
  private final RolePermissionService rolePermissionService;
  private final LinkPersonCommunityService linkPersonCommunityService;
  private final PersonRepository personRepository;

  public CommunityResponse show(String key, Person person) {

    rolePermissionService.isPermitted(person, RolePermissionCommunityTypes.READ_COMMUNITY,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final Community community = communityRepository.findCommunityByTitleSlug(key)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));

    return conversionService.convert(community, CommunityResponse.class);
  }

  /**
   * Retrieves a list of CommunityResponse objects based on the search criteria provided.
   *
   * @param indexCommunityForm The search criteria for retrieving community responses.
   * @param person             The person requesting the community responses.
   * @return The list of CommunityResponse objects that match the search criteria.
   */
  public List<CommunityResponse> index(IndexCommunity indexCommunityForm, Person person) {

    rolePermissionService.isPermitted(person, RolePermissionCommunityTypes.READ_COMMUNITY,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SortType sortType = indexCommunityForm.sortType();

    if (sortType == null) {
      if (person != null && person.getDefaultSortType() != null) {
        sortType = conversionService.convert(person.getDefaultSortType(),
            com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SortType.class);
      } else {
        sortType = com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SortType.New;
      }
    }

    SublinksListingType sublinksListingType = indexCommunityForm.listingType();

    if (sublinksListingType == null) {
      if (person != null && person.getDefaultListingType() != null) {
        sublinksListingType = conversionService.convert(person.getDefaultListingType(),
            SublinksListingType.class);
      } else if (localInstanceContext.instance()
          .getInstanceConfig()
          .getDefaultPostListingType() != null) {
        sublinksListingType = conversionService.convert(localInstanceContext.instance()
            .getInstanceConfig()
            .getDefaultPostListingType(), SublinksListingType.class);
      } else {
        sublinksListingType = SublinksListingType.Local;
      }
    }

    boolean showNsfw =
        (indexCommunityForm.showNsfw() != null && indexCommunityForm.showNsfw()) || (person != null
            && person.isShowNsfw());

    final CommunitySearchCriteria.CommunitySearchCriteriaBuilder criteria = CommunitySearchCriteria.builder()
        .perPage(indexCommunityForm.perPage())
        .page(indexCommunityForm.page())
        .sortType(conversionService.convert(sortType, SortType.class))
        .listingType(conversionService.convert(sublinksListingType, ListingType.class))
        .showNsfw(showNsfw)
        .search(indexCommunityForm.search())
        .person(person);

    final CommunitySearchCriteria communitySearchCriteria = criteria.build();

    return communityRepository.allCommunitiesBySearchCriteria(communitySearchCriteria)
        .stream()
        .map(community -> conversionService.convert(community, CommunityResponse.class))
        .toList();
  }

  /**
   * Creates a new community based on the provided community data and person.
   *
   * @param createCommunity The data for the new community.
   * @param person          The person creating the community.
   * @return The response containing the newly created community.
   * @throws ResponseStatusException If the community slug already exists or if the person is not
   *                                 authorized to create a community.
   */
  public CommunityResponse createCommunity(CreateCommunity createCommunity, Person person) {

    final Optional<Community> oldCommunity = communityRepository.findCommunityByTitleSlug(
        createCommunity.titleSlug());
    if (oldCommunity.isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "community_slug_already_exist");
    }

    if (rolePermissionService.isPermitted(person, RolePermissionCommunityTypes.CREATE_COMMUNITY)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    Community community = Community.builder()
        .title(createCommunity.title())
        .titleSlug(createCommunity.titleSlug())
        .bannerImageUrl(
            createCommunity.bannerImageUrl() != null ? createCommunity.bannerImageUrl() : null)
        .iconImageUrl(
            createCommunity.iconImageUrl() != null ? createCommunity.iconImageUrl() : null)
        .isNsfw(createCommunity.isNsfw() != null ? createCommunity.isNsfw() : false)
        .isPostingRestrictedToMods(createCommunity.isPostingRestrictedToMods())
        .description(createCommunity.description())
        .instance(localInstanceContext.instance())
        .build();
    communityService.createCommunity(community);

    return conversionService.convert(community, CommunityResponse.class);
  }

  /**
   * Updates a community based on the provided key, update form, and person.
   *
   * @param key                 The key of the community to update.
   * @param updateCommunityForm The update form containing the new community data.
   * @param person              The person performing the update.
   * @return The updated community response.
   * @throws ResponseStatusException If the community is not found, or the person is not authorized
   *                                 to perform the update.
   */
  public CommunityResponse updateCommunity(String key, final UpdateCommunity updateCommunityForm,
      final Person person)
  {

    String domain = ActorIdUtils.getActorDomain(key);
    if (domain != null && domain.equals(localInstanceContext.instance()
        .getDomain())) {
      key = ActorIdUtils.getActorId(key);
    }
    Community community = communityRepository.findCommunityByTitleSlug(key)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));

    if (!(linkPersonCommunityService.hasAnyLinkOrAdmin(person, community,
        List.of(LinkPersonCommunityType.moderator, LinkPersonCommunityType.owner))
        && rolePermissionService.isPermitted(person, RolePermissionCommunityTypes.UPDATE_COMMUNITY))
        && !rolePermissionService.isPermitted(person,
        RolePermissionCommunityTypes.ADMIN_UPDATE_COMMUNITY)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    if (updateCommunityForm.title() != null) {
      community.setTitle(updateCommunityForm.title());
    }
    if (updateCommunityForm.description() != null) {
      community.setDescription(updateCommunityForm.description());
    }
    if (updateCommunityForm.iconImageUrl() != null) {
      community.setIconImageUrl(updateCommunityForm.iconImageUrl());
    }
    if (updateCommunityForm.bannerImageUrl() != null) {
      community.setBannerImageUrl(updateCommunityForm.bannerImageUrl());
    }
    if (updateCommunityForm.isNsfw() != null) {
      community.setNsfw(updateCommunityForm.isNsfw());
    }
    if (updateCommunityForm.isPostingRestrictedToMods() != null) {
      community.setPostingRestrictedToMods(updateCommunityForm.isPostingRestrictedToMods());
    }

    communityService.updateCommunity(community);

    return conversionService.convert(community, CommunityResponse.class);
  }

  /**
   * Retrieves the moderators of a community.
   *
   * @param key The key of the community.
   * @return The list of moderators in the community.
   * @throws ResponseStatusException If the community is not found.
   */
  public List<LinkPersonCommunity> getCommunityModerators(String key, Person person) {

    rolePermissionService.isPermitted(person,
        RolePermissionCommunityTypes.READ_COMMUNITY_MODERATORS,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final Community community = communityRepository.findCommunityByTitleSlug(key)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));

    return linkPersonCommunityService.getLinkPersonCommunitiesByCommunityAndPersonAndLinkTypeIsIn(
            community, List.of(LinkPersonCommunityType.moderator, LinkPersonCommunityType.owner))
        .stream()
        .toList();
  }

  /**
   * Bans a person from a community.
   *
   * @param key                    The key of the community.
   * @param personKey              The key of the person to ban.
   * @param person                 The person performing the ban action.
   * @param communityBanPersonForm The form containing ban information.
   * @return The banned person.
   * @throws ResponseStatusException If the community or person is not found, or if the person is
   *                                 not authorized to perform the ban action.
   */
  public Person banPerson(String key, String personKey, Person person,
      CommunityBanPerson communityBanPersonForm)
  {

    final Community community = communityRepository.findCommunityByTitleSlug(key)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));

    final Person personToBan = personRepository.findOneByNameIgnoreCase(personKey)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "person_not_found"));

    if (communityBanPersonForm.ban() && linkPersonCommunityService.hasAnyLinkOrAdmin(personToBan,
        community, List.of(LinkPersonCommunityType.banned))) {
      return personToBan;
    }

    if (!(linkPersonCommunityService.hasAnyLinkOrAdmin(person, community,
        List.of(LinkPersonCommunityType.moderator, LinkPersonCommunityType.owner))
        && rolePermissionService.isPermitted(person,
        RolePermissionCommunityTypes.MODERATOR_BAN_USER)) && !rolePermissionService.isPermitted(
        person, RolePermissionCommunityTypes.ADMIN_BAN_USER)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    if (!communityBanPersonForm.ban()) {
      linkPersonCommunityService.deleteLink(community, personToBan, LinkPersonCommunityType.banned);
    } else {
      linkPersonCommunityService.removeAnyLink(personToBan, community,
          List.of(LinkPersonCommunityType.owner, LinkPersonCommunityType.moderator,
              LinkPersonCommunityType.follower, LinkPersonCommunityType.pending_follow));

      linkPersonCommunityService.createLinkPersonCommunityLink(community, personToBan,
          LinkPersonCommunityType.banned);
    }
    // @todo: Modlog

    return personToBan;
  }

  /**
   * Removes a community based on the provided key, pin comment, and person.
   *
   * @param key           The key of the community to pin.
   * @param removeComment The comment specifying the reason for removal and whether to pin all
   *                      content.
   * @param person        The person performing the removal.
   * @return The response containing the removed community.
   * @throws ResponseStatusException If the community is not found, or the person is not authorized
   *                                 to pin the community.
   */
  public CommunityResponse remove(String key, RemoveCommunity removeComment, Person person) {

    final Community community = communityRepository.findCommunityByTitleSlug(key)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));

    if (!rolePermissionService.isPermitted(person,
        RolePermissionCommunityTypes.ADMIN_REMOVE_COMMUNITY)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    community.setRemoved(removeComment.remove() != null ? removeComment.remove() : true);

    communityService.updateCommunity(community);
    // @todo: modlog

    return conversionService.convert(community, CommunityResponse.class);
  }

  /**
   * Deletes a community based on the provided key, delete form, and person.
   *
   * @param key                 The key of the community to delete.
   * @param deleteCommunityForm The delete form specifying the reason for deletion and whether to
   *                            pin the community.
   * @param person              The person performing the deletion.
   * @return The response containing the deleted community.
   * @throws ResponseStatusException If the community is not found, or the person is not authorized
   *                                 to delete the community.
   */
  public CommunityResponse delete(String key, DeleteCommunity deleteCommunityForm, Person person) {

    final Community community = communityRepository.findCommunityByTitleSlug(key)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));

    if (!(linkPersonCommunityService.hasAnyLinkOrAdmin(person, community,
        List.of(LinkPersonCommunityType.owner)) && rolePermissionService.isPermitted(person,
        RolePermissionCommunityTypes.MODERATOR_REMOVE_COMMUNITY))) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
    }

    community.setDeleted(
        deleteCommunityForm.remove() != null ? deleteCommunityForm.remove() : true);

    communityService.updateCommunity(community);
    // @todo: modlog

    return conversionService.convert(community, CommunityResponse.class);
  }

  /**
   * Retrieves the aggregated information of a community.
   *
   * @param communityKey The key of the community.
   * @param person       The person requesting the information.
   * @return The CommunityAggregateResponse containing the aggregated information of the community.
   * @throws ResponseStatusException If the person is not authorized to read the community
   *                                 aggregation or if the community is not found.
   */
  public CommunityAggregateResponse showAggregate(String communityKey, Person person) {

    rolePermissionService.isPermitted(person,
        RolePermissionCommunityTypes.READ_COMMUNITY_AGGREGATION,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final Community community = communityRepository.findCommunityByTitleSlug(communityKey)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "community_not_found"));

    return conversionService.convert(community.getCommunityAggregate(),
        CommunityAggregateResponse.class);
  }
}
