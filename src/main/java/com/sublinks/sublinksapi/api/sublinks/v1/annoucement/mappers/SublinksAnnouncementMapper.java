package com.sublinks.sublinksapi.api.sublinks.v1.annoucement.mappers;

import com.sublinks.sublinksapi.announcement.entities.Announcement;
import com.sublinks.sublinksapi.api.sublinks.v1.annoucement.models.AnnouncementResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.person.mappers.SublinksPersonMapper;
import com.sublinks.sublinksapi.api.sublinks.v1.utils.DateUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {SublinksPersonMapper.class})
public abstract class SublinksAnnouncementMapper implements
    Converter<Announcement, AnnouncementResponse> {

  @Mapping(target = "key", source = "announcement.id")
  @Mapping(target = "content", source = "announcement.content")
  @Mapping(target = "createdAt",
      source = "announcement.createdAt",
      dateFormat = DateUtils.FRONT_END_DATE_FORMAT)
  @Mapping(target = "updatedAt",
      source = "announcement.updatedAt",
      dateFormat = DateUtils.FRONT_END_DATE_FORMAT)
  public abstract AnnouncementResponse convert(@Nullable Announcement announcement);


}
