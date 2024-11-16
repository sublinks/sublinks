package com.sublinks.sublinksapi.authorization.services;

import com.sublinks.sublinksapi.authorization.AclEntityInterface;
import com.sublinks.sublinksapi.authorization.entities.Acl;
import com.sublinks.sublinksapi.authorization.enums.AuthorizedEntityType;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionInterface;
import com.sublinks.sublinksapi.authorization.repositories.AclRepository;
import com.sublinks.sublinksapi.comment.entities.Comment;
import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.post.entities.Post;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * The AclService class provides methods for handling Access Control List (ACL) policies.
 */
@Service
@RequiredArgsConstructor
public class AclService {

  private final AclRepository aclRepository;
  private final RolePermissionService rolePermissionService;
  private final RoleService roleService;

  /**
   * Determines if a person is allowed to perform an action according to the ACL rules.
   *
   * @param person The person to check. Null is allowed and it will default to a new Person object.
   * @return An EntityPolicy object representing the result of the authorization check.
   */
  public EntityPolicy canPerson(final Person person) {

    if (person == null) {
      return new EntityPolicy(ActionType.check, aclRepository, rolePermissionService, roleService);
    }
    return new EntityPolicy(person, ActionType.check, aclRepository, rolePermissionService,
        roleService);
  }

  /**
   * Determines if a person is allowed to perform an action according to the ACL rules.
   *
   * @param person The person to check. Null is allowed and it will default to a new Person object.
   * @return An EntityPolicy object representing the result of the authorization check.
   */
  public EntityPolicy allowPerson(final Person person) {

    if (person == null) {
      return new EntityPolicy(ActionType.allow, aclRepository, rolePermissionService, roleService);
    }
    return new EntityPolicy(person, ActionType.allow, aclRepository, rolePermissionService,
        roleService);
  }

  /**
   * Revoke the authorization for a person to perform actions according to ACL rules.
   *
   * @param person The person to revoke the authorization for. Pass null to revoke authorization for
   *               a new Person object.
   * @return An EntityPolicy object representing the result of the authorization revocation.
   */
  public EntityPolicy revokePerson(final Person person) {

    if (person == null) {
      return new EntityPolicy(ActionType.revoke, aclRepository, rolePermissionService, roleService);
    }
    return new EntityPolicy(person, ActionType.revoke, aclRepository, rolePermissionService,
        roleService);
  }

  /**
   * The ActionType enum represents the different types of actions that can be performed in the ACL
   * system.
   */
  public enum ActionType {
    check,
    allow,
    revoke,
  }

  /**
   * The EntityPolicy class represents the policy for entity authorization based on ACL rules. It
   * provides methods to configure the policy and perform authorization checks.
   */
  public static class EntityPolicy {

    private final Person person;
    private final ActionType actionType;
    private final AclRepository aclRepository;
    private final RolePermissionService rolePermissionService;
    private final RoleService roleService;
    private final List<RolePermissionInterface> authorizedActions = new ArrayList<>();
    private boolean isPermitted = true;
    private AuthorizedEntityType entityType;
    private Long entityId;
    private Community community;

    /**
     * The EntityPolicy class represents a policy for accessing and manipulating entities in an
     * Access Control List (ACL) system.
     *
     * @param actionType    the type of action to be performed
     * @param aclRepository the repository for accessing and manipulating ACL entities
     */
    public EntityPolicy(final ActionType actionType, final AclRepository aclRepository,
        final RolePermissionService rolePermissionService, RoleService roleService)
    {

      this.roleService = roleService;

      this.person = null;
      this.actionType = actionType;
      this.aclRepository = aclRepository;
      this.rolePermissionService = rolePermissionService;
    }

    /**
     * EntityPolicy represents a policy for accessing and manipulating entities in an Access Control
     * List (ACL) system.
     *
     * @param person        the person associated with the policy
     * @param actionType    the type of action to be performed
     * @param aclRepository the repository for accessing and manipulating ACL entities
     */
    public EntityPolicy(final Person person, final ActionType actionType,
        final AclRepository aclRepository, RolePermissionService rolePermissionService,
        RoleService roleService)
    {

      this.person = person;
      this.actionType = actionType;
      this.aclRepository = aclRepository;
      this.rolePermissionService = rolePermissionService;
      this.roleService = roleService;
    }

    /**
     * Adds an authorized action to the EntityPolicy.
     *
     * @param permission the authorized action to be added
     * @return the EntityPolicy object
     */
    public EntityPolicy performTheAction(final RolePermissionInterface permission) {

      this.authorizedActions.add(permission);
      return this;
    }

    public EntityPolicy onCommunity(final Community community) {

      this.community = community;
      return this;
    }

    /**
     * Executes the specified action on the provided entity and sets the entity type and ID for the
     * policy.
     *
     * @param entity the entity on which the action will be performed
     * @return the modified EntityPolicy object
     */
    public EntityPolicy onEntity(final AclEntityInterface entity) {

      this.entityType = entity.entityType();
      this.entityId = entity.getId();

      switch (entityType) {
        case post: {
          this.community = ((Post) entity).getCommunity();
          break;
        }
        case comment: {
          this.community = ((Comment) entity).getCommunity();
          break;
        }
        default: {
          break;
        }
      }

      return this;
    }

    /**
     * Checks if the current policy is permitted.
     *
     * @return true if the policy is permitted, false otherwise
     */
    public boolean isPermitted() {

      execute();
      return isPermitted;
    }

    /**
     * Throws an exception if the specified condition is not satisfied.
     *
     * @param exceptionSupplier supplier that provides the exception to be thrown
     * @param <X>               the type of exception to be thrown
     * @throws X the thrown exception if the condition is not satisfied
     */
    public <X extends Throwable> void orElseThrow(Supplier<? extends X> exceptionSupplier) throws X
    {

      execute();
      if (!isPermitted) {
        throw exceptionSupplier.get();
      }
    }

    /**
     * Executes the specified action based on the provided parameters.
     */
    private void execute() {

      if (entityType == null && entityId == null) {
        switch (actionType) {
          case allow -> throw new RuntimeException("Permissions cannot be granted to roles.");
          case revoke -> throw new RuntimeException("Permissions cannot be revoked from roles.");
          default -> checkRolePermission();
        }
        return;
      }
      switch (actionType) {
        case allow -> createAclRules();
        case revoke -> revokeAclRules();
        default -> {
          checkAclRules();
          checkRoles();
        }
      }
    }

    /**
     * Checks the role permission for the current policy.
     * <p>
     * This method checks if the person associated with the policy is banned in the specified
     * community or in general. If banned, it checks if the authorized action is allowed in the
     * banned role to determine if the policy is permitted. If not banned, it checks if the person
     * has the required role permission to perform the authorized action.
     */
    private void checkRolePermission() {

      if (this.person != null) {
        if (community != null) {
          this.isPermitted = this.authorizedActions.stream()
              .allMatch(permission -> rolePermissionService.isPermitted(this.person, permission,
                  community.getId()));
        } else {
          this.isPermitted = this.authorizedActions.stream()
              .allMatch(permission -> rolePermissionService.isPermitted(this.person, permission));
        }
      } else {
        this.isPermitted = this.authorizedActions.stream()
            .allMatch(permission -> rolePermissionService.isPermitted(
                roleService.getDefaultGuestRole()
                    .orElseThrow(() -> new RuntimeException("No default registered role defined.")),
                permission));
      }
    }

    /**
     * Checks the ACL rules for the authorized actions.
     */
    private void checkAclRules() {

      for (RolePermissionInterface authorizedAction : authorizedActions) {
        Acl acl = aclRepository.findAclByPersonIdAndEntityTypeAndEntityIdAndAuthorizedAction(
            person.getId(), entityType, entityId, authorizedAction.toString());
        if (acl != null && !acl.isPermitted()) {
          isPermitted = false;
        }
        if (acl == null) {
          isPermitted = false;
        }
      }
    }

    /**
     * Checks the roles of the current policy.
     *
     * <p>
     * This method checks if the person associated with the policy is banned in the specified
     * community or in general. If the person is banned, it checks if the authorized action is
     * allowed in the banned role to determine if the policy is permitted. If the person is not
     * banned, it checks if the person has the required role permission to perform the authorized
     * action.
     */
    private void checkRoles() {

      // Only permitted if the authorized action allowed in the banned role
      if (this.person != null) {
        if (RolePermissionService.isBanned(this.person)) {
          this.isPermitted = this.authorizedActions.stream()
              .allMatch(role -> rolePermissionService.isPermitted(roleService.getBannedRole()
                  .orElseThrow(() -> new RuntimeException("No banned role defined.")), role));
        }
        if (community != null) {
          if (rolePermissionService.isBannedInCommunity(this.person, community.getId())) {
            this.isPermitted = this.authorizedActions.stream()
                .allMatch(role -> rolePermissionService.isPermitted(roleService.getBannedRole()
                    .orElseThrow(() -> new RuntimeException("No banned role defined.")), role));
          }
        }
      }
    }

    /**
     * Revoke ACL rules for the authorized actions.
     */
    private void revokeAclRules() {

      if (person == null) {
        throw new RuntimeException("Person can not be null.");
      }

      for (RolePermissionInterface authorizedAction : authorizedActions) {
        Acl acl = aclRepository.findAclByPersonIdAndEntityTypeAndEntityIdAndAuthorizedActionAndPermitted(
            person.getId(), entityType, entityId, authorizedAction.toString(), true);
        if (acl == null) {
          aclRepository.saveAndFlush(Acl.builder()
              .personId(person.getId())
              .entityType(entityType)
              .entityId(entityId)
              .authorizedAction(authorizedAction.toString())
              .permitted(false)
              .build());
        } else {
          acl.setPermitted(false);
          aclRepository.saveAndFlush(acl);
        }
      }
    }

    /**
     * Creates ACL rules for the authorized actions.
     */
    private void createAclRules() {

      if (person == null) {
        throw new RuntimeException("Person can not be null.");
      }

      for (RolePermissionInterface authorizedAction : authorizedActions) {
        Acl acl = aclRepository.findAclByPersonIdAndEntityTypeAndEntityIdAndAuthorizedActionAndPermitted(
            person.getId(), entityType, entityId, authorizedAction.toString(), false);
        if (acl == null) {
          aclRepository.saveAndFlush(Acl.builder()
              .personId(person.getId())
              .entityType(entityType)
              .entityId(entityId)
              .authorizedAction(authorizedAction.toString())
              .permitted(true)
              .build());
        } else {
          acl.setPermitted(true);
          aclRepository.saveAndFlush(acl);
        }
      }
    }

    public void orThrowUnauthorized() {

      execute();
      if (!isPermitted) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized");
      }
    }

    public void orThrowBadRequest() {

      execute();
      if (!isPermitted) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bad_request");
      }
    }
  }
}
