package com.sublinks.sublinksapi.post.entities;

import com.sublinks.sublinksapi.authorization.AclEntityInterface;
import com.sublinks.sublinksapi.authorization.enums.AuthorizedEntityType;
import com.sublinks.sublinksapi.comment.entities.Comment;
import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.instance.entities.Instance;
import com.sublinks.sublinksapi.language.entities.Language;
import com.sublinks.sublinksapi.person.entities.LinkPersonPost;
import com.sublinks.sublinksapi.shared.RemovedState;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;

/**
 * The Post class represents a post in a community or instance. It contains various attributes such
 * as the post body, title, link, and timestamps. It also has relationships with other entities such
 * as comments, likes, and history.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "posts")
public class Post implements AclEntityInterface {

  /**
   * Relationships.
   */
  @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  Set<LinkPersonPost> linkPersonPost;

  @ManyToOne
  @JoinTable(name = "post_post_cross_post",
      joinColumns = @JoinColumn(name = "post_id"),
      inverseJoinColumns = @JoinColumn(name = "cross_post_id"))
  CrossPost crossPost;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "community_id")
  private Community community;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "post", fetch = FetchType.EAGER)
  private List<Comment> comments;

  @ManyToOne
  private Instance instance;

  @ManyToOne
  @JoinColumn(name = "language_id")
  private Language language;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "post")
  private PostAggregate postAggregate;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<PostLike> postLikes;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<PostHistory> postHistory;

  /**
   * Attributes.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, name = "activity_pub_id")
  private String activityPubId;

  @Column(nullable = false, name = "is_deleted")
  private boolean isDeleted;

  @Column(nullable = false, name = "removed_state")
  @Enumerated(EnumType.STRING)
  private RemovedState removedState;

  @Column(nullable = false, name = "is_local")
  private boolean isLocal;

  @Column(nullable = false, name = "is_locked")
  private boolean isLocked;

  @Column(nullable = false, name = "is_featured")
  private boolean isFeatured;

  @Column(nullable = false, name = "is_featured_in_community")
  private boolean isFeaturedInCommunity;

  @Column(nullable = false, name = "link_url")
  private String linkUrl;

  @Column(nullable = false, name = "link_title")
  private String linkTitle;

  @Column(nullable = false, name = "link_description")
  private String linkDescription;

  @Column(nullable = false, name = "link_thumbnail_url")
  private String linkThumbnailUrl;

  @Column(nullable = false, name = "link_video_url")
  private String linkVideoUrl;

  @Column(nullable = false, name = "is_nsfw")
  private boolean isNsfw;

  private String title;

  @Column(nullable = false, name = "title_slug")
  private String titleSlug;

  @Column(nullable = false, name = "post_body")
  private String postBody;

  @Column(nullable = false, name = "public_key")
  private String publicKey;

  @Column(nullable = true, name = "private_key")
  private String privateKey;

  @Column(nullable = false, updatable = false, insertable = false, name = "search_vector")
  private String searchVector;

  @CreationTimestamp(source = SourceType.DB)
  @Column(updatable = false, nullable = false, name = "created_at")
  private Date createdAt;


  @UpdateTimestamp(source = SourceType.DB)
  @Column(updatable = false, name = "updated_at")
  private Date updatedAt;

  @Override
  public final boolean equals(Object o) {

    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    Class<?> objectEffectiveClass =
        o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer()
            .getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass =
        this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
            .getPersistentClass() : this.getClass();
    if (thisEffectiveClass != objectEffectiveClass) {
      return false;
    }
    Post post = (Post) o;
    return getId() != null && Objects.equals(getId(), post.getId());
  }

  @Override
  public final int hashCode() {

    return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
        .getPersistentClass()
        .hashCode() : getClass().hashCode();
  }

  public boolean isRemoved() {

    return this.removedState != RemovedState.NOT_REMOVED;
  }

  @Override
  public AuthorizedEntityType entityType() {

    return AuthorizedEntityType.post;
  }

  @Override
  public Post clone() {

    try {
      Post clone = (Post) super.clone();
      // TODO: copy mutable state here, so the clone can't change the internals of the original
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}
