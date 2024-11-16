package com.sublinks.sublinksapi.authorization.events;

import com.sublinks.sublinksapi.authorization.entities.Role;
import com.sublinks.sublinksapi.person.entities.LinkPersonCommunity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RoleCreatedEvent extends ApplicationEvent {

  private final Role role;

  public RoleCreatedEvent(
      final Object source,
      final Role createdRole
  ) {

    super(source);
    this.role = createdRole;
  }
}
