package com.sublinks.sublinksapi.instance.services;

import com.sublinks.sublinksapi.instance.entities.Instance;
import com.sublinks.sublinksapi.instance.repositories.InstanceAggregateRepository;
import com.sublinks.sublinksapi.instance.repositories.InstanceRepository;
import com.sublinks.sublinksapi.utils.KeyGeneratorUtil;
import com.sublinks.sublinksapi.utils.KeyStore;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InstanceService {

  private final InstanceRepository instanceRepository;
  private final InstanceAggregateRepository instanceAggregateRepository;

  private final KeyGeneratorUtil keyGeneratorUtil;

  @Transactional
  public void createInstance(@NonNull Instance instance) {

    KeyStore keys = keyGeneratorUtil.generate();
    instance.setPublicKey(keys.publicKey());
    instance.setPrivateKey(keys.privateKey());
    instanceRepository.save(instance);
  }

  @Transactional
  public void createInstanceAndFlush(@NonNull Instance instance) {

    createInstance(instance);
    instanceRepository.flush();
  }

  @Transactional
  public void updateInstance(@NonNull Instance instance) {

    instanceRepository.save(instance);
  }
}
