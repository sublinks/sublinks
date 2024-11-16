package com.sublinks.sublinksapi.post.repositories;

import static com.sublinks.sublinksapi.utils.PaginationUtils.applyPagination;

import com.sublinks.sublinksapi.post.entities.PostLike;
import com.sublinks.sublinksapi.post.models.PostLikeSearchCriteria;
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
public class PostLikeRepositoryImpl implements PostLikeRepositorySearch {

  private final EntityManager em;

  public List<PostLike> allPostLikesBySearchCriteria(PostLikeSearchCriteria postLikeSearchCriteria)
  {

    final CriteriaBuilder cb = em.getCriteriaBuilder();
    final CriteriaQuery<PostLike> cq = cb.createQuery(PostLike.class);

    final Root<PostLike> commentLikeTable = cq.from(PostLike.class);

    final List<Predicate> predicates = new ArrayList<>();

    predicates.add(cb.equal(commentLikeTable.get("post")
        .get("id"), postLikeSearchCriteria.postId()));

    cq.where(predicates.toArray(new Predicate[0]));

    int perPage = Math.min(Math.abs(postLikeSearchCriteria.perPage()), 20);

    TypedQuery<PostLike> query = em.createQuery(cq);

    applyPagination(query, postLikeSearchCriteria.page(), perPage);

    return query.getResultList();
  }
}
