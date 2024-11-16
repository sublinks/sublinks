package com.sublinks.sublinksapi.language.entities;

import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.instance.entities.Instance;
import com.sublinks.sublinksapi.person.entities.Person;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "languages")
public class Language {

  /**
   * Relationships.
   */
  @ManyToMany(mappedBy = "languages", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  private List<Person> people;

  @ManyToMany(mappedBy = "languages", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  private List<Instance> instances;

  @ManyToMany(mappedBy = "languages", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  private List<Community> communities;

  /**
   * Attributes.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String code;

  @Column(unique = true)
  private String name;

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
    Language language = (Language) o;
    return getId() != null && Objects.equals(getId(), language.getId());
  }

  @Override
  public final int hashCode() {

    return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
        .getPersistentClass()
        .hashCode() : getClass().hashCode();
  }
}
