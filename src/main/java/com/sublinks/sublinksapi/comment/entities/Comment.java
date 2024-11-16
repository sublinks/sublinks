package com.sublinks.sublinksapi.comment.entities;

import com.sublinks.sublinksapi.authorization.AclEntityInterface;
import com.sublinks.sublinksapi.authorization.enums.AuthorizedEntityType;
import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.language.entities.Language;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.post.entities.Post;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.io.Serializable;
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
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;

/**
 * The Comment class represents a comment on a post in a community or instance. It contains various
 * attributes such as the comment body, timestamps, and deleted state. It also has relationships
 * with other entities such as the parent post, the person who made the comment, the community,
 * likes, and comment aggregate.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "comments")
public class Comment implements Serializable, AclEntityInterface {

  /**
   * Relationships.
   */
  @ManyToOne
  @JoinColumn(name = "post_id")
  private Post post;

  @ManyToOne
  @JoinColumn(name = "person_id")
  private Person person;

  @ManyToOne
  @JoinColumn(name = "community_id")
  private Community community;

  @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL)
  private List<CommentLike> likes;

  @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<LinkPersonComment> linkPersonComment;

  @ManyToOne
  @JoinColumn(name = "language_id")
  private Language language;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @PrimaryKeyJoinColumn
  private CommentAggregate commentAggregate;

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

  @Column(nullable = false, name = "is_featured")
  private boolean isFeatured;

  @Column(nullable = false, name = "comment_body")
  private String commentBody;

  @Column(nullable = false)
  private String path;

  @Column(nullable = true, updatable = false, insertable = false, name = "search_vector")
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
    Comment comment = (Comment) o;
    return getId() != null && Objects.equals(getId(), comment.getId());
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

    return AuthorizedEntityType.comment;
  }
}
