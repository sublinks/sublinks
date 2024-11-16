package com.sublinks.sublinksapi.api.sublinks.v1.instance.mappers;

import com.sublinks.sublinksapi.api.sublinks.v1.instance.models.InstanceResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.utils.DateUtils;
import com.sublinks.sublinksapi.instance.entities.Instance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class SublinksInstanceMapper implements Converter<Instance, InstanceResponse> {

  @Override
  @Mapping(target = "key", source = "instance.domain")
  @Mapping(target = "name", source = "instance.name")
  @Mapping(target = "description", source = "instance.description")
  @Mapping(target = "domain", source = "instance.domain")
  @Mapping(target = "software", source = "instance.software")
  @Mapping(target = "version", source = "instance.version")
  @Mapping(target = "sidebar", source = "instance.sidebar")
  @Mapping(target = "iconUrl", source = "instance.iconUrl")
  @Mapping(target = "bannerUrl", source = "instance.bannerUrl")
  @Mapping(target = "publicKey", source = "instance.publicKey")
  @Mapping(target = "createdAt",
      source = "instance.createdAt",
      dateFormat = DateUtils.FRONT_END_DATE_FORMAT)
  @Mapping(target = "updatedAt",
      source = "instance.updatedAt",
      dateFormat = DateUtils.FRONT_END_DATE_FORMAT)
  public abstract InstanceResponse convert(@Nullable Instance instance);
}
