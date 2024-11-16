package com.sublinks.sublinksapi.authorization.events;

import com.sublinks.sublinksapi.authorization.entities.Role;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class RoleCreatedPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  public RoleCreatedPublisher(final ApplicationEventPublisher applicationEventPublisher)
  {

    this.applicationEventPublisher = applicationEventPublisher;
  }

  public void publish(final Role role) {

    RoleCreatedEvent roleCreatedEvent = new RoleCreatedEvent(this, role);
    applicationEventPublisher.publishEvent(roleCreatedEvent);
  }
}
