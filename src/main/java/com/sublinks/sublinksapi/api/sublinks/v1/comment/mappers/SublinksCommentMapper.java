package com.sublinks.sublinksapi.api.sublinks.v1.comment.mappers;

import com.sublinks.sublinksapi.api.sublinks.v1.comment.models.CommentResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.person.mappers.SublinksPersonMapper;
import com.sublinks.sublinksapi.api.sublinks.v1.utils.DateUtils;
import com.sublinks.sublinksapi.comment.entities.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {SublinksPersonMapper.class})
public abstract class SublinksCommentMapper implements Converter<Comment, CommentResponse> {

  @Override
  @Mapping(target = "key", source = "comment.path")
  @Mapping(target = "activityPubId", source = "comment.activityPubId")
  @Mapping(target = "body", source = "comment.commentBody")
  @Mapping(target = "path", source = "comment.path")
  @Mapping(target = "isLocal", source = "comment.local")
  @Mapping(target = "isDeleted", source = "comment.deleted")
  @Mapping(target = "isFeatured", source = "comment.featured")
  @Mapping(target = "isRemoved", expression = "java(comment != null && comment.isRemoved())")
  // @Mapping(target = "creator", expression = "java(personMapper.convert(comment.getPerson()))")
  @Mapping(target = "creator", source = "comment.person")
  @Mapping(target = "createdAt",
      source = "comment.createdAt",
      dateFormat = DateUtils.FRONT_END_DATE_FORMAT)
  @Mapping(target = "updatedAt",
      source = "comment.updatedAt",
      dateFormat = DateUtils.FRONT_END_DATE_FORMAT)
  @Mapping(target = "replies", ignore = true)
  public abstract CommentResponse convert(@Nullable Comment comment);
}