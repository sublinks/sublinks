package com.sublinks.sublinksapi.authorization.events;

import com.sublinks.sublinksapi.authorization.entities.Role;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RoleUpdatedEvent extends ApplicationEvent {

  private final Role role;

  public RoleUpdatedEvent(final Object source, final Role updatedRole)
  {

    super(source);
    this.role = updatedRole;
  }
}
