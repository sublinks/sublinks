package com.sublinks.sublinksapi.api.sublinks.v1.post.mappers;

import com.sublinks.sublinksapi.api.sublinks.v1.post.models.LinkMetaData;
import com.sublinks.sublinksapi.post.entities.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class SublinksLinkMetaDataMapper implements Converter<Post, LinkMetaData> {

  @Override
  @Mapping(target = "postKey", source = "post.titleSlug")
  @Mapping(target = "linkUrl", source = "post.linkUrl")
  @Mapping(target = "linkTitle", source = "post.linkTitle")
  @Mapping(target = "linkDescription", source = "post.linkDescription")
  @Mapping(target = "linkThumbnailUrl", source = "post.linkThumbnailUrl")
  @Mapping(target = "LinkVideoUrl", source = "post.linkVideoUrl")
  public abstract LinkMetaData convert(@Nullable Post post);


}
