package com.sublinks.sublinksapi.comment.services;

import com.sublinks.sublinksapi.comment.entities.Comment;
import com.sublinks.sublinksapi.comment.entities.CommentAggregate;
import com.sublinks.sublinksapi.comment.events.CommentCreatedPublisher;
import com.sublinks.sublinksapi.comment.events.CommentDeletedPublisher;
import com.sublinks.sublinksapi.comment.events.CommentUpdatedPublisher;
import com.sublinks.sublinksapi.comment.repositories.CommentAggregateRepository;
import com.sublinks.sublinksapi.comment.repositories.CommentRepository;
import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.instance.models.LocalInstanceContext;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.shared.RemovedState;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CommentService.class);
  private final CommentAggregateRepository commentAggregateRepository;
  private final CommentRepository commentRepository;
  private final CommentCreatedPublisher commentCreatedPublisher;
  private final CommentUpdatedPublisher commentUpdatedPublisher;
  private final CommentDeletedPublisher commentDeletedPublisher;
  private final LocalInstanceContext localInstanceContext;

  /**
   * Generates an ActivityPub ID for a given comment.
   *
   * @param comment The comment for which the ActivityPub ID is being generated.
   * @return A string representing the ActivityPub ID.
   */
  public String generateActivityPubId(
      final com.sublinks.sublinksapi.comment.entities.Comment comment)
  {

    String domain = localInstanceContext.instance()
        .getDomain();
    return String.format("%s/comment/%d", domain, comment.getId());
  }

  /**
   * Retrieves the parent comment of a given comment, if it exists.
   *
   * @param comment The comment whose parent is to be retrieved.
   * @return An Optional containing the parent Comment, or empty if no parent exists.
   */
  public Optional<Comment> getParentComment(final Comment comment) {

    if (comment.getPath() == null) {
      return Optional.empty();
    }

    String[] path = comment.getPath()
        .split("\\.");
    if (path.length == 1) {
      return Optional.empty();
    }

    return commentRepository.findById(Long.parseLong(path[path.length - 2]));

  }

  /**
   * Creates a new comment without a parent and publishes an event upon creation.
   *
   * @param comment The Comment object to be created.
   */
  @Transactional
  public void createComment(final Comment comment) {

    commentRepository.saveAndFlush(comment);
    if (comment.getPath() == null || comment.getPath()
        .isBlank()) {
      comment.setPath(String.format("0.%d", comment.getId()));
    }
    comment.setActivityPubId(generateActivityPubId(comment));
    commentRepository.save(comment);

    CommentAggregate commentAggregate = CommentAggregate.builder()
        .comment(comment)
        .build();
    commentAggregateRepository.save(commentAggregate);
    comment.setCommentAggregate(commentAggregate);
    commentRepository.save(comment);

    commentCreatedPublisher.publish(comment);
  }

  /**
   * Creates a new comment as a reply to a specified parent and publishes an event upon creation.
   *
   * @param comment The Comment object to be created, a reply to the parent.
   * @param parent  The parent Comment of the new comment.
   */
  @Transactional
  public void createComment(final Comment comment, final Comment parent) {

    commentRepository.saveAndFlush(comment);
    comment.setPath(String.format("%s.%d", parent.getPath(), comment.getId()));

    createComment(comment);
    final CommentAggregate commentAggregate = parent.getCommentAggregate();
    commentAggregate.setChildrenCount(commentAggregate.getChildrenCount() + 1);
    commentAggregateRepository.save(commentAggregate);

    parent.setCommentAggregate(commentAggregate);
    commentRepository.save(parent);
  }

  /**
   * Deletes a comment and publishes an event.
   *
   * @param comment The Comment object to be deleted.
   */
  public Comment deleteComment(final Comment comment) {

    String DELETED_REPLACEMENT_TEXT = "*Permanently Deleted*";

    Comment foundComment = commentRepository.findById(comment.getId())
        .orElseThrow(() -> {
          logger.error("no comment found by id: {}", comment.getId());
          return new EntityNotFoundException("could not find comment by Id");
        });
    foundComment.setCommentBody(DELETED_REPLACEMENT_TEXT);
    foundComment.setRemovedState(RemovedState.PURGED);

    final Comment updatedComment = commentRepository.save(foundComment);
    commentDeletedPublisher.publish(updatedComment);
    return updatedComment;
  }

  public Comment softDeleteComment(final Comment comment) {

    Comment foundComment = commentRepository.findById(comment.getId())
        .orElseThrow(() -> {
          logger.error("no comment found by id: {}", comment.getId());
          return new EntityNotFoundException("could not find comment by Id");
        });

    final Comment updatedComment = commentRepository.save(foundComment);
    commentDeletedPublisher.publish(updatedComment);
    return updatedComment;
  }

  public List<Comment> deleteAllCommentsByPerson(final Person person) {

    List<Comment> comments = commentRepository.findByPerson(person);

    comments.forEach(comment -> {
      comment.setCommentBody("*Permanently deleted by creator*");
      comment.setRemovedState(RemovedState.PURGED);
      commentRepository.save(comment);
      this.softDeleteComment(comment);
    });

    return comments;
  }

  /**
   * Updates a comment without publishing any event.
   *
   * @param comment The Comment object to be updated.
   */
  @Transactional
  public void updateCommentQuietly(final Comment comment) {

    commentRepository.save(comment);
  }

  /**
   * Updates a comment and publishes an event upon update.
   *
   * @param comment The Comment object to be updated.
   */
  @Transactional
  public void updateComment(final Comment comment) {

    commentRepository.save(comment);
    commentUpdatedPublisher.publish(comment);
  }

  /**
   * Removes all comments made by a specific user in a specific community.
   *
   * @param community The community from which comments are to be removed.
   * @param person    The user whose comments are to be removed.
   * @param removed   Boolean flag indicating whether the comments should be marked as removed.
   */
  @Transactional
  public void removeAllCommentsFromCommunityAndUser(final Community community, final Person person,
      final boolean removed)
  {

    commentRepository.allCommentsByCommunityAndPersonAndRemoved(community, person,
            List.of(removed ? RemovedState.NOT_REMOVED : RemovedState.REMOVED_BY_COMMUNITY))
        .forEach(comment -> {
          comment.setRemovedState(
              removed ? RemovedState.REMOVED_BY_COMMUNITY : RemovedState.NOT_REMOVED);
          commentRepository.save(comment);
        });
  }

  /**
   * Removes all comments made by a specific user.
   *
   * @param person  The user whose comments are to be removed.
   * @param removed Boolean flag indicating whether the comments should be marked as removed.
   */
  @Transactional
  public void removeAllCommentsFromUser(final Person person, final boolean removed) {

    commentRepository.allCommentsByPersonAndRemoved(person,
            List.of(removed ? RemovedState.NOT_REMOVED : RemovedState.REMOVED_BY_INSTANCE))
        .forEach(comment -> {
          comment.setRemovedState(
              removed ? RemovedState.REMOVED_BY_INSTANCE : RemovedState.NOT_REMOVED);
          commentRepository.save(comment);
        });
  }

}
