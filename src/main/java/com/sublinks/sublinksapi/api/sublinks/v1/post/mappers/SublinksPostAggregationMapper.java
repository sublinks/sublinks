package com.sublinks.sublinksapi.api.sublinks.v1.post.mappers;

import com.sublinks.sublinksapi.api.sublinks.v1.post.models.AggregatePostResponse;
import com.sublinks.sublinksapi.post.entities.PostAggregate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class SublinksPostAggregationMapper implements
    Converter<PostAggregate, AggregatePostResponse> {

  @Override
  @Mapping(target = "key", source = "postAggregate.id")
  @Mapping(target = "commentCount", source = "postAggregate.commentCount")
  @Mapping(target = "downvoteCount", source = "postAggregate.downVoteCount")
  @Mapping(target = "upvoteCount", source = "postAggregate.upVoteCount")
  @Mapping(target = "score", source = "postAggregate.score")
  @Mapping(target = "hotRank", source = "postAggregate.hotRank")
  @Mapping(target = "controversyRank", source = "postAggregate.controversyRank")
  public abstract AggregatePostResponse convert(@Nullable PostAggregate postAggregate);


}
