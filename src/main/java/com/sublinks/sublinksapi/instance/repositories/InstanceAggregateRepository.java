package com.sublinks.sublinksapi.instance.repositories;

import com.sublinks.sublinksapi.instance.entities.InstanceAggregate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstanceAggregateRepository extends JpaRepository<InstanceAggregate, Long> {

  InstanceAggregate findByInstance_Domain(String domain);

}
