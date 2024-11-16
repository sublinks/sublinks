/**
  Media
 */
ALTER TABLE media
  ADD FOREIGN KEY (person_id) REFERENCES people (id) ON DELETE CASCADE;

/**
  Comments table
 */
ALTER TABLE comments
  ADD FOREIGN KEY (language_id) REFERENCES languages (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (person_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (community_id) REFERENCES communities (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE;

/**
  Comment Aggregates table
 */
ALTER TABLE comment_aggregates
  ADD FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE;

/**
  Comment Likes table
 */
ALTER TABLE comment_likes
  ADD FOREIGN KEY (person_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE;

/**
  Communities table
 */
ALTER TABLE communities
  ADD FOREIGN KEY (instance_id) REFERENCES instances (id) ON DELETE CASCADE;

/**
  Community Languages table
 */
ALTER TABLE community_languages
  ADD FOREIGN KEY (community_id) REFERENCES communities (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (language_id) REFERENCES languages (id) ON DELETE CASCADE;

/**
  Community Aggregates table
 */
ALTER TABLE community_aggregates
  ADD FOREIGN KEY (community_id) REFERENCES communities (id) ON DELETE CASCADE;

/**
  Instance Aggregates table
 */
ALTER TABLE instance_aggregates
  ADD FOREIGN KEY (instance_id) REFERENCES instances (id) ON DELETE CASCADE;

/**
  Instance Languages table
 */
ALTER TABLE instance_languages
  ADD FOREIGN KEY (instance_id) REFERENCES instances (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (language_id) REFERENCES languages (id) ON DELETE CASCADE;

/**
  Person Aggregates table
 */
ALTER TABLE person_aggregates
  ADD FOREIGN KEY (person_id) REFERENCES people (id) ON DELETE CASCADE;

/**
  Link Person Communities table
 */
ALTER TABLE link_person_instances
  ADD FOREIGN KEY (person_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (instance_id) REFERENCES instances (id) ON DELETE CASCADE;

/**
  Person Languages table
 */
ALTER TABLE person_languages
  ADD FOREIGN KEY (person_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (language_id) REFERENCES languages (id) ON DELETE CASCADE;

/**
  Posts table
 */
ALTER TABLE posts
  ADD FOREIGN KEY (instance_id) REFERENCES instances (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (language_id) REFERENCES languages (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (community_id) REFERENCES communities (id) ON DELETE CASCADE;

/**
  Post Aggregates table
 */
ALTER TABLE post_aggregates
  ADD FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (community_id) REFERENCES communities (id) ON DELETE CASCADE;

/**
  Posts Likes table
 */
ALTER TABLE post_likes
  ADD FOREIGN KEY (person_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE;

/**
  Comment Likes table
 */
ALTER TABLE comment_likes
  ADD FOREIGN KEY (person_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE;

/**
  Post Post Cross Post table
 */
ALTER TABLE post_post_cross_post
  ADD FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (cross_post_id) REFERENCES post_cross_posts (id) ON DELETE CASCADE;

/**
  Private Messages table
 */
ALTER TABLE private_messages
  ADD FOREIGN KEY (sender_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (recipient_id) REFERENCES people (id) ON DELETE CASCADE;

/**
  Person Mention table
 */
ALTER TABLE person_mentions
  ADD FOREIGN KEY (recipient_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE;

/**
  Private Message Report table
 */
ALTER TABLE private_messages_reports
  ADD FOREIGN KEY (private_message_id) REFERENCES private_messages (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (resolver_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (creator_id) REFERENCES people (id) ON DELETE CASCADE;

/**
  Comment Report table
  */
ALTER TABLE comment_reports
  ADD FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (creator_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (resolver_id) REFERENCES people (id) ON DELETE CASCADE;

/**
  Comment Report table
  */
ALTER TABLE post_reports
  ADD FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (creator_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (resolver_id) REFERENCES people (id) ON DELETE CASCADE;

/**
  Comment Replies table
  */
ALTER TABLE comment_replies
  ADD FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (recipient_id) REFERENCES people (id) ON DELETE CASCADE;

/**
  People Applications table
  */
ALTER TABLE person_applications
  ADD FOREIGN KEY (person_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (admin_id) REFERENCES people (id) ON DELETE CASCADE;

/**
  Instance Config table
 */
ALTER TABLE instance_configs
  ADD FOREIGN KEY (instance_id) REFERENCES instances (id) ON DELETE CASCADE;

/**
  Moderation Logs table
 */
ALTER TABLE moderation_logs
  ADD FOREIGN KEY (instance_id) REFERENCES instances (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (admin_person_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (moderation_person_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (other_person_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (community_id) REFERENCES communities (id) ON DELETE CASCADE;

/**
  Custom Emojis Keyword table
 */
ALTER TABLE custom_emoji_keywords
  ADD FOREIGN KEY (custom_emoji_id) REFERENCES custom_emojis (id) ON DELETE CASCADE;

/**
  Role Permissions table
 */
ALTER TABLE acl_role_permissions
  ADD FOREIGN KEY (role_id) REFERENCES acl_roles (id) ON DELETE CASCADE;

/**
  Role Table
 */
ALTER TABLE acl_roles
  ADD FOREIGN KEY (inherits_from) REFERENCES acl_roles (id) ON DELETE SET NULL;

/**
  People table
 */
ALTER TABLE people
  ADD FOREIGN KEY (role_id) REFERENCES acl_roles (id) ON DELETE RESTRICT ON UPDATE CASCADE;

/**
  Post History table
 */
ALTER TABLE post_history
  ADD FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE ON UPDATE CASCADE;

/**
  Comment History table
 */
ALTER TABLE comment_history
  ADD FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE ON UPDATE CASCADE;

/**
  Person Email Verifications table
 */
ALTER TABLE person_email_verification
  ADD FOREIGN KEY (person_id) REFERENCES people (id) ON DELETE CASCADE;

/**
  Password Reset table
 */
ALTER TABLE reset_password
  ADD FOREIGN KEY (person_id) REFERENCES people (id) ON DELETE CASCADE;

/**
  Email Data table
 */
ALTER TABLE email_data
  ADD FOREIGN KEY (email_id) REFERENCES email (id) ON DELETE CASCADE;

/**
  Email Person Table
 */
ALTER TABLE email_person_recipients
  ADD FOREIGN KEY (email_id) REFERENCES email (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (person_id) REFERENCES people (id) ON DELETE CASCADE;

/**
  Comment Saves table
 */
ALTER TABLE link_person_comments
  ADD FOREIGN KEY (person_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE;


/**
  Person Post Link table
 */
ALTER TABLE link_person_posts
  ADD FOREIGN KEY (person_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE;
/**
  Announcement table
 */
ALTER TABLE announcements
  ADD FOREIGN KEY (creator_id) REFERENCES people (id) ON DELETE CASCADE;
/**
  Link Person Person Table
 */
ALTER TABLE link_person_person
  ADD FOREIGN KEY (from_person_id) REFERENCES people (id) ON DELETE CASCADE,
  ADD FOREIGN KEY (to_person_id) REFERENCES people (id) ON DELETE CASCADE;