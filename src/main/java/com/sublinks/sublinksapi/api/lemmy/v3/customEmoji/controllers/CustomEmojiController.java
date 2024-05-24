package com.sublinks.sublinksapi.api.lemmy.v3.customEmoji.controllers;

import com.sublinks.sublinksapi.api.lemmy.v3.customEmoji.models.CreateCustomEmoji;
import com.sublinks.sublinksapi.api.lemmy.v3.customEmoji.models.CustomEmojiResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.customEmoji.models.DeleteCustomEmoji;
import com.sublinks.sublinksapi.api.lemmy.v3.customEmoji.models.EditCustomEmoji;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "/api/v3/custom_emoji")
@Tag(name = "custom_emoji", description = "the custom emoji API")
public class CustomEmojiController {
    @PostMapping
    CustomEmojiResponse create(@Valid final CreateCustomEmoji createCustomEmojiForm) {

        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping
    CustomEmojiResponse update(@Valid final EditCustomEmoji editCustomEmojiForm) {

        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("delete")
    CustomEmojiResponse delete(@Valid final DeleteCustomEmoji deleteCustomEmojiForm) {

        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }
}
