package com.sublinks.sublinksapi.authorization.repositories;

import com.sublinks.sublinksapi.authorization.entities.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long>, RoleSearchRepository {

  Optional<Role> findFirstByName(String name);

  List<Role> findAllByNameIsLikeIgnoreCase(String name, Pageable pageable);
}
