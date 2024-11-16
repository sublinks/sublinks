package com.sublinks.sublinksapi.api.sublinks.v1.person.mappers;

import com.sublinks.sublinksapi.api.sublinks.v1.instance.mappers.SublinksInstanceMapper;
import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.roles.mappers.SublinksPersonRoleMapper;
import com.sublinks.sublinksapi.api.sublinks.v1.utils.DateUtils;
import com.sublinks.sublinksapi.api.sublinks.v1.utils.PersonKeyUtils;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.person.entities.Person;
import java.text.SimpleDateFormat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {SublinksPersonRoleMapper.class, SublinksInstanceMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SublinksPersonMapper implements Converter<Person, PersonResponse> {

  @Autowired
  public PersonKeyUtils personKeyUtils;

  @Override
  @Mapping(target = "key", source = "person", qualifiedByName = "personKey")
  @Mapping(target = "name", source = "person.name")
  @Mapping(target = "displayName", source = "person", qualifiedByName = "display_name")
  @Mapping(target = "isBanned", source = "person", qualifiedByName = "is_banned")
  @Mapping(target = "banExpiresAt", source = "person", qualifiedByName = "ban_expires_at")
  @Mapping(target = "isDeleted", source = "person.deleted")
  @Mapping(target = "avatarImageUrl", source = "person", qualifiedByName = "avatar")
  @Mapping(target = "bannerImageUrl", source = "person", qualifiedByName = "banner")
  @Mapping(target = "isBotAccount", source = "person.botAccount")
  @Mapping(target = "role", source = "person.role")
  @Mapping(target = "bio", source = "person.biography")
  @Mapping(target = "isLocal", source = "person.local")
  @Mapping(target = "instance", source = "person.instance")
  @Mapping(target = "createdAt",
      source = "person.createdAt",
      dateFormat = DateUtils.FRONT_END_DATE_FORMAT)
  @Mapping(target = "updatedAt",
      source = "person.updatedAt",
      dateFormat = DateUtils.FRONT_END_DATE_FORMAT)
  public abstract PersonResponse convert(@Nullable Person person);

  @Named("personKey")
  String mapPersonKey(Person person) {

    return personKeyUtils.getPersonKey(person);
  }

  @Named("is_banned")
  Boolean mapIsBanned(Person person) {

    return RolePermissionService.isBanned(person);
  }

  @Named("display_name")
  String mapDisplayName(Person person) {

    return !person.getDisplayName()
        .isBlank() ? person.getDisplayName() : null;
  }

  @Named("avatar")
  String mapAvatar(Person person) {

    return !person.getAvatarImageUrl()
        .isBlank() ? person.getAvatarImageUrl() : null;
  }

  @Named("banner")
  String mapBanner(Person person) {

    return !person.getBannerImageUrl()
        .isBlank() ? person.getBannerImageUrl() : null;
  }

  @Named("ban_expires_at")
  String mapBanExpiresAt(Person person) {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

    simpleDateFormat.applyPattern(DateUtils.FRONT_END_DATE_FORMAT);

    return person.getRole()
        .getExpiresAt() != null ? simpleDateFormat.format(person.getRole()
        .getExpiresAt()) : null;
  }
}
