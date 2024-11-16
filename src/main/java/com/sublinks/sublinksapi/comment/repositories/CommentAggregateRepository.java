package com.sublinks.sublinksapi.comment.repositories;

import com.sublinks.sublinksapi.comment.entities.Comment;
import com.sublinks.sublinksapi.comment.entities.CommentAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CommentAggregateRepository extends JpaRepository<CommentAggregate, Long> {

  Optional<CommentAggregate> findByComment_Path(String path);

}
