package com.sublinks.sublinksapi.post.repositories;

import static com.sublinks.sublinksapi.utils.PaginationUtils.applyPagination;

import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.person.entities.LinkPersonPost;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.enums.LinkPersonPostType;
import com.sublinks.sublinksapi.post.entities.Post;
import com.sublinks.sublinksapi.post.entities.PostReport;
import com.sublinks.sublinksapi.post.models.PostReportSearchCriteria;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PostReportRepositoryImpl implements PostReportRepositorySearch {

  private final EntityManager em;

  @Override
  public List<PostReport> allPostReportsBySearchCriteria(
      PostReportSearchCriteria postReportSearchCriteria)
  {

    final CriteriaBuilder cb = em.getCriteriaBuilder();
    final CriteriaQuery<PostReport> cq = cb.createQuery(PostReport.class);

    final Root<PostReport> postTable = cq.from(PostReport.class);

    final List<Predicate> predicates = new ArrayList<>();

    if (postReportSearchCriteria.unresolvedOnly()) {
      predicates.add(cb.equal(postTable.get("resolved"), false));
    }

    if (postReportSearchCriteria.community() != null) {
      // Join PostG and check community id
      final Join<PostReport, Post> postJoin = postTable.join("post", JoinType.LEFT);
      List<Predicate> communityPredicates = new ArrayList<>();
      postReportSearchCriteria.community()
          .forEach(community -> {

            communityPredicates.add(cb.equal(postJoin.get("community"), community));
          });
      predicates.add(cb.or(communityPredicates.toArray(new Predicate[0])));
    }

    cq.where(predicates.toArray(new Predicate[0]));

    cq.orderBy(cb.desc(postTable.get("createdAt")));

    int perPage = Math.min(Math.abs(postReportSearchCriteria.perPage()), 20);

    TypedQuery<PostReport> query = em.createQuery(cq);

    applyPagination(query, postReportSearchCriteria.page(), perPage);

    return query.getResultList();
  }

  @Override
  public long countAllPostReportsByResolvedFalseAndCommunity(@Nullable List<Community> communities)
  {

    final CriteriaBuilder cb = em.getCriteriaBuilder();
    final CriteriaQuery<Long> cq = cb.createQuery(Long.class);

    final Root<PostReport> postReportTable = cq.from(PostReport.class);

    final List<Predicate> predicates = new ArrayList<>();

    predicates.add(cb.equal(postReportTable.get("resolved"), false));

    if (communities != null) {
      // Join Comment or check community id
      final Join<PostReport, Post> postJoin = postReportTable.join("post", JoinType.LEFT);
      List<Predicate> communityPredicates = new ArrayList<>();
      communities.forEach(community -> {
        // Add a Or condition for each community

        communityPredicates.add(cb.equal(postJoin.get("community"), community));
      });
      predicates.add(cb.or(communityPredicates.toArray(new Predicate[0])));
    }

    cq.where(predicates.toArray(new Predicate[0]));

    cq.select(cb.count(postReportTable));

    return em.createQuery(cq)
        .getSingleResult();
  }

  @Override
  public long countAllPostReportsReportsByResolvedFalse() {

    return countAllPostReportsByResolvedFalseAndCommunity(null);
  }

  @Override
  public void resolveAllPostReportsByPerson(Person person, Person resolver) {

    final CriteriaBuilder cb = em.getCriteriaBuilder();
    final CriteriaQuery<PostReport> cq = cb.createQuery(PostReport.class);

    final Root<PostReport> postReportTable = cq.from(PostReport.class);
    final List<Predicate> predicates = new ArrayList<>();

    predicates.add(cb.equal(postReportTable.get("resolved"), false));

    Join<PostReport, Post> postJoin = postReportTable.join("post", JoinType.LEFT);
    Join<Post, LinkPersonPost> linkJoin = postJoin.join("linkPersonPost", JoinType.LEFT);

    predicates.add(cb.equal(linkJoin.get("linkType"), LinkPersonPostType.creator));
    predicates.add(cb.equal(linkJoin.get("person"), person));

    cq.where(predicates.toArray(new Predicate[0]));

    TypedQuery<PostReport> query = em.createQuery(cq);

    List<PostReport> postReports = query.getResultList();

    postReports.forEach(postReport -> {
      postReport.setResolved(true);
      postReport.setResolver(resolver);
      em.merge(postReport);
    });
  }

  @Override
  public void resolveAllPostReportsByPersonAndCommunity(Person person, Community community,
      Person resolver)
  {

    final CriteriaBuilder cb = em.getCriteriaBuilder();
    final CriteriaQuery<PostReport> cq = cb.createQuery(PostReport.class);

    final Root<PostReport> postReportTable = cq.from(PostReport.class);
    final List<Predicate> predicates = new ArrayList<>();

    predicates.add(cb.equal(postReportTable.get("resolved"), false));

    Join<PostReport, Post> postJoin = postReportTable.join("post", JoinType.LEFT);

    predicates.add(cb.equal(postJoin.get("community"), community));

    Join<Post, LinkPersonPost> linkJoin = postJoin.join("linkPersonPost", JoinType.LEFT);

    predicates.add(cb.equal(linkJoin.get("linkType"), LinkPersonPostType.creator));
    predicates.add(cb.equal(linkJoin.get("person"), person));

    cq.where(predicates.toArray(new Predicate[0]));

    TypedQuery<PostReport> query = em.createQuery(cq);

    List<PostReport> postReports = query.getResultList();

    postReports.forEach(postReport -> {
      postReport.setResolved(true);
      postReport.setResolver(resolver);
      em.merge(postReport);
    });
  }
}
