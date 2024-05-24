package com.sublinks.sublinksapi.api.lemmy.v3.community.controllers;

import com.sublinks.sublinksapi.api.lemmy.v3.community.models.AddModToCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.AddModToCommunityResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.BanFromCommunityResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.CommunityResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.DeleteCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.GetCommunityResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.HideCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.RemoveCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.TransferCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.user.models.BanPerson;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "/api/v3/community")
@Tag(name = "community", description = "the community API")
public class CommunityModActionsController {
    @PutMapping("hide")
    CommunityResponse hide(@Valid final HideCommunity hideCommunityForm) {

        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("delete")
    CommunityResponse delete(@Valid final DeleteCommunity deleteCommunityForm) {

        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("remove")
    CommunityResponse remove(@Valid final RemoveCommunity removeCommunityForm) {

        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("transfer")
    GetCommunityResponse transfer(@Valid final TransferCommunity transferCommunityForm) {

        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("ban_user")
    BanFromCommunityResponse banUser(@Valid final BanPerson banPersonForm) {

        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("mod")
    AddModToCommunityResponse addMod(@Valid final AddModToCommunity addModToCommunityForm) {

        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }
}
