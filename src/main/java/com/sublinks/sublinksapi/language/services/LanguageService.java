package com.sublinks.sublinksapi.language.services;

import com.sublinks.sublinksapi.community.entities.Community;
import com.sublinks.sublinksapi.instance.entities.Instance;
import com.sublinks.sublinksapi.language.entities.Language;
import com.sublinks.sublinksapi.language.repositories.LanguageRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LanguageService {

  private final LanguageRepository languageRepository;

  public Language getLanguageOfCommunityOrUndefined(final Community community) {

    return community.getLanguages()
        .stream()
        .findFirst()
        .orElse(languageRepository.findLanguageByCode("und"));
  }

  public List<Long> instanceLanguageIds(final Instance instance) {

    final List<Long> discussionLanguages = new ArrayList<>();
    for (Language language : instance.getLanguages()) {
      discussionLanguages.add(language.getId());
    }
    return discussionLanguages;
  }

  public List<Language> languageIdsToEntity(final Collection<String> languageIds) {

    final List<Language> languages = new ArrayList<>();
    for (String languageCode : languageIds) {
      final Optional<Language> language = languageRepository.findById(Long.valueOf(languageCode));
      language.ifPresent(languages::add);
    }
    return languages;
  }
}
