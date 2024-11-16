package com.sublinks.sublinksapi.post.repositories;

import com.sublinks.sublinksapi.post.entities.Post;
import com.sublinks.sublinksapi.shared.RemovedState;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositorySearch {

  Optional<Post> findByTitleSlugAndRemovedStateIs(String titleSlug, RemovedState removedState);

  Optional<Post> findByTitleSlug(String titleSlug);
}
