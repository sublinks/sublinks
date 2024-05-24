package com.sublinks.sublinksapi.community.listeners;

import com.sublinks.sublinksapi.comment.events.CommentCreatedEvent;
import com.sublinks.sublinksapi.community.dto.CommunityAggregate;
import com.sublinks.sublinksapi.community.repositories.CommunityAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class CommunityCommentCreatedListener implements ApplicationListener<CommentCreatedEvent> {

  private final CommunityAggregateRepository communityAggregateRepository;

  @Override
  public void onApplicationEvent(CommentCreatedEvent event) {

    final CommunityAggregate communityAggregate = event.getComment().getCommunity()
        .getCommunityAggregate();
    communityAggregate.setCommentCount(communityAggregate.getCommentCount() + 1);
    communityAggregateRepository.save(communityAggregate);
  }
}
