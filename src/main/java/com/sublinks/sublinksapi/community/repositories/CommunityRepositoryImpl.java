package com.sublinks.sublinksapi.community.repositories;

import static com.sublinks.sublinksapi.utils.PaginationUtils.applyPagination;

import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.community.models.CommunitySearchCriteria;
import com.sublinks.sublinksapi.person.enums.LinkPersonCommunityType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CommunityRepositoryImpl implements CommunitySearchRepository {

  private final EntityManager em;

  @Override
  public List<Community> allCommunitiesBySearchCriteria(
      final CommunitySearchCriteria communitySearchCriteria)
  {

    final CriteriaBuilder cb = em.getCriteriaBuilder();
    final CriteriaQuery<Community> cq = cb.createQuery(Community.class);

    final Root<Community> communityTable = cq.from(Community.class);

    final List<Predicate> predicates = new ArrayList<>();

    if (!communitySearchCriteria.showNsfw()) {
      predicates.add(cb.equal(communityTable.get("isNsfw"), false));
    }

    switch (communitySearchCriteria.listingType()) {
      case Subscribed:
        if (communitySearchCriteria.person() == null) {
          break;
        }
        final var linkPersonCommunityJoin = communityTable.join("linkPersonCommunity");
        final List<Predicate> subPredicates = new ArrayList<>();
        subPredicates.add(
            cb.equal(linkPersonCommunityJoin.get("person"), communitySearchCriteria.person()));
        subPredicates.add(
            cb.equal(linkPersonCommunityJoin.get("linkType"), LinkPersonCommunityType.follower));

        predicates.add(cb.and(subPredicates.toArray(new Predicate[0])));
        break;
      case Local:
        predicates.add(cb.equal(communityTable.get("isLocal"), true));
        break;
      default:
        break;
    }

    if (communitySearchCriteria.search() != null && !communitySearchCriteria.search()
        .isEmpty()) {
      predicates.add(cb.equal(
          cb.function("fn_search_vector_is_same", Boolean.class, communityTable.get("searchVector"),
              cb.literal(communitySearchCriteria.search())), true));
    }

    cq.where(predicates.toArray(new Predicate[0]));

    switch (communitySearchCriteria.sortType()) {
      case New:
        cq.orderBy(cb.desc(communityTable.get("createdAt")));
        break;
      case Old:
        cq.orderBy(cb.asc(communityTable.get("createdAt")));
        break;
      default:
        cq.orderBy(cb.desc(communityTable.get("createdAt")));
        break;
    }

    int perPage = Math.min(Math.abs(communitySearchCriteria.perPage()), 20);

    TypedQuery<Community> query = em.createQuery(cq);

    applyPagination(query, communitySearchCriteria.page(), perPage);

    return query.getResultList();
  }
}
