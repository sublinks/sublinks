package com.sublinks.sublinksapi.api.sublinks.v1.instance.service;

import com.sublinks.sublinksapi.api.sublinks.v1.instance.models.IndexInstance;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.models.InstanceAggregateResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.models.InstanceConfigResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.models.InstanceResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.instance.models.UpdateInstanceConfig;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionInstanceTypes;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.instance.entities.Instance;
import com.sublinks.sublinksapi.instance.entities.InstanceConfig;
import com.sublinks.sublinksapi.instance.repositories.InstanceAggregateRepository;
import com.sublinks.sublinksapi.instance.repositories.InstanceConfigRepository;
import com.sublinks.sublinksapi.instance.repositories.InstanceRepository;
import com.sublinks.sublinksapi.instance.services.InstanceConfigService;
import com.sublinks.sublinksapi.instance.services.InstanceService;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.utils.PaginationUtils;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class SublinksInstanceService {

  private final InstanceRepository instanceRepository;
  private final InstanceConfigRepository instanceConfigRepository;
  private final InstanceAggregateRepository instanceAggregateRepository;
  private final InstanceService instanceService;
  private final InstanceConfigService instanceConfigService;
  private final ConversionService conversionService;
  private final RolePermissionService rolePermissionService;

  public List<InstanceResponse> index(final IndexInstance indexInstance) {

    PageRequest pageRequest = PageRequest.of(PaginationUtils.getPage(indexInstance.page()),
        PaginationUtils.getPerPage(indexInstance.perPage()));
    List<Instance> instances;

    if (indexInstance.search() == null) {
      instances = instanceRepository.findAll(pageRequest)
          .getContent();
    } else {
      instances = instanceRepository.findInstancesByDomainOrDescriptionOrSidebar(
          indexInstance.search(), pageRequest);
    }

    return convertToInstanceResponses(instances);
  }

  private List<InstanceResponse> convertToInstanceResponses(List<Instance> instances) {

    return instances.stream()
        .map(instance -> conversionService.convert(instance, InstanceResponse.class))
        .toList();
  }

  public InstanceResponse show(final String key) {

    return conversionService.convert(instanceRepository.findInstanceByDomain(key),
        InstanceResponse.class);
  }

  public InstanceAggregateResponse showAggregate(final String key) {

    return conversionService.convert(instanceAggregateRepository.findByInstance_Domain(key),
        InstanceAggregateResponse.class);
  }

  public InstanceConfigResponse showConfig(final String key, final Person person) {

    rolePermissionService.isPermitted(person,
        RolePermissionInstanceTypes.INSTANCE_READ_ANNOUNCEMENT,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    return conversionService.convert(instanceConfigRepository.findByInstance_Domain(key)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "instance_not_found")),
        InstanceConfigResponse.class);
  }

  public InstanceConfigResponse updateConfig(final String key,
      final UpdateInstanceConfig updateInstanceConfigForm, final Person person)
  {

    rolePermissionService.isPermitted(person, RolePermissionInstanceTypes.INSTANCE_UPDATE_SETTINGS,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final InstanceConfig config = instanceConfigRepository.findByInstance_Domain(key)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "instance_not_found"));

    if (updateInstanceConfigForm.nameMaxLength() != null) {
      config.setActorNameMaxLength(Math.max(updateInstanceConfigForm.nameMaxLength(), 0));
    }

    instanceConfigService.updateInstanceConfig(config);
    return conversionService.convert(config, InstanceConfigResponse.class);
  }
}
