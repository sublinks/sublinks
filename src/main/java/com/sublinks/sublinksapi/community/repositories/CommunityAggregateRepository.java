package com.sublinks.sublinksapi.community.repositories;

import com.sublinks.sublinksapi.community.entities.CommunityAggregate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityAggregateRepository extends JpaRepository<CommunityAggregate, Long> {

}
