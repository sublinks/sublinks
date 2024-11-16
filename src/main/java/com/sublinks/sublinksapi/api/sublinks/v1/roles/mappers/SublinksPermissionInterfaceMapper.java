package com.sublinks.sublinksapi.api.sublinks.v1.roles.mappers;

import com.sublinks.sublinksapi.authorization.enums.AllRoleTypes;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionInterface;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import java.util.logging.Logger;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {RolePermissionService.class})
public class SublinksPermissionInterfaceMapper implements
    Converter<String, RolePermissionInterface> {

  private final Logger logger = Logger.getLogger(SublinksPermissionInterfaceMapper.class.getName());

  @Override
  public RolePermissionInterface convert(@Nullable String permission)
  {

    for (RolePermissionInterface rolePermissionInterface : AllRoleTypes.ALL_ROLE_TYPES) {
      if (rolePermissionInterface.toString()
          .equals(permission)) {
        return rolePermissionInterface;
      }
    }
    logger.warning("Permission not found: " + permission);
    return null;
  }
}
