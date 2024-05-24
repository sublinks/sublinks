package com.sublinks.sublinksapi.authorization.listeners;

import com.sublinks.sublinksapi.authorization.enums.AuthorizeAction;
import com.sublinks.sublinksapi.person.dto.Person;
import com.sublinks.sublinksapi.post.events.PostCreatedEvent;
import com.sublinks.sublinksapi.post.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorizationPostCreatedListener implements ApplicationListener<PostCreatedEvent> {

  private final PostService postService;

  @Override
  public void onApplicationEvent(PostCreatedEvent event) {

    Person person = postService.getPostCreator(event.getPost());
  }
}
