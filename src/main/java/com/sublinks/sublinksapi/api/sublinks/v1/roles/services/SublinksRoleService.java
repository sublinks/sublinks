package com.sublinks.sublinksapi.api.sublinks.v1.roles.services;

import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SortOrder;
import com.sublinks.sublinksapi.api.sublinks.v1.roles.models.CreateRole;
import com.sublinks.sublinksapi.api.sublinks.v1.roles.models.IndexRole;
import com.sublinks.sublinksapi.api.sublinks.v1.roles.models.RoleResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.roles.models.UpdateRole;
import com.sublinks.sublinksapi.authorization.entities.Role;
import com.sublinks.sublinksapi.authorization.entities.RolePermissions;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionRoleTypes;
import com.sublinks.sublinksapi.authorization.repositories.RoleRepository;
import com.sublinks.sublinksapi.authorization.services.AclService;
import com.sublinks.sublinksapi.authorization.services.RoleService;
import com.sublinks.sublinksapi.person.entities.Person;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@Service
public class SublinksRoleService {

  private final ConversionService conversionService;
  private final AclService aclService;
  private final RoleRepository roleRepository;
  private final RoleService roleService;

  public List<RoleResponse> indexRole(final IndexRole indexRoleForm, final Person person)
  {

    aclService.canPerson(person)
        .performTheAction(RolePermissionRoleTypes.ROLE_READ)
        .orThrowUnauthorized();

    final List<Role> roles = new java.util.ArrayList<>();

    final int page = indexRoleForm.page() != null ? Math.max(indexRoleForm.page(), 0) : 0;
    final int limit = indexRoleForm.limit() != null ? Math.max(Math.min(indexRoleForm.limit(), 20),
        0) : 20;

    Sort sortOrder = indexRoleForm.sort() != null && indexRoleForm.sort()
        .equals(SortOrder.Desc) ? Sort.by("name")
        .descending() : Sort.by("name")
        .ascending();

    PageRequest pageRequest = PageRequest.of(page, limit, sortOrder);

    if (indexRoleForm.search() != null) {
      roles.addAll(
          roleRepository.findAllByNameIsLikeIgnoreCase(indexRoleForm.search(), pageRequest));
    } else {
      roles.addAll(roleRepository.findAll(pageRequest)
          .stream()
          .toList());
    }

    return roles.stream()
        .map(role -> conversionService.convert(role, RoleResponse.class))
        .toList();
  }

  public RoleResponse show(@NonNull final String key, final Person person)
  {

    aclService.canPerson(person)
        .performTheAction(RolePermissionRoleTypes.ROLE_READ)
        .orThrowUnauthorized();

    return roleRepository.findFirstByName(key)
        .map(role -> conversionService.convert(role, RoleResponse.class))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "role not found"));
  }

  /**
   * Creates a new role with the provided information.
   *
   * @param createRoleForm The CreateRole object containing the details of the role to be created.
   *                       Must not be null.
   * @param person         The Person object representing the user performing the action. Must not
   *                       be null.
   * @return The RoleResponse object representing the newly created role.
   * @throws ResponseStatusException if the user is not authorized to perform the action.
   */
  public RoleResponse create(@NonNull final CreateRole createRoleForm, final Person person)
  {

    aclService.canPerson(person)
        .performTheAction(RolePermissionRoleTypes.ROLE_CREATE)
        .orThrowUnauthorized();

    final Role role = Role.builder()
        .name(createRoleForm.name())
        .description(createRoleForm.description())
        .isActive(createRoleForm.active())
        .rolePermissions(createRoleForm.permissions()
            .stream()
            .map(permission -> RolePermissions.builder()
                .permission(permission)
                .build())
            .collect(Collectors.toSet()))
        .expiresAt(createRoleForm.expiresAt() != null ? new Date(createRoleForm.expiresAt() * 1000L)
            : null)
        .build();

    return conversionService.convert(roleService.createRole(role), RoleResponse.class);
  }

  /**
   * Updates an existing role with the provided information.
   *
   * @param key            The key of the role to be updated. Must not be null.
   * @param updateRoleForm The UpdateRole object containing the updated details of the role. Must
   *                       not be null.
   * @param person         The Person object representing the user performing the action. Must not
   *                       be null.
   * @return The RoleResponse object representing the updated role.
   * @throws ResponseStatusException if the user is not authorized to perform the action or if the
   *                                 role is not found.
   */
  public RoleResponse update(final String key, @NonNull final UpdateRole updateRoleForm,
      final Person person)
  {

    aclService.canPerson(person)
        .performTheAction(RolePermissionRoleTypes.ROLE_UPDATE)
        .orThrowUnauthorized();

    final Role role = roleRepository.findFirstByName(key)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "role_not_found"));

    if (updateRoleForm.name() != null) {
      role.setName(updateRoleForm.name());
    }
    if (updateRoleForm.description() != null) {
      role.setDescription(updateRoleForm.description());
    }
    if (updateRoleForm.active() != null) {
      role.setActive(updateRoleForm.active());
    }
    if (updateRoleForm.permissions() != null) {
      role.setRolePermissions(updateRoleForm.permissions()
          .stream()
          .map(permission -> RolePermissions.builder()
              .permission(permission)
              .build())
          .collect(Collectors.toSet()));
    }
    if (updateRoleForm.expiresAt() != null) {
      role.setExpiresAt(new Date(updateRoleForm.expiresAt() * 1000L));
    }

    return conversionService.convert(roleService.updateRole(role), RoleResponse.class);
  }

  /**
   *
   */
  public void delete(final String key, final Person person)
  {

    aclService.canPerson(person)
        .performTheAction(RolePermissionRoleTypes.ROLE_DELETE)
        .orThrowUnauthorized();

    final Role role = roleRepository.findFirstByName(key)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "role_not_found"));

    roleService.deleteRole(role);
  }
}