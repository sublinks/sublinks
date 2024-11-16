package com.sublinks.sublinksapi.language.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sublinks.sublinksapi.instance.entities.Instance;
import com.sublinks.sublinksapi.language.entities.Language;
import com.sublinks.sublinksapi.language.repositories.LanguageRepository;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LanguageServiceUnitTests {

  static Language english;
  static Language afrikaans;
  @Mock
  LanguageRepository languageRepository;

  @BeforeAll
  static void setup() {

    english = new Language();
    english.setId((long) 1);
    english.setCode("en");
    english.setName("English");

    afrikaans = new Language();
    afrikaans.setId((long) 2);
    afrikaans.setCode("af");
    afrikaans.setName("Afrikaans");
  }

  @Test
  void givenInstance_whenInstanceLanguageIds_thenReturnListOfIds() {

    List<Language> languages = Arrays.asList(english, afrikaans);

    Instance instance = new Instance();
    instance.setLanguages(languages);

    LanguageService languageService = new LanguageService(languageRepository);
    List<Long> discussionLanguages = languageService.instanceLanguageIds(instance);

    List<Long> expectedList = Arrays.asList(1L, 2L);

    assertTrue(expectedList.size() == discussionLanguages.size() && expectedList.containsAll(
            discussionLanguages) && discussionLanguages.containsAll(expectedList),
        "List of language ids returned did not match expected");
  }

  @Test
  void givenCollectionOfLanguageIds_whenLanguageIdsToEntity_thenReturnListOfLanguages() {

    Collection<String> languageIds = Arrays.asList("1", "2");
    Optional<Language> optionalEnglish = Optional.of(english);
    Optional<Language> optionalAfrikaans = Optional.of(afrikaans);

    Mockito.when(languageRepository.findById(1L))
        .thenReturn(optionalEnglish);
    Mockito.when(languageRepository.findById(2L))
        .thenReturn(optionalAfrikaans);

    LanguageService languageService = new LanguageService(languageRepository);

    List<Language> languages = languageService.languageIdsToEntity(languageIds);

    assertEquals(2, languages.size(),
        "Number of languages instances returned did not match expected");
  }
}
