/*
 * Triggers Functions
 */

CREATE OR REPLACE FUNCTION fn_sublinks_updated_at()
  RETURNS TRIGGER AS
$$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP(3);
  RETURN NEW;
END;
$$ language 'plpgsql';

CREATE OR REPLACE FUNCTION fn_search_vector_is_same(search_vector TSVECTOR, text TEXT)
  RETURNS BOOLEAN AS
$$
BEGIN
  RETURN search_vector @@ to_tsquery('english', text);
END;
$$ language 'plpgsql';

/**
 * Comments table
 */
CREATE TABLE comments
(
  id              BIGSERIAL PRIMARY KEY,
  activity_pub_id TEXT         NOT NULL,
  language_id     BIGINT       NOT NULL,
  is_deleted      BOOL         NOT NULL DEFAULT false,
  removed_state   VARCHAR(255) NULL     DEFAULT 'NOT_REMOVED',
  is_local        BOOL         NOT NULL DEFAULT false,
  person_id       BIGINT       NOT NULL,
  community_id    BIGINT       NOT NULL,
  post_id         BIGINT       NOT NULL,
  is_featured     BOOL         NOT NULL DEFAULT false,
  comment_body    TEXT         NULL,
  search_vector   TSVECTOR GENERATED ALWAYS AS (to_tsvector('english', comment_body)) STORED,
  path            VARCHAR(512),
  created_at      TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at      TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);


CREATE INDEX IDX_COMMENTS_LANGUAGE_ID ON comments (language_id);
CREATE INDEX IDX_COMMENTS_PERSON_ID ON comments (person_id);
CREATE INDEX IDX_COMMENTS_COMMUNITY_ID ON comments (community_id);
CREATE INDEX IDX_COMMENTS_POST_ID ON comments (post_id);

CREATE INDEX IDX_COMMENTS_SEARCH_VECTOR ON comments USING GIN (search_vector);


/**
 Comment Likes table
 */
CREATE TABLE comment_likes
(
  id           BIGSERIAL PRIMARY KEY,
  post_id      BIGINT   NOT NULL,
  person_id    BIGINT   NOT NULL,
  comment_id   BIGINT   NOT NULL,
  is_up_vote   BOOL     NULL DEFAULT false,
  is_down_vote BOOL     NULL DEFAULT false,
  score        SMALLINT NULL DEFAULT 0,
  created_at   TIMESTAMP(3)  DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at   TIMESTAMP(3)  DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE INDEX IDX_COMMENT_ID ON comment_likes (comment_id);
CREATE INDEX IDX_PERSON_ID ON comment_likes (person_id);

/**
 Communities table
 */
CREATE TABLE communities
(
  id                            BIGSERIAL PRIMARY KEY,
  activity_pub_id               TEXT         NOT NULL,
  instance_id                   BIGINT       NOT NULL,
  title                         VARCHAR(255) NULL,
  title_slug                    VARCHAR(255) NOT NULL,
  description                   TEXT         NULL,
  is_deleted                    BOOL         NULL DEFAULT false,
  is_removed                    BOOL         NULL DEFAULT false,
  is_local                      BOOL         NULL DEFAULT false,
  is_nsfw                       BOOL         NULL DEFAULT false,
  is_posting_restricted_to_mods BOOL         NULL DEFAULT false,
  icon_image_url                TEXT         NULL,
  banner_image_url              TEXT         NULL,
  public_key                    TEXT         NOT NULL,
  private_key                   TEXT         NULL,
  search_vector                 TSVECTOR GENERATED ALWAYS AS (to_tsvector('english',
                                                                          coalesce(title, '') ||
                                                                          ' ' ||
                                                                          coalesce(title_slug, '') ||
                                                                          ' ' ||
                                                                          coalesce(description, ''))) STORED,
  created_at                    TIMESTAMP(3)      DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at                    TIMESTAMP(3)      DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE INDEX IDX_COMMUNITIES_INSTANCE_ID ON communities (instance_id);
CREATE INDEX IDX_COMMUNITIES_TITLE_SLUG ON communities (title_slug);
CREATE INDEX IDX_COMMUNITIES_IS_DELETED ON communities (is_deleted);
CREATE INDEX IDX_COMMUNITIES_IS_REMOVED ON communities (is_removed);
CREATE INDEX IDX_COMMUNITIES_IS_LOCAL ON communities (is_local);
CREATE INDEX IDX_COMMUNITIES_IS_NSFW ON communities (is_nsfw);
CREATE INDEX IDX_COMMUNITIES_IS_POSTING_RESTRICTED_TO_MODS ON communities (is_posting_restricted_to_mods);

CREATE INDEX IDX_COMMUNITIES_SEARCH_VECTOR ON communities USING GIN (search_vector);

/**
 Community Languages table
 */
CREATE TABLE community_languages
(
  community_id BIGINT NOT NULL,
  language_id  BIGINT NOT NULL,
  PRIMARY KEY (community_id, language_id)
);

/**
 Instances table
 */
CREATE TABLE instances
(
  id              BIGSERIAL PRIMARY KEY,
  activity_pub_id TEXT                                      NOT NULL,
  domain          VARCHAR(255)                              NOT NULL,
  software        VARCHAR(255)                              NULL,
  version         VARCHAR(255)                              NULL,
  name            VARCHAR(255)                              NULL,
  description     TEXT                                      NULL,
  sidebar         TEXT                                      NULL,
  search_vector   TSVECTOR GENERATED ALWAYS AS (to_tsvector('english',
                                                            coalesce(name, '') ||
                                                            ' ' ||
                                                            coalesce(description, ''))) STORED,
  icon_url        TEXT                                      NULL,
  banner_url      TEXT                                      NULL,
  public_key      TEXT                                      NOT NULL,
  private_key     TEXT                                      NULL,
  created_at      TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at      TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

/**
 Person Instance table
 */
CREATE TABLE link_person_instances
(
  id          BIGSERIAL PRIMARY KEY,
  person_id   BIGINT                                    NOT NULL,
  instance_id BIGINT                                    NOT NULL,
  created_at  TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE UNIQUE INDEX IDX_LINK_PERSON_INSTANCE_PERSON_ID_INSTANCE_ID ON link_person_instances (person_id, instance_id);

/**
 Instance Languages table
 */
CREATE TABLE instance_languages
(
  instance_id BIGINT NOT NULL,
  language_id BIGINT NOT NULL,
  PRIMARY KEY (instance_id, language_id)
);

/**
 Blocked Instances table
 */
CREATE TABLE instance_blocks
(
  id          BIGSERIAL PRIMARY KEY,
  instance_id BIGINT                                    NOT NULL,
  created_at  TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

/**
 Languages table
 */
CREATE TABLE languages
(
  id   BIGSERIAL PRIMARY KEY,
  code CHAR(3)      NOT NULL,
  name VARCHAR(255) NOT NULL
);

/**
 People table
 */
CREATE TABLE people
(
  id                             BIGSERIAL PRIMARY KEY,
  is_local                       BOOL         NULL     DEFAULT false,
  is_bot_account                 BOOL         NULL     DEFAULT false,
  is_deleted                     BOOL         NULL     DEFAULT false,
  activity_pub_id                TEXT         NOT NULL,
  actor_id                       TEXT         NOT NULL,
  role_id                        BIGINT       NOT NULL,
  name                           VARCHAR(255) NULL,
  search_vector                  TSVECTOR GENERATED ALWAYS AS (to_tsvector('english',
                                                                           coalesce(name, '') ||
                                                                           ' ' ||
                                                                           coalesce(display_name, '') ||
                                                                           ' ' ||
                                                                           coalesce(biography, ''))) STORED,
  display_name                   VARCHAR(255) NULL,
  email                          VARCHAR(255) NULL,
  is_email_verified              BOOL         NULL     DEFAULT false,
  password                       VARCHAR(255) NOT NULL,
  avatar_image_url               TEXT         NULL,
  banner_image_url               TEXT         NULL,
  biography                      TEXT         NULL,
  interface_language             VARCHAR(20)  NULL,
  default_theme                  VARCHAR(255) NULL,
  default_listing_type           VARCHAR(255) NULL     DEFAULT 'List',
  default_sort_type              VARCHAR(255) NULL     DEFAULT 'Active',
  is_show_scores                 BOOL         NOT NULL DEFAULT false,
  is_show_read_posts             BOOL         NOT NULL DEFAULT false,
  is_show_nsfw                   BOOL         NOT NULL DEFAULT false,
  is_show_bot_accounts           BOOL         NOT NULL DEFAULT false,
  is_show_avatars                BOOL         NOT NULL DEFAULT false,
  is_send_notifications_to_email BOOL         NOT NULL DEFAULT false,
  is_open_links_in_new_tab       BOOL         NOT NULL DEFAULT false,
  is_infinite_scroll             BOOL         NOT NULL DEFAULT false,
  is_keyboard_navigation         BOOL         NOT NULL DEFAULT false,
  is_animated_images             BOOL         NOT NULL DEFAULT false,
  is_collapse_bot_comments       BOOL         NOT NULL DEFAULT false,
  is_auto_expanding              BOOL         NOT NULL DEFAULT false,
  is_blur_nsfw                   BOOL         NOT NULL DEFAULT false,
  post_listing_type              VARCHAR(255) NULL     DEFAULT 'List',
  matrix_user_id                 TEXT         NULL,
  public_key                     TEXT         NOT NULL,
  private_key                    TEXT         NULL,
  totp_secret                    TEXT         NULL,
  totp_verified_secret           TEXT         NULL,
  role_expire_at                 TIMESTAMP(3) NULL,
  created_at                     TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at                     TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE INDEX IDX_PEOPLE_NAME ON people (name);
CREATE INDEX IDX_PEOPLE_EMAIL ON people (email);
CREATE INDEX IDX_PEOPLE_IS_LOCAL ON people (is_local);
CREATE INDEX IDX_PEOPLE_ROLE_ID ON people (role_id);

CREATE INDEX IDX_PEOPLE_SEARCH_VECTOR ON people USING GIN (search_vector);

CREATE TABLE person_languages
(
  person_id   BIGINT NOT NULL,
  language_id BIGINT NOT NULL,
  PRIMARY KEY (person_id, language_id)
);

/**
 Link Person Communities table
 */
CREATE TABLE link_person_communities
(
  id           BIGSERIAL PRIMARY KEY,
  person_id    BIGINT                                    NOT NULL,
  community_id BIGINT                                    NOT NULL,
  link_type    TEXT                                      NOT NULL,
  expire_at    TIMESTAMP(3)                              NULL,
  created_at   TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE INDEX IDX_LINK_PERSON_COMMUNITIES_PERSON_ID_COMMUNITY_ID ON link_person_communities (person_id, community_id);
CREATE UNIQUE INDEX IDX_LINK_PERSON_COMMUNITIES_PERSON_ID_COMMUNITY_ID_LINK_TYPE ON link_person_communities (person_id, community_id, link_type);

/**
 Posts table
 */
CREATE TABLE posts
(
  id                       BIGSERIAL PRIMARY KEY,
  instance_id              BIGINT       NOT NULL,
  activity_pub_id          TEXT         NOT NULL,
  language_id              BIGINT       NOT NULL,
  is_deleted               BOOL         NOT NULL DEFAULT false,
  removed_state            VARCHAR(255) NULL     DEFAULT 'NOT_REMOVED',
  is_local                 BOOL         NOT NULL DEFAULT false,
  is_locked                BOOL         NOT NULL DEFAULT false,
  community_id             BIGINT       NOT NULL,
  is_featured              BOOL         NOT NULL DEFAULT false,
  is_featured_in_community BOOL         NOT NULL DEFAULT false,
  link_url                 TEXT         NULL,
  link_title               VARCHAR(255) NULL,
  link_description         TEXT         NULL,
  link_thumbnail_url       TEXT         NULL,
  link_video_url           TEXT         NULL,
  is_nsfw                  BOOL         NULL     DEFAULT false,
  title                    VARCHAR(255) NOT NULL,

  title_slug               VARCHAR(255) NOT NULL,
  post_body                TEXT         NULL,
  public_key               TEXT         NOT NULL,
  private_key              TEXT         NULL,
  search_vector            TSVECTOR GENERATED ALWAYS AS (to_tsvector('english',
                                                                     coalesce(title, '') ||
                                                                     ' ' ||
                                                                     coalesce(title_slug, '') ||
                                                                     ' ' ||
                                                                     coalesce(post_body, ''))) STORED,
  created_at               TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at               TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,

  CONSTRAINT UC_TITLE_SLUG UNIQUE (title_slug)
);

CREATE INDEX IDX_POSTS_LANGUAGE_ID ON posts (language_id);
CREATE INDEX IDX_POSTS_COMMUNITY_ID ON posts (community_id);
CREATE INDEX IDX_POSTS_IS_NSFW ON posts (is_nsfw);
CREATE INDEX IDX_POSTS_IS_FEATURED ON posts (is_featured);
CREATE INDEX IDX_POSTS_IS_FEATURED_IN_COMMUNITY ON posts (is_featured_in_community);
CREATE INDEX IDX_POSTS_TITLE_SLUG ON posts (title_slug);

CREATE INDEX IDX_POSTS_SEARCH_VECTOR ON posts USING GIN (search_vector);

/**
 Post Likes table
 */
CREATE TABLE post_likes
(
  id           BIGSERIAL PRIMARY KEY,
  post_id      BIGINT   NOT NULL,
  person_id    BIGINT   NOT NULL,
  is_up_vote   BOOL     NOT NULL DEFAULT false,
  is_down_vote BOOL     NOT NULL DEFAULT false,
  score        SMALLINT NOT NULL DEFAULT 0,
  created_at   TIMESTAMP(3)      DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at   TIMESTAMP(3)      DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE INDEX IDX_POST_LIKES_POST_ID ON post_likes (post_id);


/**
 Person Posts table
 */
CREATE TABLE link_person_posts
(
  id         BIGSERIAL PRIMARY KEY,
  person_id  BIGINT                                    NOT NULL,
  post_id    BIGINT                                    NOT NULL,
  link_type  VARCHAR(255)                              NOT NULL,
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE INDEX IDX_LINK_PERSON_POSTS_PERSON_ID_POST_ID ON link_person_posts (person_id, post_id);
CREATE UNIQUE INDEX IDX_LINK_PERSON_POSTS_PERSON_ID_POST_ID_LINK_TYPE ON link_person_posts (person_id, post_id, link_type);

/**
 Person Access Control List table
 */
CREATE TABLE acl
(
  id                BIGSERIAL PRIMARY KEY,
  person_id         BIGINT                                    NOT NULL,
  entity_type       VARCHAR(255)                              NOT NULL,
  entity_id         BIGINT                                    NULL,
  authorized_action VARCHAR(255)                              NOT NULL,
  is_permitted      BOOL                                      NULL DEFAULT false,
  created_at        TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at        TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE INDEX IDX_ACL_PERSON_ID_ENTITY_TYPE_ENTITY_ID ON acl (person_id, entity_type, entity_id);
CREATE UNIQUE INDEX IDX_ACL_PERSON_ID_ENTITY_TYPE_ENTITY_ID_AUTHORIZED_ACTION ON acl (person_id,
                                                                                      entity_type,
                                                                                      entity_id,
                                                                                      authorized_action);

/**
 Post Read table
 */
CREATE TABLE post_reads
(
  id         BIGSERIAL PRIMARY KEY,
  post_id    BIGINT                                    NOT NULL,
  person_id  BIGINT                                    NOT NULL,
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE UNIQUE INDEX IDX_POST_READ_POST_ID_PERSON_ID ON post_reads (post_id, person_id);

/**
 Comment Read table
 */
CREATE TABLE comment_reads
(
  id         BIGSERIAL PRIMARY KEY,
  comment_id BIGINT                                    NOT NULL,
  person_id  BIGINT                                    NOT NULL,
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE UNIQUE INDEX IDX_COMMENT_READ_COMMENT_ID_PERSON_ID ON comment_reads (comment_id, person_id);

/**
 Moderation Logs table
 */
CREATE TABLE moderation_logs
(
  id                   BIGSERIAL PRIMARY KEY,
  instance_id          BIGINT                                    NOT NULL,
  action_type          VARCHAR(255)                              NOT NULL,
  reason               VARCHAR(255)                              NULL,
  entity_id            BIGINT                                    NOT NULL,
  admin_person_id      BIGINT                                    NULL,
  post_id              BIGINT                                    NULL,
  comment_id           BIGINT                                    NULL,
  community_id         BIGINT                                    NULL,
  moderation_person_id BIGINT                                    NULL,
  other_person_id      BIGINT                                    NULL,
  removed              BOOL                                      NULL,
  hidden               BOOL                                      NULL,
  locked               BOOL                                      NULL,
  banned               BOOL                                      NULL,
  featured             BOOL                                      NULL,
  featured_community   BOOL                                      NULL,
  expires              TIMESTAMP(3)                              NULL,
  created_at           TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE INDEX IDX_MODERATION_LOGS_ACTION_TYPE ON moderation_logs (action_type);

/**
 Post Post Cross Post table
 */
CREATE TABLE post_post_cross_post
(
  post_id       BIGINT NOT NULL,
  cross_post_id BIGINT NOT NULL,
  PRIMARY KEY (post_id, cross_post_id)
);

/**
 Post Cross Posts table
 */
CREATE TABLE post_cross_posts
(
  id       BIGSERIAL PRIMARY KEY,
  md5_hash CHAR(32) NOT NULL
);

CREATE INDEX IDX_POST_CROSS_POST_MD5_HASH ON post_cross_posts (md5_hash);

/**

 Post Mention table
 */
CREATE TABLE person_mentions
(
  id           BIGSERIAL PRIMARY KEY,
  recipient_id BIGINT                                    NOT NULL,
  comment_id   BIGINT                                    NOT NULL,
  is_read      BOOL         DEFAULT false                NOT NULL,
  created_at   TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE UNIQUE INDEX IDX_PEOPLE_MENTIONS_RECIPIENT_ID ON person_mentions (recipient_id);

/**
 Private Message
 */
CREATE TABLE private_messages
(
  id              BIGSERIAL PRIMARY KEY,
  sender_id       BIGINT                                    NOT NULL,
  recipient_id    BIGINT                                    NOT NULL,
  activity_pub_id TEXT                                      NOT NULL,
  content         TEXT                                      NOT NULL,
  is_local        BOOL         DEFAULT false                NOT NULL,
  is_deleted      BOOL         DEFAULT false                NOT NULL,
  is_read         BOOL         DEFAULT false                NOT NULL,
  created_at      TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at      TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE INDEX IDX_PRIVATE_MESSAGES_SENDER_ID ON private_messages (sender_id);
CREATE INDEX IDX_PRIVATE_MESSAGES_RECIPIENT_ID ON private_messages (recipient_id);

/**
 Private Message Report
 */
CREATE TABLE private_messages_reports
(
  id                 BIGSERIAL PRIMARY KEY,
  creator_id         BIGINT                                    NOT NULL,
  resolver_id        BIGINT                                    NULL,
  private_message_id BIGINT                                    NOT NULL,
  original_content   TEXT                                      NOT NULL,
  reason             TEXT                                      NOT NULL,
  resolved           BOOL         DEFAULT false                NOT NULL,
  created_at         TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at         TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE INDEX IDX_PRIVATE_MESSAGES_REPORT_CREATOR_ID ON private_messages_reports (creator_id);
CREATE INDEX IDX_PRIVATE_MESSAGES_REPORT_PRIVATE_MESSAGE_ID ON private_messages_reports (private_message_id);

/**
 Comment Report
 */
CREATE TABLE comment_reports
(
  id               BIGSERIAL PRIMARY KEY,
  creator_id       BIGINT                                    NOT NULL,
  resolver_id      BIGINT                                    NULL,
  comment_id       BIGINT                                    NOT NULL,
  original_content TEXT                                      NOT NULL,
  reason           TEXT                                      NOT NULL,
  resolved         BOOL         DEFAULT false                NOT NULL,
  created_at       TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at       TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE INDEX IDX_COMMENT_REPORTS_CREATOR_ID ON comment_reports (creator_id);

/**
 Comment Reply
 */
CREATE TABLE comment_replies
(
  id           BIGSERIAL PRIMARY KEY,
  recipient_id BIGINT                                    NOT NULL,
  comment_id   BIGINT                                    NOT NULL,
  is_read      BOOL         DEFAULT false                NOT NULL,
  created_at   TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at   TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);


CREATE INDEX IDX_COMMENT_REPLIES_RECIPIENT_ID ON comment_replies (recipient_id);

/**
    Post Report
    */
CREATE TABLE post_reports
(
  id             BIGSERIAL PRIMARY KEY,
  creator_id     BIGINT                                    NOT NULL,
  resolver_id    BIGINT                                    NULL,
  post_id        BIGINT                                    NOT NULL,
  original_title TEXT                                      NOT NULL,
  original_body  TEXT                                      NOT NULL,
  original_url   TEXT                                      NOT NULL,
  reason         TEXT                                      NOT NULL,
  resolved       BOOL         DEFAULT false                NOT NULL,
  created_at     TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at     TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE INDEX IDX_POST_REPORTS_CREATOR_ID ON post_reports (creator_id);


/**
    Person Registration Application
    */
CREATE TABLE person_applications
(
  id                 BIGSERIAL PRIMARY KEY,
  admin_id           BIGINT                                    NULL,
  person_id          BIGINT                                    NOT NULL,
  question           TEXT                                      NULL,
  answer             TEXT                                      NULL,
  application_status VARCHAR(255)                              NOT NULL,
  created_at         TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at         TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE INDEX IDX_PEOPLE_APPLICATIONS_PERSON_ID ON person_applications (person_id);

/**
    Instance Config
    */

CREATE TABLE instance_configs
(
  id                            BIGSERIAL PRIMARY KEY,
  instance_id                   BIGINT       NOT NULL UNIQUE,
  registration_mode             VARCHAR(255) NULL     DEFAULT 'Closed',
  registration_question         TEXT         NULL,
  private_instance              BOOL         NULL,
  enable_downvotes              BOOL         NULL,
  enable_nsfw                   BOOL         NULL,
  community_creation_admin_only BOOL         NULL,
  application_email_admins      BOOL         NULL,
  report_email_admins           BOOL         NULL,
  hide_modlog_mod_names         BOOL         NULL,
  federation_enabled            BOOL         NULL,
  captcha_enabled               BOOL         NULL,
  captcha_difficulty            TEXT         NULL,
  require_email_verification    BOOL         NULL,
  actor_name_max_length         BIGINT       NULL,
  default_theme                 TEXT         NULL,
  legal_information             TEXT         NULL,
  default_post_listing_type     VARCHAR(255) NULL,
  created_at                    timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at                    timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

/**
  Slur Filters
 */
CREATE TABLE slur_filters
(
  id               BIGSERIAL PRIMARY KEY,
  slur_action_type VARCHAR(255)                              NOT NULL,
  slur_regex       TEXT                                      NOT NULL,
  created_at       TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at       TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

/**
  Announcements
 */
CREATE TABLE announcements
(
  id            BIGSERIAL PRIMARY KEY,
  content       TEXT                                      NOT NULL,
  is_active     BOOL                                      NOT NULL DEFAULT true,
  creator_id    BIGINT                                    NOT NULL,
  local_site_id BIGINT                                    NOT NULL,
  created_at    TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at    TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

/*
  Captcha
 */
CREATE TABLE captcha
(
  id         BIGSERIAL PRIMARY KEY,
  uuid       VARCHAR(36)  NOT NULL,
  word       TEXT         NOT NULL,
  png        TEXT         NOT NULL,
  wav        TEXT         NULL,
  locked     BOOLEAN      NOT NULL DEFAULT false,
  updated_at TIMESTAMP(3) NOT NULL
);

CREATE UNIQUE INDEX IDX_CAPTCHA_UUID ON captcha (uuid);

/*
  Custom Emoji table
 */
CREATE TABLE custom_emojis
(
  id            BIGSERIAL PRIMARY KEY,
  local_site_id BIGINT                                    NOT NULL,
  alt_text      TEXT                                      NOT NULL,
  image_url     TEXT                                      NOT NULL,
  category      TEXT                                      NOT NULL,
  shortcode     VARCHAR(128)                              NOT NULL,
  created_at    TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at    TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

/**
  Custom Emoji Keyword table
 */
CREATE TABLE custom_emoji_keywords
(
  id              BIGSERIAL PRIMARY KEY,
  custom_emoji_id BIGINT       NOT NULL,
  keyword         VARCHAR(255) NOT NULL
);

CREATE INDEX IDX_CUSTOM_EMOJI_KEYWORD_CUSTOM_EMOJI_ID ON custom_emoji_keywords (custom_emoji_id);

/**
  Roles table
 */
CREATE TABLE acl_roles
(
  id            BIGSERIAL PRIMARY KEY,
  name          VARCHAR(255)                              NOT NULL,
  description   TEXT                                      NOT NULL,
  inherits_from BIGINT                                    NULL,
  is_active     BOOL                                      NOT NULL DEFAULT true,
  expires_at    TIMESTAMP(3)                              NULL,
  created_at    TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at    TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE UNIQUE INDEX IDX_ACL_ROLES_NAME ON acl_roles (name);
CREATE INDEX IDX_ACL_ROLES_INHERITS_FROM ON acl_roles (inherits_from);

/**
  Role permissions table
 */
CREATE TABLE acl_role_permissions
(
  id         BIGSERIAL PRIMARY KEY,
  role_id    BIGINT NOT NULL,
  permission TEXT   NOT NULL
);

CREATE INDEX IDX_ACL_ROLE_PERMISSIONS_ROLE_ID ON acl_role_permissions (role_id);
CREATE UNIQUE INDEX IDX_ACL_ROLE_PERMISSIONS_ROLE_ID_PERMISSION ON acl_role_permissions (role_id, permission);

/**
  Person Meta Data table
 */
CREATE TABLE person_meta_data
(
  id           BIGSERIAL PRIMARY KEY,
  person_id    BIGINT                                    NOT NULL,
  token        VARCHAR(255)                              NOT NULL,
  ip_address   VARCHAR(255)                              NULL,
  user_agent   TEXT                                      NULL,
  active       BOOL                                      NULL DEFAULT true,
  created_at   TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  last_used_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

/**
  Post History table
 */
CREATE TABLE post_history
(
  id            BIGSERIAL PRIMARY KEY,
  post_id       BIGINT       NOT NULL,
  title         TEXT         NOT NULL,
  body          TEXT         NOT NULL,
  url           TEXT         NULL,
  is_nsfw       BOOL         NOT NULL,
  is_locked     BOOL         NOT NULL,
  is_deleted    BOOL         NOT NULL,
  removed_state VARCHAR(255) NULL     DEFAULT 'NOT_REMOVED',
  created_at    TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

/**
  Comment History table
 */
CREATE TABLE comment_history
(
  id            BIGSERIAL PRIMARY KEY,
  comment_id    BIGINT       NOT NULL,
  content       TEXT         NOT NULL,
  is_deleted    BOOL         NOT NULL,
  removed_state VARCHAR(255) NULL     DEFAULT 'NOT_REMOVED',
  created_at    TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);


/**
  Reset Password table
 */

CREATE TABLE reset_password
(
  id         BIGSERIAL PRIMARY KEY,
  person_id  BIGINT       NOT NULL,
  token      VARCHAR(255) NOT NULL,
  is_used    BOOL         NULL     DEFAULT false,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

/**
  Person Verification table
 */

CREATE TABLE person_email_verification
(
  id         BIGSERIAL PRIMARY KEY,
  person_id  BIGINT       NOT NULL,
  token      VARCHAR(255) NOT NULL,
  ip_address VARCHAR(255) NULL,
  user_agent TEXT         NULL,
  active     BOOL         NOT NULL DEFAULT true,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

/**
  Email Table
 */

CREATE TABLE email
(
  id           BIGSERIAL PRIMARY KEY,
  subject      TEXT         NOT NULL,
  html_content TEXT         NOT NULL,
  text_content TEXT         NOT NULL,
  created_at   TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  last_try_at  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

/**
  Email Data Table
 */

CREATE TABLE email_data
(
  id        BIGSERIAL PRIMARY KEY,
  recipient VARCHAR(255) NOT NULL,
  email_id  BIGINT       NOT NULL
);

/**
  Create Media Table
 */
CREATE TABLE media
(
  id           BIGSERIAL PRIMARY KEY,
  person_id    BIGSERIAL    NOT NULL,
  delete_token VARCHAR(255) NOT NULL,
  file         VARCHAR(255) NOT NULL,
  created_at   TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at   TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);
CREATE INDEX IDX_MEDIA_PERSON_ID ON media (person_id);
CREATE INDEX IDX_MEDIA_FILE ON media (file);

/**
  Email Person Table
 */

CREATE TABLE email_person_recipients
(
  id        BIGSERIAL PRIMARY KEY,
  person_id BIGINT NOT NULL,
  email_id  BIGINT NOT NULL
);

CREATE INDEX IDX_EMAIL_PERSON_RECIPIENTS_PERSON_ID ON email_person_recipients (person_id);
CREATE INDEX IDX_EMAIL_PERSON_RECIPIENTS_EMAIL_ID ON email_person_recipients (email_id);

/**
 Person Comment Link table
 */
CREATE TABLE link_person_comments
(
  id         BIGSERIAL PRIMARY KEY,
  person_id  BIGINT                                    NOT NULL,
  comment_id BIGINT                                    NOT NULL,
  link_type  VARCHAR(255)                              NOT NULL,
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL
);

CREATE INDEX IDX_LINK_PERSON_COMMENT_PERSON_ID_COMMENT_ID ON link_person_comments (person_id, comment_id);
CREATE UNIQUE INDEX IDX_LINK_PERSON_COMMENT_PERSON_ID_COMMENT_ID_LINK_TYPE ON link_person_comments (person_id,
                                                                                                    comment_id,
                                                                                                    link_type);

/**
  Link Person Person Table
 */
CREATE TABLE link_person_person
(
  id             BIGSERIAL PRIMARY KEY,
  from_person_id BIGINT       NOT NULL,
  to_person_id   BIGINT       NOT NULL,
  link_type      VARCHAR(255) NOT NULL,
  created_at     TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE INDEX IDX_LINK_PERSON_PERSON_FROM_PERSON_ID_TO_PERSON_ID ON link_person_person (from_person_id, to_person_id);
CREATE UNIQUE INDEX IDX_LINK_PERSON_PERSON_FROM_PERSON_ID_TO_PERSON_ID_LINK_TYPE ON link_person_person (from_person_id,
                                                                                                        to_person_id,
                                                                                                        link_type);

-- Add a trigger for every updated_at column ( yes i know its hacky but it works ;) )

DO
$$
  DECLARE
    t RECORD;
  BEGIN
    FOR t IN
      SELECT table_name, column_name
      FROM information_schema.columns
      WHERE column_name = 'updated_at'
      LOOP
        EXECUTE 'CREATE TRIGGER update_' || t.table_name || '_updated_at_time
   BEFORE UPDATE ON ' || t.table_name || '
   FOR EACH ROW
   EXECUTE PROCEDURE fn_sublinks_updated_at();';
      END LOOP;
  END
$$;

