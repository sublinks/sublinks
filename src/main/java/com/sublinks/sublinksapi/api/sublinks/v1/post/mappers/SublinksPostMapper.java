package com.sublinks.sublinksapi.api.sublinks.v1.post.mappers;

import com.sublinks.sublinksapi.api.sublinks.v1.community.mappers.SublinksCommunityMapper;
import com.sublinks.sublinksapi.api.sublinks.v1.person.mappers.SublinksPersonMapper;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.post.models.PostResponse;
import com.sublinks.sublinksapi.person.entities.LinkPersonPost;
import com.sublinks.sublinksapi.person.enums.LinkPersonPostType;
import com.sublinks.sublinksapi.post.entities.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {SublinksLinkMetaDataMapper.class, SublinksPostAggregationMapper.class,
        SublinksPersonMapper.class, SublinksCommunityMapper.class, ConversionService.class})
public abstract class SublinksPostMapper implements Converter<Post, PostResponse> {

  protected SublinksPostAggregationMapper sublinksPostAggregationMapper;
  protected SublinksLinkMetaDataMapper sublinksLinkMetaDataMapper;
  protected SublinksPersonMapper sublinksPersonMapper;
  protected SublinksCommunityMapper sublinksCommunityMapper;
  protected ConversionService conversionService;

  @Override
  @Mapping(target = "key", source = "post.titleSlug")
  @Mapping(target = "title", source = "post.title")
  @Mapping(target = "titleSlug", source = "post.titleSlug")
  @Mapping(target = "body", source = "post.postBody")
  @Mapping(target = "linkMetaData", source = "post")
  @Mapping(target = "isLocal", source = "post.local")
  @Mapping(target = "isDeleted", source = "post.deleted")
  @Mapping(target = "isNsfw", source = "post.nsfw")
  @Mapping(target = "isLocked", source = "post.locked")
  @Mapping(target = "isFeatured", source = "post.featured")
  @Mapping(target = "isFeaturedInCommunity", source = "post.featuredInCommunity")
  @Mapping(target = "community", source = "post.community")
  @Mapping(target = "creator",
      source = "post",
      defaultExpression = "java(getCreator(post, conversionService))")
  @Mapping(target = "postAggregate", source = "post.postAggregate")
  @Mapping(target = "activityPubId", source = "post.activityPubId")
  @Mapping(target = "publicKey", source = "post.publicKey")
  @Mapping(target = "isRemoved", expression = "java(post.isRemoved())")
  public abstract PostResponse convert(@Nullable Post post);


  public PersonResponse getCreator(Post post, ConversionService conversionService)
  {

    for (LinkPersonPost linkPersonPost : post.getLinkPersonPost()) {
      if (linkPersonPost.getLinkType()
          .equals(LinkPersonPostType.creator)) {
        return conversionService.convert(linkPersonPost.getPerson(), PersonResponse.class);
      }
    }
    return null;
  }

}
