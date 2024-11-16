package com.sublinks.sublinksapi.instance.entities;

import com.sublinks.sublinksapi.api.lemmy.v3.enums.ListingType;
import com.sublinks.sublinksapi.api.lemmy.v3.enums.RegistrationMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Date;
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
@Table(name = "instance_configs")
public class InstanceConfig {

  /**
   * Relationships.
   */
  @OneToOne
  @JoinColumn(name = "instance_id")
  private Instance instance;

  /**
   * Attributes.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "registration_mode")
  @Enumerated(EnumType.STRING)
  private RegistrationMode registrationMode;

  @Column(name = "registration_question")
  private String registrationQuestion;

  @Column(name = "private_instance")
  private boolean privateInstance;

  @Column(name = "require_email_verification")
  private boolean requireEmailVerification;

  @Column(name = "enable_downvotes")
  private boolean enableDownvotes;

  @Column(name = "enable_nsfw")
  private boolean enableNsfw;

  @Column(name = "community_creation_admin_only")
  private boolean communityCreationAdminOnly;

  @Column(name = "application_email_admins")
  private boolean applicationEmailAdmins;

  @Column(name = "report_email_admins")
  private boolean reportEmailAdmins;

  @Column(name = "hide_modlog_mod_names")
  private boolean hideModlogModNames;

  @Column(name = "federation_enabled")
  private boolean federationEnabled;

  @Column(name = "captcha_enabled")
  private boolean captchaEnabled;

  @Column(name = "captcha_difficulty")
  private String captchaDifficulty;

  @Column(name = "actor_name_max_length")
  private int actorNameMaxLength;

  @Column(name = "default_theme")
  private String defaultTheme;

  @Column(name = "default_post_listing_type")
  @Enumerated(EnumType.STRING)
  private ListingType defaultPostListingType;

  @Column(name = "legal_information")
  private String legalInformation;

  @CreationTimestamp(source = SourceType.DB)
  @Column(updatable = false, nullable = false, name = "created_at")
  private Date createdAt;

  @UpdateTimestamp(source = SourceType.DB)
  @Column(updatable = false, name = "updated_at")
  private Date updatedAt;
}
