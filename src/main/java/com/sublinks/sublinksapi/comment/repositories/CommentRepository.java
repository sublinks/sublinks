package com.sublinks.sublinksapi.comment.repositories;

import com.sublinks.sublinksapi.comment.entities.Comment;
import com.sublinks.sublinksapi.person.entities.Person;
import java.util.List;
import java.util.Optional;
import com.sublinks.sublinksapi.post.entities.Post;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositorySearch {

  List<Comment> findByPerson(Person person);

  Optional<Comment> findByPost(Post post);

  Optional<Comment> findByPath(String path);
}
