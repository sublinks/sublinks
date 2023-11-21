package com.sublinks.sublinksapi.community.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "community_aggregates")
public class CommunityAggregate {

  /**
   * Relationships.
   */
  @OneToOne
  @JoinColumn(name = "community_id")
  private Community community;

  /**
   * Attributes.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, name = "subscriber_count")
  private int subscriberCount;

  @Column(nullable = false, name = "post_count")
  private int postCount;

  @Column(nullable = false, name = "comment_count")
  private int commentCount;

  @Column(nullable = false, name = "active_daily_user_count")
  private int activeDailyUserCount;

  @Column(nullable = false, name = "active_weekly_user_count")
  private int activeWeeklyUserCount;

  @Column(nullable = false, name = "active_monthly_user_count")
  private int activeMonthlyUserCount;

  @Column(nullable = false, name = "active_half_year_user_count")
  private int activeHalfYearUserCount;

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
    CommunityAggregate that = (CommunityAggregate) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {

    return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
        .getPersistentClass().hashCode() : getClass().hashCode();
  }
}
