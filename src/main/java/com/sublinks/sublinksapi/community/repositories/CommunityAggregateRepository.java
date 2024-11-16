package com.sublinks.sublinksapi.community.repositories;

import com.sublinks.sublinksapi.community.entities.CommunityAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommunityAggregateRepository extends JpaRepository<CommunityAggregate, Long> {

  @Query("SELECT ca FROM CommunityAggregate ca WHERE ca.community.titleSlug = :key")
  CommunityAggregate findByCommunityKey(String key);

}
