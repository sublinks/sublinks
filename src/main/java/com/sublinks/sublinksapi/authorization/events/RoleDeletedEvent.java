package com.sublinks.sublinksapi.authorization.events;

import com.sublinks.sublinksapi.authorization.entities.Role;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RoleDeletedEvent extends ApplicationEvent {

  private final Role role;

  public RoleDeletedEvent(final Object source, final Role updatedRole)
  {

    super(source);
    this.role = updatedRole;
  }
}
