package com.sublinks.sublinksapi.authorization.events;

import com.sublinks.sublinksapi.authorization.entities.Role;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class RoleUpdatedPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  public RoleUpdatedPublisher(final ApplicationEventPublisher applicationEventPublisher)
  {

    this.applicationEventPublisher = applicationEventPublisher;
  }

  public void publish(final Role role) {

    RoleUpdatedEvent roleUpdatedEvent = new RoleUpdatedEvent(this, role);
    applicationEventPublisher.publishEvent(roleUpdatedEvent);
  }
}
