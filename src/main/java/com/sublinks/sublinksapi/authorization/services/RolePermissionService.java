package com.sublinks.sublinksapi.authorization.services;

import com.sublinks.sublinksapi.authorization.entities.Role;
import com.sublinks.sublinksapi.authorization.entities.RolePermissions;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionInterface;
import com.sublinks.sublinksapi.authorization.enums.RoleTypes;
import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.community.repositories.CommunityRepository;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.enums.LinkPersonCommunityType;
import com.sublinks.sublinksapi.person.services.LinkPersonCommunityService;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

/**
 * Service class for role authorization.
 */
@Service
@RequiredArgsConstructor
public class RolePermissionService {

  private final RoleService roleService;
  private final LinkPersonCommunityService linkPersonCommunityService;
  private final CommunityRepository communityRepository;
  private final ConversionService conversionService;

  /**
   * Checks if a role is banned.
   *
   * @param role The role to check for ban status.
   * @return True if the role is banned, false otherwise.
   */
  public static boolean isBanned(final Role role) {

    return Objects.equals(role.getName(), RoleTypes.BANNED.toString());
  }

  /**
   * Checks if a person is banned.
   *
   * @param person The person to check for ban status.
   * @return True if the person is banned, false otherwise.
   */
  public static boolean isBanned(final Person person) {

    if (person == null || person.getRole() == null) {
      return false;
    }
    return isBanned(person.getRole());
  }

  /**
   * Checks if the given person is banned in the specified community.
   *
   * @param person    The person to check for ban status. Cannot be null.
   * @param community The community to check. Cannot be null.
   * @return True if the person is banned in the community, false otherwise.
   */
  public boolean isBannedInCommunity(final Person person, final Community community) {

    if (person == null || person.getRole() == null) {
      return false;
    }
    return linkPersonCommunityService.hasLink(community, person, LinkPersonCommunityType.banned);
  }

  public boolean isBannedInCommunity(final Person person, final Long communityId) {

    if (person == null || person.getRole() == null) {
      return false;
    }
    final Community community = communityRepository.getReferenceById(communityId);
    return isBannedInCommunity(person, community);
  }

  /**
   * Checks if the given person is an admin.
   *
   * @param person The person to check.
   * @return True if the person is an admin, false otherwise.
   */
  public static boolean isAdmin(final Person person) {

    if (person == null || person.getRole() == null) {
      return false;
    }
    return isAdmin(person.getRole());
  }

  /**
   * Checks if the given role is an admin.
   *
   * @param role The role to check.
   * @return True if the role is an admin, false otherwise.
   */
  public static boolean isAdmin(@NonNull final Role role) {

    return Objects.equals(role.getName(), RoleTypes.ADMIN.toString());
  }

  /**
   * Checks if the given person is an admin. If the person is not an admin, it throws the specified
   * exception.
   *
   * @param person            The person to check.
   * @param exceptionSupplier A supplier that provides the exception to throw if the person is not
   *                          an admin.
   * @param <X>               The type of exception thrown.
   * @throws X The exception provided by the exceptionSupplier if the person is not an admin.
   */
  public static <X extends Throwable> void isAdminElseThrow(Person person,
      Supplier<? extends X> exceptionSupplier) throws X
  {

    if (!isAdmin(person)) {
      throw exceptionSupplier.get();
    }
  }

  /**
   * Checks if the given person is permitted to perform the specified role permission.
   *
   * @param person         The person to check. Can be null.
   * @param rolePermission The permission to check.
   * @return True if the person is permitted, false otherwise.
   */
  public boolean isPermitted(@Nullable final Person person,
      final RolePermissionInterface rolePermission)
  {

    final Role role = person == null ? roleService.getDefaultGuestRole(
        () -> new RuntimeException("No Guest role found.")) : person.getRole();

    return isAdmin(role) || doesRoleHavePermission(role, rolePermission);
  }

  /**
   * Checks if the given role is permitted to perform the specified role permission.
   *
   * @param role           The role to check.
   * @param rolePermission The permission to check.
   * @return True if the role is permitted, false otherwise.
   */
  public boolean isPermitted(@NonNull final Role role, final RolePermissionInterface rolePermission)
  {

    return isAdmin(role) || doesRoleHavePermission(role, rolePermission);
  }

  /**
   * Checks if the given role is permitted to perform any of the specified role permissions.
   *
   * @param role            The role to check.
   * @param rolePermissions The set of role permissions to check.
   * @return True if the role is permitted, false otherwise.
   */
  public boolean isPermitted(@NonNull final Role role,
      final Set<RolePermissionInterface> rolePermissions)
  {

    return rolePermissions.stream()
        .anyMatch(x -> doesRoleHavePermission(role, x));
  }

  /**
   * Checks if the given role is permitted to perform any of the specified role permissions.
   *
   * @param role              The role to check.
   * @param rolePermissions   The set of role permissions to check.
   * @param exceptionSupplier A supplier that provides the exception to throw if the role is not
   *                          permitted.
   * @param <X>               The type of exception thrown.
   * @throws X The exception provided by the exceptionSupplier if the role is not permitted.
   */
  public <X extends Throwable> void isPermitted(@NonNull final Role role,
      final Set<RolePermissionInterface> rolePermissions, Supplier<? extends X> exceptionSupplier)
      throws X
  {

    if (!isPermitted(role, rolePermissions)) {
      throw exceptionSupplier.get();
    }
  }

  /**
   * Checks if the given person is permitted to perform the specified role permissions.
   *
   * @param person            The person to check. Can be null.
   * @param rolePermissions   The set of role permissions to check.
   * @param exceptionSupplier A supplier that provides the exception to throw if the person is not
   *                          permitted.
   * @param <X>               The type of exception thrown.
   * @throws X The exception provided by the exceptionSupplier if the person is not permitted.
   */
  public <X extends Throwable> void isPermitted(final Person person,
      final Set<RolePermissionInterface> rolePermissions, Supplier<? extends X> exceptionSupplier)
      throws X
  {

    final Role role = person == null ? roleService.getDefaultGuestRole(
        () -> new RuntimeException("No Guest role found.")) : person.getRole();

    isPermitted(role, rolePermissions, exceptionSupplier);
  }

  /**
   * Checks if a role is permitted based on their role and permission.
   *
   * @param role              The role to check.
   * @param rolePermission    The permission to check.
   * @param exceptionSupplier A supplier that provides the exception to throw if the role is not
   *                          permitted.
   * @param <X>               The type of exception thrown.
   * @throws X The exception provided by the exceptionSupplier if the role is not permitted.
   */
  public <X extends Throwable> void isPermitted(@NonNull final Role role,
      final RolePermissionInterface rolePermission, Supplier<? extends X> exceptionSupplier)
      throws X
  {

    if (!isPermitted(role, rolePermission)) {
      throw exceptionSupplier.get();
    }
  }

  /**
   * Checks if a person is permitted based on their role and permission.
   *
   * @param person            The person to check. Can be null.
   * @param rolePermission    The permission to check.
   * @param exceptionSupplier A supplier that provides the exception to throw if the person is not
   *                          permitted.
   * @param <X>               The type of exception thrown.
   * @throws X The exception provided by the exceptionSupplier if the person is not permitted.
   */
  public <X extends Throwable> void isPermitted(final Person person,
      final RolePermissionInterface rolePermission, Supplier<? extends X> exceptionSupplier)
      throws X
  {

    if (person != null && person.isDeleted()) {
      throw exceptionSupplier.get();
    }

    final Role role = person == null ? roleService.getDefaultGuestRole(
        () -> new RuntimeException("No Guest role found.")) : person.getRole();

    isPermitted(role, rolePermission, exceptionSupplier);
  }

  /**
   * Checks if the given person is permitted to perform the specified role permission.
   *
   * @param person         The person to check. Can be null.
   * @param rolePermission The permission to check.
   * @param communityId    The ID of the community to check.
   * @return True if the person is permitted, false otherwise.
   */
  public boolean isPermitted(final Person person, final RolePermissionInterface rolePermission,
      final Long communityId)
  {

    if (person != null && person.isDeleted()) {
      return false;
    }
    Role role;

    if (person != null) {
      role = isBannedInCommunity(person, communityId) ? this.roleService.getBannedRole()
          .orElseThrow(() -> new RuntimeException("No Banned role found.")) : person.getRole();
    } else {
      role = roleService.getDefaultGuestRole(() -> new RuntimeException("No Guest role found."));
    }

    return isAdmin(role) || doesRoleHavePermission(role, rolePermission);
  }

  /**
   * Checks if the given person is permitted to perform the specified role permission.
   *
   * @param person            The person to check. Can be null.
   * @param rolePermission    The permission to check.
   * @param communityId       The ID of the community to check.
   * @param exceptionSupplier A supplier that provides the exception to throw if the person is not
   *                          permitted.
   * @param <X>               The type of exception thrown.
   * @throws X The exception provided by the exceptionSupplier if the person is not permitted.
   */
  public <X extends Throwable> void isPermitted(final Person person,
      final RolePermissionInterface rolePermission, final Long communityId,
      final Supplier<? extends X> exceptionSupplier) throws X
  {

    if (person != null && person.isDeleted()) {
      throw exceptionSupplier.get();
    }
    final Role role = person == null ? roleService.getDefaultGuestRole(
        () -> new RuntimeException("No Guest role found.")) : isBannedInCommunity(person,
        communityId) ? this.roleService.getBannedRole()
        .orElseThrow(() -> new RuntimeException("No Banned role found.")) : person.getRole();

    if (!isAdmin(role) && !doesRoleHavePermission(role, rolePermission)) {
      throw exceptionSupplier.get();
    }
  }

  /**
   * Checks if a role has a specific permission.
   *
   * @param role           The role to check.
   * @param rolePermission The permission to check.
   * @return True if the role has the permission, false otherwise.
   */
  private boolean doesRoleHavePermission(final Role role,
      final RolePermissionInterface rolePermission)
  {

    if (isAdmin(role)) {
      return true;
    }
    return getRolePermissions(role).stream()
        .anyMatch(x -> x.toString()
            .equals(rolePermission.toString()));
  }

  public Set<RolePermissionInterface> getRolePermissions(final Role role) {

    final Set<RolePermissions> rolePermissions = new HashSet<>();

    Role currentRole = role;

    while (currentRole != null) {
      rolePermissions.addAll(currentRole.getRolePermissions());
      currentRole = currentRole.getInheritsFrom();
    }

    return rolePermissions.stream()
        .map(x -> conversionService.convert(x.getPermission(), RolePermissionInterface.class))
        .collect(Collectors.toSet());
  }
}
