package com.sublinks.sublinksapi.community.repositories;

import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.instance.entities.Instance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, Long>,
    CommunitySearchRepository {

  Community findCommunityByIdOrTitleSlug(Long id, String titleSlug);

  Community findCommunityByIsLocalTrueAndTitleSlug(String titleSlug);

  Optional<Community> findCommunityByTitleSlug(String titleSlug);

  List<Community> findCommunityByTitleSlugIn(List<String> titleSlug);

  boolean existsByTitleSlug(String titleSlug);

  List<Community> findCommunitiesByInstance(Instance instance);

  Community findCommunityByPublicKey(String publicKey);
}
