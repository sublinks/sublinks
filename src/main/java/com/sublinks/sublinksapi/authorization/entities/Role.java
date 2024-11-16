package com.sublinks.sublinksapi.authorization.entities;

import com.sublinks.sublinksapi.person.entities.Person;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "acl_roles")
public class Role {

  @OneToMany(mappedBy = "role", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private Set<RolePermissions> rolePermissions;

  @ManyToOne(targetEntity = Role.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "inherits_from", referencedColumnName = "id", nullable = true)
  private Role inheritsFrom;

  @OneToMany(mappedBy = "inheritsFrom", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Set<Role> inheritedRoles;

  @OneToMany(mappedBy = "role")
  private Set<Person> persons;


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, name = "name")
  private String name;

  @Column(nullable = false, name = "description")
  private String description;

  @Column(nullable = false, name = "is_active")
  private boolean isActive;

  @Column(nullable = true, name = "expires_at")
  private Date expiresAt;

  @CreationTimestamp(source = SourceType.DB)
  @Column(updatable = false, nullable = false, name = "created_at")
  private Date createdAt;

  @UpdateTimestamp(source = SourceType.DB)
  @Column(updatable = false, name = "updated_at")
  private Date updatedAt;
}
