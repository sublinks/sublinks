package com.sublinks.sublinksapi.api.sublinks.v1.search.services;

import com.sublinks.sublinksapi.api.sublinks.v1.comment.models.IndexComment;
import com.sublinks.sublinksapi.api.sublinks.v1.comment.services.SublinksCommentService;
import com.sublinks.sublinksapi.api.sublinks.v1.community.models.IndexCommunity;
import com.sublinks.sublinksapi.api.sublinks.v1.community.services.SublinksCommunityService;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.models.IndexInstance;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.service.SublinksInstanceService;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.IndexPerson;
import com.sublinks.sublinksapi.api.sublinks.v1.person.services.SublinksPersonService;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.IndexPost;
import com.sublinks.sublinksapi.api.sublinks.v1.post.services.SublinksPostService;
import com.sublinks.sublinksapi.api.sublinks.v1.search.enums.SearchScope;
import com.sublinks.sublinksapi.api.sublinks.v1.search.models.Search;
import com.sublinks.sublinksapi.api.sublinks.v1.search.models.SearchResponse;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionCommentTypes;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionCommunityTypes;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionInstanceTypes;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionPostTypes;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.utils.PaginationUtils;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@Service
public class SublinksSearchService {


  private final RolePermissionService rolePermissionService;
  private final SublinksPersonService sublinksPersonService;
  private final SublinksCommentService sublinksCommentService;
  private final SublinksCommunityService sublinksCommunityService;
  private final SublinksPostService sublinksPostService;
  private final SublinksInstanceService sublinksInstanceService;

  /**
   * Perform a search and return the search result.
   *
   * @param searchForm the search form containing search parameters
   * @param person     an optional person object representing the user performing the search
   * @return the search response containing the search results
   */
  public SearchResponse list(final Search searchForm, final Optional<Person> person) {

    rolePermissionService.isPermitted(person.orElse(null),
        RolePermissionInstanceTypes.INSTANCE_SEARCH,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final String search = searchForm.search();

    final int page = PaginationUtils.getPage(searchForm.page());
    final int perPage = PaginationUtils.getPerPage(searchForm.perPage());

    final SearchResponse.SearchResponseBuilder searchResponseBuilder = SearchResponse.builder();

    if (rolePermissionService.isPermitted(person.orElse(null), RolePermissionPostTypes.READ_POSTS)
        && searchForm.scopes()
        .contains(SearchScope.POSTS)) {

      searchResponseBuilder.persons(sublinksPersonService.index(IndexPerson.builder()
          .search(search)
          .page(page)
          .perPage(perPage)
          .listingType(searchForm.listingType())
          .sortType(searchForm.type())
          .build(), person.orElse(null)));
    }

    if (rolePermissionService.isPermitted(person.orElse(null), RolePermissionPostTypes.READ_POSTS)
        && searchForm.scopes()
        .contains(SearchScope.POSTS)) {

      searchResponseBuilder.posts(sublinksPostService.index(IndexPost.builder()
          .search(search)
          .sortType(searchForm.type())
          .listingType(searchForm.listingType())
          .showNsfw(searchForm.showNsfw())
          .savedOnly(searchForm.savedOnly())
          .perPage(perPage)
          .page(page)
          .build(), person.orElse(null)));
    }

    if (rolePermissionService.isPermitted(person.orElse(null),
        RolePermissionCommentTypes.READ_COMMENTS) && searchForm.scopes()
        .contains(SearchScope.COMMENTS)) {

      searchResponseBuilder.comments(sublinksCommentService.index(IndexComment.builder()
          .search(search)
          .listingType(searchForm.listingType())
          .showNsfw(searchForm.showNsfw())
          .page(searchForm.page())
          .perPage(searchForm.perPage())
          .build(), person.orElse(null)));
    }

    if (rolePermissionService.isPermitted(person.orElse(null),
        RolePermissionCommunityTypes.READ_COMMUNITIES) && searchForm.scopes()
        .contains(SearchScope.COMMUNITIES)) {

      searchResponseBuilder.communities(sublinksCommunityService.index(IndexCommunity.builder()
          .search(search)
          .page(page)
          .perPage(perPage)
          .listingType(searchForm.listingType())
          .showNsfw(searchForm.showNsfw())
          .sortType(searchForm.type())
          .build(), person.orElse(null)));
    }

    if (rolePermissionService.isPermitted(person.orElse(null),
        RolePermissionInstanceTypes.INSTANCE_SEARCH) && searchForm.scopes()
        .contains(SearchScope.INSTANCES)) {
      searchResponseBuilder.instances(sublinksInstanceService.index(IndexInstance.builder()
          .search(search)
          .page(page)
          .perPage(perPage)
          .build()));
    }

    return searchResponseBuilder.build();
  }
}
