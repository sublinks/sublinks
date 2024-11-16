package com.sublinks.sublinksapi.api.sublinks.v1.utils.mappers;

import com.sublinks.sublinksapi.api.sublinks.v1.languages.mappers.SublinksLanguageMapper;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {SublinksLanguageMapper.class})
public class OptionalStringMapper implements Converter<String, Optional<String>> {

  @Nullable
  @Override
  public Optional<String> convert(@Nullable String source) {

    return Optional.ofNullable(source);
  }
}
