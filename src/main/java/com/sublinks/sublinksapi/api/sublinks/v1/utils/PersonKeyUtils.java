package com.sublinks.sublinksapi.api.sublinks.v1.utils;

import com.sublinks.sublinksapi.api.sublinks.v1.person.models.PersonIdentity;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.utils.UrlUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class PersonKeyUtils {

    private final UrlUtil urlUtil;

    public String getPersonKey(Person person) {
        return person.getName() + "@" + urlUtil.cleanUrlProtocol(person.getInstance().getDomain());
    }

    public PersonIdentity getPersonIdentity(String personKey) {
        String[] parts = personKey.split("@");
        return new PersonIdentity(parts[0], parts[1]);
    }

}
