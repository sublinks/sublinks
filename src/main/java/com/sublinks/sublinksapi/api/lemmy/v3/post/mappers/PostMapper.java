package com.sublinks.sublinksapi.api.lemmy.v3.post.mappers;

import com.sublinks.sublinksapi.api.lemmy.v3.post.models.Post;
import com.sublinks.sublinksapi.api.lemmy.v3.utils.DateUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PostMapper extends Converter<com.sublinks.sublinksapi.post.entities.Post, Post> {

  @Override
  @Mapping(target = "creator_id", ignore = true)
  @Mapping(target = "url", source = "post.linkUrl")
  @Mapping(target = "thumbnail_url", source = "post.linkThumbnailUrl")
  @Mapping(target = "published", source = "post.createdAt", dateFormat = DateUtils.FRONT_END_DATE_FORMAT)
  @Mapping(target = "name", source = "post.title")
  @Mapping(target = "locked", source = "post.locked")
  @Mapping(target = "local", constant = "true")
  @Mapping(target = "language_id", source = "post.language.id")
  @Mapping(target = "featured_local", source = "post.featured")
  @Mapping(target = "featured_community", source = "post.featuredInCommunity")
  @Mapping(target = "embed_video_url", source = "post.linkVideoUrl")
  @Mapping(target = "embed_title", source = "post.linkTitle")
  @Mapping(target = "embed_description", source = "post.linkDescription")
  @Mapping(target = "community_id", source = "post.community.id")
  @Mapping(target = "body", source = "post.postBody")
  @Mapping(target = "removed", expression = "java(post.isRemoved())")
  @Mapping(target = "updated", source = "post.updatedAt", dateFormat = DateUtils.FRONT_END_DATE_FORMAT)
  @Mapping(target = "ap_id", source = "post.activityPubId")
  Post convert(@Nullable com.sublinks.sublinksapi.post.entities.Post post);

}
