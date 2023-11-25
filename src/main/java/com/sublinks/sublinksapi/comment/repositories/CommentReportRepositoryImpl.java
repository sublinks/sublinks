package com.sublinks.sublinksapi.comment.repositories;

import com.sublinks.sublinksapi.comment.dto.Comment;
import com.sublinks.sublinksapi.comment.dto.CommentReport;
import com.sublinks.sublinksapi.comment.models.CommentReportSearchCriteria;
import com.sublinks.sublinksapi.community.dto.Community;
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
import org.springframework.beans.factory.annotation.Autowired;

public class CommentReportRepositoryImpl implements CommentReportRepositorySearch {

  @Autowired
  EntityManager em;

  @Override
  public List<CommentReport> allCommentReportsBySearchCriteria(
      CommentReportSearchCriteria commentReportSearchCriteria) {

    final CriteriaBuilder cb = em.getCriteriaBuilder();
    final CriteriaQuery<CommentReport> cq = cb.createQuery(CommentReport.class);

    final Root<CommentReport> commentReportTable = cq.from(CommentReport.class);

    final List<Predicate> predicates = new ArrayList<>();

    if (commentReportSearchCriteria.unresolvedOnly()) {
      predicates.add(cb.equal(commentReportTable.get("resolved"), false));
    }

    if (commentReportSearchCriteria.community() != null) {
      // Join Comment and check community id
      final Join<CommentReport, Comment> commentJoin = commentReportTable.join("comment",
          JoinType.LEFT);
      commentReportSearchCriteria.community().forEach(community -> {
        predicates.add(cb.equal(commentJoin.get("community"), community));
      });
    }

    cq.where(predicates.toArray(new Predicate[0]));

    cq.orderBy(cb.desc(commentReportTable.get("createdAt")));

    int perPage = Math.min(Math.abs(commentReportSearchCriteria.perPage()), 20);
    int page = Math.max(commentReportSearchCriteria.page() - 1, 0);

    TypedQuery<CommentReport> query = em.createQuery(cq);
    query.setMaxResults(perPage);
    query.setFirstResult(page * perPage);

    return query.getResultList();
  }

  @Override
  public long countAllCommentReportsByResolvedFalseAndCommunity(@Nullable Community community) {

    final CriteriaBuilder cb = em.getCriteriaBuilder();
    final CriteriaQuery<Long> cq = cb.createQuery(Long.class);

    final Root<CommentReport> commentReportTable = cq.from(CommentReport.class);

    final List<Predicate> predicates = new ArrayList<>();

    predicates.add(cb.equal(commentReportTable.get("resolved"), false));

    if (community != null) {
      // Join Comment and check community id
      final Join<CommentReport, Comment> commentJoin = commentReportTable.join("comment",
          JoinType.LEFT);
      predicates.add(cb.equal(commentJoin.get("community"), community));
    }

    cq.where(predicates.toArray(new Predicate[0]));
    cq.select(cb.count(commentReportTable));
    return em.createQuery(cq).getSingleResult();
  }

  @Override
  public long countAllCommentReportsByResolvedFalse() {

    return countAllCommentReportsByResolvedFalseAndCommunity(null);
  }
}
