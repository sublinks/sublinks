package com.sublinks.sublinksapi.post.services;

import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.person.entities.LinkPersonPost;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.enums.LinkPersonPostType;
import com.sublinks.sublinksapi.person.services.LinkPersonPostService;
import com.sublinks.sublinksapi.post.entities.Post;
import com.sublinks.sublinksapi.post.entities.PostAggregate;
import com.sublinks.sublinksapi.post.events.PostCreatedPublisher;
import com.sublinks.sublinksapi.post.events.PostDeletedPublisher;
import com.sublinks.sublinksapi.post.events.PostUpdatedPublisher;
import com.sublinks.sublinksapi.post.repositories.PostRepository;
import com.sublinks.sublinksapi.shared.RemovedState;
import com.sublinks.sublinksapi.utils.KeyGeneratorUtil;
import com.sublinks.sublinksapi.utils.KeyStore;
import com.sublinks.sublinksapi.utils.UrlUtil;
import jakarta.persistence.EntityManager;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final PostCreatedPublisher postCreatedPublisher;
  private final KeyGeneratorUtil keyGeneratorUtil;
  private final LinkPersonPostService linkPersonPostService;
  private final PostDeletedPublisher postDeletedPublisher;
  private final PostLikeService postLikeService;
  private final PostUpdatedPublisher postUpdatedPublisher;
  private final UrlUtil urlUtil;
  private final EntityManager em;

  public String getPostMd5Hash(final Post post) {

    return getStringMd5Hash(post.getLinkUrl());
  }

  public String getStringMd5Hash(final String post) {

    if (post == null || post.isEmpty()) {
      return null;
    }

    try {
      final byte[] bytesOfLink = urlUtil.normalizeUrl(post)
          .getBytes(StandardCharsets.UTF_8);
      final MessageDigest md = MessageDigest.getInstance("MD5");
      final byte[] bytesOfMD5Link = md.digest(bytesOfLink);
      return new BigInteger(1, bytesOfMD5Link).toString(16);
    } catch (Exception ignored) {
      return null;
    }
  }

  public Person getPostCreator(final Post post) {

    if (post.getLinkPersonPost() == null || post.getLinkPersonPost()
        .isEmpty()) {
      return null;
    }
    for (LinkPersonPost linkPersonPost : post.getLinkPersonPost()) {
      if (linkPersonPost.getLinkType() == LinkPersonPostType.creator) {
        return linkPersonPost.getPerson();
      }
    }
    return null;
  }

  @Transactional
  public void updatePost(final Post post) {

    postRepository.save(post);
    postUpdatedPublisher.publish(post);
  }

  @Transactional
  public void createPost(final Post post, final Person creator) {

    final KeyStore keys = keyGeneratorUtil.generate();
    post.setPublicKey(keys.publicKey());
    post.setPrivateKey(keys.privateKey());

    post.setLocal(true);
    final PostAggregate postAggregate = PostAggregate.builder()
        .post(post)
        .community(post.getCommunity())
        .build();
    post.setPostAggregate(postAggregate);
    post.setActivityPubId("");
    postRepository.saveAndFlush(post); // @todo fix second save making post look edited right away
    post.setActivityPubId("%s/post/%d".formatted(post.getInstance()
        .getDomain(), post.getId()));
    postRepository.save(post);

    linkPersonPostService.createPostLink(post, creator, LinkPersonPostType.creator);
    em.refresh(post);
    postLikeService.updateOrCreatePostLikeLike(post, creator);

    postCreatedPublisher.publish(post);
  }

  public void softDeletePost(final Post post) {

    postRepository.save(post);
    postDeletedPublisher.publish(post);
  }

  public List<Post> deleteAllPostsByPerson(final Person person) {

    final List<Post> posts = postRepository.allPostsByPersonAndRemoved(person,
        List.of(RemovedState.NOT_REMOVED, RemovedState.REMOVED_BY_INSTANCE,
            RemovedState.REMOVED_BY_COMMUNITY, RemovedState.REMOVED));
    posts.forEach(post -> {
      post.setDeleted(true);
      post.setTitle("*Permanently deleted by creator*");
      post.setRemovedState(RemovedState.PURGED);
      post.setPostBody("");
      post.setLinkUrl("");
      postRepository.save(post);
    });
    return posts;
  }

  @Transactional
  public void removeAllPostsFromCommunityAndUser(final Community community, final Person person,
      final boolean removed)
  {

    postRepository.allPostsByCommunityAndPersonAndRemoved(community, person,
            List.of(removed ? RemovedState.NOT_REMOVED : RemovedState.REMOVED_BY_COMMUNITY))
        .forEach(post -> {
          post.setRemovedState(
              removed ? RemovedState.REMOVED_BY_COMMUNITY : RemovedState.NOT_REMOVED);
          postRepository.save(post);
        });
  }

  @Transactional
  public void removeAllPostsFromUser(final Person person, final boolean removed) {

    postRepository.allPostsByPersonAndRemoved(person,
            List.of(removed ? RemovedState.NOT_REMOVED : RemovedState.REMOVED_BY_INSTANCE))
        .forEach(post -> {
          post.setRemovedState(
              removed ? RemovedState.REMOVED_BY_INSTANCE : RemovedState.NOT_REMOVED);
          postRepository.save(post);
        });
  }
}
