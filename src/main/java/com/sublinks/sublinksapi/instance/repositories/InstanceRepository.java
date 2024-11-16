package com.sublinks.sublinksapi.instance.repositories;

import com.sublinks.sublinksapi.instance.entities.Instance;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InstanceRepository extends JpaRepository<Instance, Long> {

  Instance findInstanceByDomain(String domain);

  @Query(value = "SELECT s.* FROM instances s WHERE s.search_vector @@ to_tsquery('english', :keyword)",
      countQuery = "SELECT COUNT(s.id) FROM instances s WHERE s.search_vector @@ to_tsquery('english', :keyword)",
      nativeQuery = true)
  List<Instance> findInstancesByDomainOrDescriptionOrSidebar(@Param("keyword") String keyword, Pageable pageable);

}
