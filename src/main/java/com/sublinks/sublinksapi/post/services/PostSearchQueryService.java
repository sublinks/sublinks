package com.sublinks.sublinksapi.post.services;

import com.sublinks.sublinksapi.authorization.entities.Role;
import com.sublinks.sublinksapi.authorization.enums.RoleTypes;
import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.person.entities.LinkPersonCommunity;
import com.sublinks.sublinksapi.person.entities.LinkPersonPerson;
import com.sublinks.sublinksapi.person.entities.LinkPersonPost;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.enums.LinkPersonCommunityType;
import com.sublinks.sublinksapi.person.enums.LinkPersonPersonType;
import com.sublinks.sublinksapi.person.enums.LinkPersonPostType;
import com.sublinks.sublinksapi.person.enums.ListingType;
import com.sublinks.sublinksapi.person.enums.SortType;
import com.sublinks.sublinksapi.post.entities.Post;
import com.sublinks.sublinksapi.post.entities.PostLike;
import com.sublinks.sublinksapi.post.sorts.SortFactory;
import com.sublinks.sublinksapi.post.sorts.SortingTypeInterface;
import com.sublinks.sublinksapi.shared.RemovedState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class PostSearchQueryService {

  private final EntityManager entityManager;
  private final SortFactory sortFactory;

  public Builder builder() {

    return new Builder(entityManager, entityManager.getCriteriaBuilder(), sortFactory);
  }

  public Results results(Builder builder) {

    return new Results(builder);
  }

  @Getter
  public static class Results {

    private final Builder builder;
    private int perPage = 20;

    public Results(Builder builder) {

      this.builder = builder;
    }

    public Results setPerPage(final Integer perPage) {

      this.perPage = Math.min(Math.abs(perPage), 21);
      return this;
    }

    public Results setPage(final Integer page) {

      int p = Math.max(Math.abs(page), 1);
      getQuery().setFirstResult((p - 1) * this.getPerPage());
      return this;
    }

    private TypedQuery<Post> getQuery() {

      if (builder.getSorter() != null) {
        builder.getSorter()
            .applySorting(builder);
        if (builder.getCursor() != null && !builder.getCursor()
            .isBlank()) {
          builder.getSorter()
              .applyCursor(builder);
        }
      }
      List<Predicate> predicates = builder.getPredicates();

      if (!builder.getRemovedState()
          .isEmpty()) {
        predicates.add(builder.getPostTable()
            .get("removedState")
            .in(builder.getRemovedState()));
      }

      builder.getCriteriaQuery()
          .where(predicates.toArray(new Predicate[0]));

      final TypedQuery<Post> query = builder.getEntityManager()
          .createQuery(builder.getCriteriaQuery());

      return query;
    }

    public List<Post> getResults() {

      final TypedQuery<Post> query = getQuery();

      query.setMaxResults(Math.abs(this.perPage));

      return query.getResultList();
    }

    public List<Post> getCursorResults() {

      final TypedQuery<Post> query = getQuery();

      query.setMaxResults(Math.abs(this.perPage));

      return query.getResultList();
    }
  }

  @Getter
  public static class Builder {

    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private final SortFactory sortFactory;
    private final Root<Post> postTable;
    final List<Predicate> predicates = new ArrayList<>();
    private final CriteriaQuery<Post> criteriaQuery;
    private Person person;
    private String cursor;
    private SortingTypeInterface sorter;
    private Set<RemovedState> removedState;

    public Builder(EntityManager entityManager, CriteriaBuilder criteriaBuilder,
        SortFactory sortFactory)
    {

      this.entityManager = entityManager;
      this.criteriaBuilder = criteriaBuilder;
      this.sortFactory = sortFactory;
      this.criteriaQuery = criteriaBuilder.createQuery(Post.class);
      this.postTable = criteriaQuery.from(Post.class);
      this.removedState = Set.of(RemovedState.NOT_REMOVED);
    }

    public Builder filterByCommunities(List<Long> communityIds) {

      final Join<Post, Community> communityJoin = postTable.join("community", JoinType.INNER);
      if (communityIds != null && communityIds.isEmpty()) {
        final Expression<Long> expression = communityJoin.get("id");
        predicates.add(expression.in(communityIds));
      }
      return this;
    }

    public Builder filterBlockedPosts(ListingType listingType) {

      if (this.person != null && (listingType == null || !listingType.equals(
          ListingType.ModeratorView))) {

        final Join<Post, LinkPersonPost> linkPersonPostJoin = this.postTable.join("linkPersonPost",
            JoinType.LEFT);

        linkPersonPostJoin.on(
            criteriaBuilder.equal(linkPersonPostJoin.get("linkType"), LinkPersonPostType.creator));

        final Join<LinkPersonPost, Person> personJoin = linkPersonPostJoin.join("person",
            JoinType.LEFT);

        final Join<Person, LinkPersonPerson> linkPersonPersonJoin = personJoin.join(
            "linkPersonPersonTo", JoinType.LEFT);
        linkPersonPersonJoin.on(
            this.criteriaBuilder.equal(linkPersonPersonJoin.get("fromPerson"), this.person));

        final Join<LinkPersonPerson, Person> linkToPersonPersonPersonJoin = linkPersonPersonJoin.join(
            "fromPerson", JoinType.LEFT);

        final Join<Person, Role> roleJoin = personJoin.join("role", JoinType.LEFT);

        predicates.add(this.criteriaBuilder.or(
            this.criteriaBuilder.or(linkPersonPersonJoin.isNull(),
                this.criteriaBuilder.notEqual(linkPersonPersonJoin.get("linkType"),
                    LinkPersonPersonType.blocked)),
            this.criteriaBuilder.equal(roleJoin.get("name"), RoleTypes.ADMIN.toString())));
      }

      return this;
    }

    public Builder filterByListingType(ListingType listingType, Person person) {

      this.setPerson(person);
      return this.filterByListingType(listingType);
    }

    public Builder filterByListingType(ListingType listingType) {

      if (this.person == null) {
        return this;
      }

      if (listingType != ListingType.ModeratorView) {

        final Join<Post, LinkPersonPost> linkPersonPostJoin = postTable.join("linkPersonPost",
            JoinType.INNER);
        predicates.add(criteriaBuilder.or(criteriaBuilder.and(
                criteriaBuilder.equal(linkPersonPostJoin.get("person"), this.person),
                criteriaBuilder.equal(linkPersonPostJoin.get("linkType"), LinkPersonPostType.creator),
                criteriaBuilder.equal(postTable.get("isDeleted"), true)),
            criteriaBuilder.equal(postTable.get("isDeleted"), false)));
      }

      if (listingType == ListingType.Subscribed) {
        final Join<Post, Community> communityJoin = postTable.join("community", JoinType.INNER);
        final Join<Community, LinkPersonCommunity> linkPersonCommunityJoin = communityJoin.join(
            "linkPersonCommunity", JoinType.INNER);
        predicates.add(criteriaBuilder.equal(linkPersonCommunityJoin.get("person"), this.person));
      } else if (listingType == ListingType.ModeratorView) {
        final Join<Post, Community> communityJoin = postTable.join("community", JoinType.INNER);
        final Join<Community, LinkPersonCommunity> linkPersonCommunityJoin = communityJoin.join(
            "linkPersonCommunity", JoinType.INNER);
        predicates.add(criteriaBuilder.equal(linkPersonCommunityJoin.get("person"), this.person));
        predicates.add(criteriaBuilder.or(
            criteriaBuilder.equal(linkPersonCommunityJoin.get("linkType"),
                LinkPersonCommunityType.moderator),
            criteriaBuilder.equal(linkPersonCommunityJoin.get("linkType"),
                LinkPersonCommunityType.owner)));
        removedState = Set.of();
      }
      return this;
    }

    public Builder addRemovedState(RemovedState removedState) {

      this.removedState.add(removedState);
      return this;
    }

    public Builder removeRemovedState(RemovedState removedState) {

      this.removedState.remove(removedState);
      return this;
    }

    public Builder clearRemovedState() {

      this.removedState.clear();
      return this;
    }

    public Builder addPersonLikesToPost(Person person) {

      this.setPerson(person);
      return addPersonLikesToPost();
    }

    public Builder addPersonLikesToPost() {

      if (this.person == null) {
        return this;
      }

      final Join<Post, PostLike> postPostLikeJoin = postTable.join("postLikes", JoinType.LEFT);
      postPostLikeJoin.on(criteriaBuilder.equal(postPostLikeJoin.get("person"), this.person));

      return this;
    }

    public Builder setSorter(final SortingTypeInterface sorter) {

      this.sorter = sorter;
      return this;
    }

    public Builder setSortType(final SortType sortType) {

      return setSorter(sortFactory.createSort(sortType));
    }

    public Builder setSavedOnly(final Boolean savedOnly) {

      if (savedOnly != null && savedOnly && this.person != null) {

        final Join<Post, LinkPersonPost> linkPersonPostJoin = this.postTable.join("linkPersonPost",
            JoinType.LEFT);

        linkPersonPostJoin.on(
            criteriaBuilder.equal(linkPersonPostJoin.get("linkType"), LinkPersonPostType.saved));
        predicates.add(this.criteriaBuilder.equal(linkPersonPostJoin.get("person"), this.person));
      }
      return this;
    }

    public Builder setCursor(final String cursor) {

      byte[] decodedBytes = Base64.getDecoder()
          .decode(cursor);
      this.cursor = new String(decodedBytes);
      return this;
    }

    public Builder setPerson(Person person) {

      this.person = person;
      return this;
    }

    public Builder setShowNsfw(Boolean isShowNsfw) {

      if (!isShowNsfw) {
        predicates.add(criteriaBuilder.isFalse(postTable.get("isNsfw")));
      }
      return this;
    }

    public Builder addSearch(String search) {

      if (search != null && !search.isBlank()) {

        predicates.add(criteriaBuilder.equal(
            criteriaBuilder.function("fn_search_vector_is_same", Boolean.class,
                postTable.get("searchVector"), criteriaBuilder.literal(search)), true));
      }
      return this;
    }
  }
}
