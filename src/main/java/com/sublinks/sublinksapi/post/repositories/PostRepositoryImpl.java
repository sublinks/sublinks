package com.sublinks.sublinksapi.post.repositories;

import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.instance.models.LocalInstanceContext;
import com.sublinks.sublinksapi.person.entities.LinkPersonPost;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.enums.LinkPersonPostType;
import com.sublinks.sublinksapi.person.enums.ListingType;
import com.sublinks.sublinksapi.post.entities.Post;
import com.sublinks.sublinksapi.post.models.PostSearchCriteria;
import com.sublinks.sublinksapi.post.services.PostSearchQueryService;
import com.sublinks.sublinksapi.post.services.PostSearchQueryService.Builder;
import com.sublinks.sublinksapi.post.services.PostSearchQueryService.Results;
import com.sublinks.sublinksapi.shared.RemovedState;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PostRepositoryImpl implements PostRepositorySearch {

  private final EntityManager em;
  private final PostSearchQueryService postSearchQueryService;
  private final LocalInstanceContext localInstanceContext;

  @Override
  public List<Post> allPostsBySearchCriteria(final PostSearchCriteria postSearchCriteria) {

    final Builder searchBuilder = postSearchQueryService.builder();
    if (postSearchCriteria.person() != null) {
      searchBuilder.setPerson(postSearchCriteria.person())
          .addPersonLikesToPost();
    }
    if (postSearchCriteria.communityIds() != null) {
      searchBuilder.filterByCommunities(postSearchCriteria.communityIds());
    }
    if (postSearchCriteria.person() != null) {
      searchBuilder.filterByListingType(postSearchCriteria.listingType(),
          postSearchCriteria.person());
      searchBuilder.filterBlockedPosts(postSearchCriteria.listingType());
    } else {
      searchBuilder.filterByListingType(postSearchCriteria.listingType());
    }
    if (postSearchCriteria.person() != null
        && postSearchCriteria.listingType() == ListingType.Subscribed) {
      searchBuilder.filterByListingType(postSearchCriteria.listingType());
    }
    if (postSearchCriteria.cursorBasedPageable() != null) {
      searchBuilder.setCursor(postSearchCriteria.cursorBasedPageable());
    }
    if (postSearchCriteria.sortType() != null) {
      searchBuilder.setSortType(postSearchCriteria.sortType());
    }
    searchBuilder.setSavedOnly(postSearchCriteria.isSavedOnly());

    if (localInstanceContext.instance() != null && !localInstanceContext.instance()
        .getInstanceConfig()
        .isEnableNsfw()) {
      searchBuilder.setShowNsfw(false);
    } else if (postSearchCriteria.isShowNsfw() != null) {
      searchBuilder.setShowNsfw(postSearchCriteria.isShowNsfw());
    } else if (postSearchCriteria.person() != null) {
      searchBuilder.setShowNsfw(postSearchCriteria.person()
          .isShowNsfw());
    } else {
      searchBuilder.setShowNsfw(false);
    }

    searchBuilder.addSearch(postSearchCriteria.search());

    Results results = postSearchQueryService.results(searchBuilder);
    results.setPerPage(postSearchCriteria.perPage());
    if (postSearchCriteria.cursorBasedPageable() != null) {
      return results.getCursorResults();
    }
    results.setPage(postSearchCriteria.page());
    return results.getResults();
  }

  @Override
  public List<Post> allPostsByCommunityAndPersonAndRemoved(Community community, Person person,
      List<RemovedState> removedStates)
  {

    if (community == null || person == null) {
      throw new IllegalArgumentException("Community and person cannot be null");
    }

    final CriteriaBuilder cb = em.getCriteriaBuilder();
    final CriteriaQuery<Post> cq = cb.createQuery(Post.class);

    final Root<Post> postTable = cq.from(Post.class);

    final List<Predicate> predicates = new ArrayList<>();

    if (removedStates != null) {
      final Expression<RemovedState> expression = postTable.get("removedState");
      predicates.add(expression.in(removedStates));
    }

    predicates.add(cb.equal(postTable.get("community"), community));

    final Join<Post, LinkPersonPost> linkPersonPostJoin = postTable.join("linkPersonPost",
        JoinType.INNER);
    predicates.add(cb.equal(linkPersonPostJoin.get("person"), person));
    predicates.add(cb.equal(linkPersonPostJoin.get("linkType"), LinkPersonPostType.creator));

    cq.where(predicates.toArray(new Predicate[0]));

    return em.createQuery(cq)
        .getResultList();
  }

  @Override
  public List<Post> allPostsByPersonAndRemoved(@Nullable Person person,
      @Nullable List<RemovedState> removedStates)
  {

    final CriteriaBuilder cb = em.getCriteriaBuilder();
    final CriteriaQuery<Post> cq = cb.createQuery(Post.class);

    final Root<Post> postTable = cq.from(Post.class);

    final List<Predicate> predicates = new ArrayList<>();

    // Not removed filter
    if (removedStates != null) {
      final Expression<RemovedState> expression = postTable.get("removedState");
      predicates.add(expression.in(removedStates));
    }

    if (person != null) {
      final Join<Post, LinkPersonPost> linkPersonPostJoin = postTable.join("linkPersonPost",
          JoinType.INNER);
      predicates.add(cb.equal(linkPersonPostJoin.get("person"), person));
      predicates.add(cb.equal(linkPersonPostJoin.get("linkType"), LinkPersonPostType.creator));
    }

    cq.where(predicates.toArray(new Predicate[0]));

    return em.createQuery(cq)
        .getResultList();
  }
}
