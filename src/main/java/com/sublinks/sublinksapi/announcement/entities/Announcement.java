package com.sublinks.sublinksapi.announcement.entities;

import com.sublinks.sublinksapi.person.entities.Person;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
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
 * Announcement class represents an announcement entity.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "announcements")
public class Announcement {

  /**
   * Relationships.
   */
  @ManyToOne
  @JoinColumn(name = "creator_id")
  private Person creator;

  /**
   * Attributes.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "content")
  private String content;

  @Column(name = "is_active")
  private Boolean active;

  @Column(name = "local_site_id")
  private Long localSiteId;

  @CreationTimestamp(source = SourceType.DB)
  @Column(name = "created_at")
  private String createdAt;

  @UpdateTimestamp(source = SourceType.DB)
  @Column(name = "updated_at")
  private String updatedAt;

  /**
   * @param o Object to compare to
   * @return boolean
   */
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
    Announcement that = (Announcement) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  /**
   * Object hashCode.
   *
   * @return int
   */
  @Override
  public final int hashCode() {

    return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
        .getPersistentClass()
        .hashCode() : getClass().hashCode();
  }
}
