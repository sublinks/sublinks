package com.sublinks.sublinksapi.authorization.events;

import com.sublinks.sublinksapi.authorization.entities.Role;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class RoleDeletedPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  public RoleDeletedPublisher(final ApplicationEventPublisher applicationEventPublisher)
  {

    this.applicationEventPublisher = applicationEventPublisher;
  }

  public void publish(final Role role) {

    RoleDeletedEvent roleDeletedEvent = new RoleDeletedEvent(this, role);
    applicationEventPublisher.publishEvent(roleDeletedEvent);
  }
}
