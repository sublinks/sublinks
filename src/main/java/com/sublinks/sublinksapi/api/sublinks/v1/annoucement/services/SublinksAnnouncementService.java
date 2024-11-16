package com.sublinks.sublinksapi.api.sublinks.v1.annoucement.services;

import com.sublinks.sublinksapi.announcement.entities.Announcement;
import com.sublinks.sublinksapi.announcement.repositories.AnnouncementRepository;
import com.sublinks.sublinksapi.api.sublinks.v1.annoucement.models.AnnouncementResponse;
import com.sublinks.sublinksapi.api.sublinks.v1.annoucement.models.CreateAnnouncement;
import com.sublinks.sublinksapi.api.sublinks.v1.annoucement.models.IndexAnnouncement;
import com.sublinks.sublinksapi.api.sublinks.v1.annoucement.models.RemoveAnnouncement;
import com.sublinks.sublinksapi.api.sublinks.v1.annoucement.models.UpdateAnnouncement;
import com.sublinks.sublinksapi.api.sublinks.v1.common.enums.SortOrder;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionInstanceTypes;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.instance.models.LocalInstanceContext;
import com.sublinks.sublinksapi.person.entities.Person;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@Service
public class SublinksAnnouncementService {

  private final LocalInstanceContext localInstanceContext;
  private final ConversionService conversionService;
  private final RolePermissionService rolePermissionService;
  private final AnnouncementRepository announcementRepository;

  /**
   * Retrieves a list of AnnouncementResponse objects based on the provided indexAnnouncementForm
   * and person.
   *
   * @param indexAnnouncementForm The form containing the sorting and pagination details.
   * @param person                The person requesting the announcements.
   * @return Returns a list of AnnouncementResponse objects representing the retrieved
   * announcements.
   * @throws ResponseStatusException if the person is not allowed to read the announcements.
   */
  public List<AnnouncementResponse> index(final IndexAnnouncement indexAnnouncementForm,
      final Person person)
  {

    rolePermissionService.isPermitted(person,
        RolePermissionInstanceTypes.INSTANCE_READ_ANNOUNCEMENTS,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final List<Announcement> announcements = this.announcementRepository.findAll(
            PageRequest.of(indexAnnouncementForm.page() - 1, indexAnnouncementForm.perPage(), Sort.by(
                indexAnnouncementForm.sortOrder() == SortOrder.Desc ? Direction.DESC : Direction.ASC,
                "createdAt")))
        .toList();

    return announcements.stream()
        .map((announcement) -> this.conversionService.convert(announcement,
            AnnouncementResponse.class))
        .toList();
  }

  /**
   * Retrieves the announcement with the given key and converts it to an
   * {@link AnnouncementResponse} object.
   *
   * @param key    The key of the announcement to be retrieved.
   * @param person The person requesting the announcement.
   * @return Returns the converted {@link AnnouncementResponse} object representing the retrieved
   * announcement.
   * @throws ResponseStatusException if the person is not allowed to read the announcement or if the
   *                                 announcement does not exist.
   */
  public AnnouncementResponse show(final Long key, final Person person) {

    rolePermissionService.isPermitted(person,
        RolePermissionInstanceTypes.INSTANCE_READ_ANNOUNCEMENT,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    try {
      final Announcement announcement = this.announcementRepository.getReferenceById(key);

      return this.conversionService.convert(announcement, AnnouncementResponse.class);
    } catch (EntityNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "announcement_not_found");
    }
  }

  /**
   * Creates a new announcement based on the provided form and person.
   *
   * @param createAnnouncementForm The form containing the details of the announcement to be
   *                               created.
   * @param person                 The person creating the announcement.
   * @return Returns an AnnouncementResponse object representing the created announcement.
   */
  public AnnouncementResponse create(final CreateAnnouncement createAnnouncementForm,
      final Person person)
  {

    rolePermissionService.isPermitted(person,
        RolePermissionInstanceTypes.INSTANCE_CREATE_ANNOUNCEMENT,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final Announcement announcement = Announcement.builder()
        .content(createAnnouncementForm.content())
        .localSiteId(localInstanceContext.instance()
            .getId())
        .active(createAnnouncementForm.active())
        .creator(person)
        .build();

    this.announcementRepository.save(announcement);

    return this.conversionService.convert(announcement, AnnouncementResponse.class);
  }

  /**
   * Updates an announcement based on the given ID, update form, and person.
   *
   * @param id                     The ID of the announcement to be updated.
   * @param updateAnnouncementForm The form containing the updated details.
   * @param person                 The person making the update.
   * @return Returns an AnnouncementResponse object representing the updated announcement.
   */
  public AnnouncementResponse update(final Long id, final UpdateAnnouncement updateAnnouncementForm,
      final Person person)
  {

    rolePermissionService.isPermitted(person,
        RolePermissionInstanceTypes.INSTANCE_UPDATE_ANNOUNCEMENT,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final Announcement announcement = this.announcementRepository.getReferenceById(id);

    if (updateAnnouncementForm.content() != null) {
      announcement.setContent(updateAnnouncementForm.content());
    }
    if (updateAnnouncementForm.active() != null) {
      announcement.setActive(updateAnnouncementForm.active());
    }
    this.announcementRepository.save(announcement);
    return conversionService.convert(announcement, AnnouncementResponse.class);
  }

  /**
   * Deletes an announcement based on the given ID and person.
   *
   * @param id                     The ID of the announcement to be deleted.
   * @param removeAnnouncementForm The form containing the removal details.
   * @param person                 The person making the deletion.
   * @return Returns an {@link AnnouncementResponse} object representing the deleted announcement.
   */
  public AnnouncementResponse remove(final Long id, final RemoveAnnouncement removeAnnouncementForm,
      final Person person)
  {

    rolePermissionService.isPermitted(person,
        RolePermissionInstanceTypes.INSTANCE_DELETE_ANNOUNCEMENT,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final Announcement announcement = this.announcementRepository.getReferenceById(id);

    announcement.setActive(removeAnnouncementForm.removed());

    announcementRepository.save(announcement);

    return conversionService.convert(announcement, AnnouncementResponse.class);
  }

  /**
   * Deletes an announcement based on the given ID and person.
   *
   * @param id     The ID of the announcement to be deleted.
   * @param person The person making the deletion.
   * @return Returns an {@link AnnouncementResponse} object representing the deleted announcement.
   */
  public AnnouncementResponse delete(final Long id, final Person person) {

    rolePermissionService.isPermitted(person,
        RolePermissionInstanceTypes.INSTANCE_DELETE_ANNOUNCEMENT,
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized"));

    final Announcement announcement = this.announcementRepository.getReferenceById(id);

    announcementRepository.delete(announcement);

    return conversionService.convert(announcement, AnnouncementResponse.class);
  }
}
