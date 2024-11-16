package com.sublinks.sublinksapi.authorization.services;

import com.sublinks.sublinksapi.authorization.entities.Role;
import com.sublinks.sublinksapi.authorization.entities.RolePermissions;
import com.sublinks.sublinksapi.authorization.enums.RoleTypes;
import com.sublinks.sublinksapi.authorization.events.RoleCreatedPublisher;
import com.sublinks.sublinksapi.authorization.events.RoleDeletedPublisher;
import com.sublinks.sublinksapi.authorization.events.RoleUpdatedPublisher;
import com.sublinks.sublinksapi.authorization.repositories.RoleRepository;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.repositories.PersonRepository;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository roleRepository;
  private final PersonRepository personRepository;
  private final RoleCreatedPublisher roleCreatedEventPublisher;
  private final RoleUpdatedPublisher roleUpdatedEventPublisher;
  private final RoleDeletedPublisher roleDeletedEventPublisher;


  /**
   * Retrieves the admin role from the role repository.
   *
   * @return An Optional object containing the admin role if found, otherwise an empty Optional.
   */
  public Optional<Role> getAdminRole() {

    return roleRepository.findFirstByName(RoleTypes.ADMIN.toString());
  }

  /**
   * Retrieves the admin role using a supplier to throw an exception if the role is not found.
   *
   * @param supplier A Supplier that provides the exception to throw if the admin role is not
   *                 found.
   * @param <X>      The type of exception to throw.
   * @return The admin role if found.
   * @throws X The exception provided by the supplier if the admin role is not found.
   */
  public <X extends Throwable> Role getAdminRole(Supplier<? extends X> supplier) throws X {

    return getAdminRole().orElseThrow(supplier);
  }

  /**
   * Retrieves the default registered role from the role repository.
   *
   * @return An Optional object containing the default registered role if found, otherwise an empty
   * Optional.
   */
  public Optional<Role> getDefaultRegisteredRole() {

    return roleRepository.findFirstByName(RoleTypes.REGISTERED.toString());
  }

  /**
   * Retrieves the default registered role from the role repository and throws a specified exception
   * if not found.
   *
   * @param supplier A Supplier that provides the exception to throw if the default registered role
   *                 is not found.
   * @param <X>      The type of exception to throw.
   * @return The default registered role if found.
   * @throws X The exception provided by the supplier if the default registered role is not found.
   */
  public <X extends Throwable> Role getDefaultRegisteredRole(Supplier<? extends X> supplier)
      throws X
  {

    return getDefaultRegisteredRole().orElseThrow(supplier);
  }

  /**
   * Retrieves the default guest role from the role repository.
   *
   * @return An Optional object containing the default guest role if found, otherwise an empty
   * Optional.
   */
  public Optional<Role> getDefaultGuestRole() {

    return roleRepository.findFirstByName(RoleTypes.GUEST.toString());
  }

  /**
   * Retrieves the default guest role from the role repository and throws a specified exception if
   * not found.
   *
   * @param supplier A Supplier that provides the exception to throw if the default guest role is
   *                 not found.
   * @param <X>      The type of exception to throw.
   * @return The default guest role if found.
   * @throws X The exception provided by the supplier if the default guest role is not found.
   */
  public <X extends Throwable> Role getDefaultGuestRole(Supplier<? extends X> supplier) throws X {

    return getDefaultGuestRole().orElseThrow(supplier);
  }

  /**
   * Retrieves the banned role from the role repository.
   *
   * @return An Optional object containing the banned role if found, otherwise an empty Optional.
   */
  public Optional<Role> getBannedRole() {

    return roleRepository.findFirstByName(RoleTypes.BANNED.toString());
  }

  /**
   * Retrieves the banned role from the role repository and throws a specified exception if not
   * found.
   *
   * @param supplier A Supplier that provides the exception to throw if the banned role is not
   *                 found.
   * @param <X>      The type of exception to throw.
   * @return The banned role if found.
   * @throws X The exception provided by the supplier if the banned role is not found.
   */
  public <X extends Throwable> Role getBannedRole(Supplier<? extends X> supplier) throws X {

    return getBannedRole().orElseThrow(supplier);
  }

  /**
   * Retrieves a set of administrators.
   *
   * @return A set of Person objects representing administrators.
   */
  public Set<Person> getAdmins() {

    return personRepository.findAllByRole(getAdminRole(() -> new RuntimeException(
        "Cannot produce list of admins because the Admin role doesn't exist.")));
  }

  /**
   * Retrieves a set of banned users.
   *
   * @return A set of Person objects representing banned users.
   */
  public Set<Person> getBannedUsers() {

    return personRepository.findAllByRole(getBannedRole(() -> new RuntimeException(
        "Cannot produce list of banned people because the Banned role doesn't exist.")));
  }

  public Role createRole(Role role) {

    roleRepository.save(role);
    roleCreatedEventPublisher.publish(role);

    return role;
  }

  public Role updateRole(Role role) {

    roleRepository.save(role);
    roleUpdatedEventPublisher.publish(role);

    return role;
  }

  public void deleteRole(Role role) {

    roleRepository.delete(role);
    roleDeletedEventPublisher.publish(role);
  }

  public RolePermissions getOrCreateRolePermission(Role role, String permission) {

    return role.getRolePermissions()
        .stream()
        .filter(rolePermission -> rolePermission.getPermission()
            .equals(permission))
        .findFirst()
        .orElseGet(() -> {

          RolePermissions rolePermission = RolePermissions.builder()
              .role(role)
              .permission(permission)
              .build();

          role.getRolePermissions()
              .add(rolePermission);
          this.updateRole(role);

          return rolePermission;
        });
  }
}
