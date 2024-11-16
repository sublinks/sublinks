package com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.controllers;

import com.sublinks.sublinksapi.api.lemmy.v3.authentication.JwtPerson;
import com.sublinks.sublinksapi.api.lemmy.v3.common.controllers.AbstractLemmyApiController;
import com.sublinks.sublinksapi.api.lemmy.v3.errorhandler.ApiError;
import com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.models.CreatePrivateMessage;
import com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.models.CreatePrivateMessageReport;
import com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.models.DeletePrivateMessage;
import com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.models.EditPrivateMessage;
import com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.models.GetPrivateMessages;
import com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.models.ListPrivateMessageReports;
import com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.models.ListPrivateMessageReportsResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.models.MarkPrivateMessageAsRead;
import com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.models.PrivateMessageReportResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.models.PrivateMessageReportView;
import com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.models.PrivateMessageResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.models.PrivateMessageView;
import com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.models.PrivateMessagesResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.models.ResolvePrivateMessageReport;
import com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.services.LemmyPrivateMessageReportService;
import com.sublinks.sublinksapi.api.lemmy.v3.privatemessage.services.LemmyPrivateMessageService;
import com.sublinks.sublinksapi.api.lemmy.v3.utils.PaginationControllerUtils;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionInstanceTypes;
import com.sublinks.sublinksapi.authorization.enums.RolePermissionPrivateMessageTypes;
import com.sublinks.sublinksapi.authorization.services.RolePermissionService;
import com.sublinks.sublinksapi.person.entities.Person;
import com.sublinks.sublinksapi.person.repositories.PersonRepository;
import com.sublinks.sublinksapi.privatemessages.entities.PrivateMessage;
import com.sublinks.sublinksapi.privatemessages.entities.PrivateMessageReport;
import com.sublinks.sublinksapi.privatemessages.enums.PrivateMessageSortType;
import com.sublinks.sublinksapi.privatemessages.models.PrivateMessageReportSearchCriteria;
import com.sublinks.sublinksapi.privatemessages.models.PrivateMessageSearchCriteria;
import com.sublinks.sublinksapi.privatemessages.repositories.PrivateMessageReportRepository;
import com.sublinks.sublinksapi.privatemessages.repositories.PrivateMessageRepository;
import com.sublinks.sublinksapi.privatemessages.services.PrivateMessageReportService;
import com.sublinks.sublinksapi.privatemessages.services.PrivateMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v3/private_message")
@Tag(name = "PrivateMessage")
public class PrivateMessageController extends AbstractLemmyApiController {

  private final PrivateMessageService privateMessageService;
  private final PrivateMessageRepository privateMessageRepository;
  private final LemmyPrivateMessageService lemmyPrivateMessageService;
  private final PersonRepository personRepository;
  private final LemmyPrivateMessageReportService lemmyPrivateMessageReportService;
  private final PrivateMessageReportService privateMessageReportService;
  private final PrivateMessageReportRepository privateMessageReportRepository;
  private final RolePermissionService rolePermissionService;

  @Operation(summary = "Get / fetch private messages.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PrivateMessagesResponse.class))})})
  @GetMapping("list")
  PrivateMessagesResponse list(@Valid final GetPrivateMessages getPrivateMessagesForm,
      final JwtPerson principal)
  {

    final Person sender = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(sender,
        RolePermissionPrivateMessageTypes.READ_PRIVATE_MESSAGES,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    // @todo: add support for other sort types
    final PrivateMessageSortType sortType = PrivateMessageSortType.New;

    final int page = PaginationControllerUtils.getAbsoluteMinNumber(getPrivateMessagesForm.page(),
        1);
    final int perPage = PaginationControllerUtils.getAbsoluteMinNumber(
        getPrivateMessagesForm.limit(), 20);

    final PrivateMessageSearchCriteria privateMessageSearchCriteria = PrivateMessageSearchCriteria.builder()
        .page(page)
        .perPage(perPage)
        .privateMessageSortType(sortType)
        .person(sender)
        .unreadOnly(
            getPrivateMessagesForm.unread_only() != null && getPrivateMessagesForm.unread_only())
        .build();

    final List<PrivateMessage> privateMessages = privateMessageRepository.allPrivateMessagesBySearchCriteria(
        privateMessageSearchCriteria);
    final List<PrivateMessageView> privateMessageViews = new ArrayList<>();
    privateMessages.forEach(privateMessage -> privateMessageViews.add(
        lemmyPrivateMessageService.createPrivateMessageView(privateMessage)));

    return PrivateMessagesResponse.builder()
        .private_messages(privateMessageViews)
        .build();
  }

  @Operation(summary = "Create a private message.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PrivateMessageResponse.class))}), @ApiResponse(
      responseCode = "404",
      description = "Recipient Not Found",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ApiError.class))})})
  @PostMapping
  PrivateMessageResponse create(
      @Valid @RequestBody final CreatePrivateMessage createPrivateMessageForm,
      final JwtPerson principal)
  {

    final Person sender = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(sender,
        RolePermissionPrivateMessageTypes.CREATE_PRIVATE_MESSAGE,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    final Person recipient = personRepository.findById(
            (long) createPrivateMessageForm.recipient_id())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "recipient_not_found"));

    final PrivateMessage privateMessage = PrivateMessage.builder()
        .content(createPrivateMessageForm.content())
        .sender(sender)
        .recipient(recipient)
        .isLocal(recipient.isLocal())
        .build();
    privateMessageService.createPrivateMessage(privateMessage);

    final PrivateMessageView privateMessageView = lemmyPrivateMessageService.createPrivateMessageView(
        privateMessage);

    return PrivateMessageResponse.builder()
        .private_message_view(privateMessageView)
        .build();
  }

  @Operation(summary = "Edit a private message.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PrivateMessageResponse.class))}), @ApiResponse(
      responseCode = "404",
      description = "Private Message Not Found",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ApiError.class))})})
  @PutMapping
  PrivateMessageResponse update(@Valid @RequestBody final EditPrivateMessage editPrivateMessageForm,
      final JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person,
        RolePermissionPrivateMessageTypes.UPDATE_PRIVATE_MESSAGE,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    PrivateMessage privateMessage = privateMessageRepository.findById(
            (long) editPrivateMessageForm.private_message_id())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "private_message_not_found"));

    privateMessage.setContent(editPrivateMessageForm.content());

    privateMessageService.updatePrivateMessage(privateMessage);

    final PrivateMessageView privateMessageView = lemmyPrivateMessageService.createPrivateMessageView(
        privateMessage);
    return PrivateMessageResponse.builder()
        .private_message_view(privateMessageView)
        .build();
  }

  @Operation(summary = "Delete a private message.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PrivateMessageResponse.class))}), @ApiResponse(
      responseCode = "404",
      description = "Private Message Not Found",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ApiError.class))})})
  @PostMapping("delete")
  PrivateMessageResponse delete(
      @Valid @RequestBody final DeletePrivateMessage deletePrivateMessageForm,
      final JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person,
        RolePermissionPrivateMessageTypes.DELETE_PRIVATE_MESSAGE,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    PrivateMessage privateMessage = privateMessageRepository.findById(
            (long) deletePrivateMessageForm.private_message_id())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "private_message_not_found"));

    privateMessageService.deletePrivateMessage(privateMessage);

    final PrivateMessageView privateMessageView = lemmyPrivateMessageService.createPrivateMessageView(
        privateMessage);
    return PrivateMessageResponse.builder()
        .private_message_view(privateMessageView)
        .build();
  }

  @Operation(summary = "Mark a private message as read.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PrivateMessageResponse.class))}), @ApiResponse(
      responseCode = "404",
      description = "Private Message Not Found",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ApiError.class))})})
  @PostMapping("mark_as_read")
  PrivateMessageResponse markAsRead(
      @Valid @RequestBody MarkPrivateMessageAsRead markPrivateMessageAsReadForm,
      final JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person,
        RolePermissionPrivateMessageTypes.MARK_PRIVATE_MESSAGE_AS_READ,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    PrivateMessage privateMessage = privateMessageRepository.findById(
            (long) markPrivateMessageAsReadForm.private_message_id())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "private_message_not_found"));

    privateMessage.setRead(markPrivateMessageAsReadForm.read());

    privateMessageService.updatePrivateMessage(privateMessage);

    final PrivateMessageView privateMessageView = lemmyPrivateMessageService.createPrivateMessageView(
        privateMessage);
    return PrivateMessageResponse.builder()
        .private_message_view(privateMessageView)
        .build();
  }

  @Operation(summary = "Create a report for a private message.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PrivateMessageReportResponse.class))})})
  @PostMapping("report")
  PrivateMessageReportResponse report(
      @Valid @RequestBody final CreatePrivateMessageReport privateMessageReportForm,
      final JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person,
        RolePermissionPrivateMessageTypes.REPORT_PRIVATE_MESSAGE,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    final PrivateMessage privateMessage = privateMessageRepository.findById(
            (long) privateMessageReportForm.private_message_id())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "private_message_not_found"));

    final PrivateMessageReport privateMessageReport = PrivateMessageReport.builder()
        .originalContent(privateMessage.getContent())
        .reason(privateMessageReportForm.reason())
        .privateMessage(privateMessage)
        .creator(person)
        .build();

    privateMessageReportService.createPrivateMessageReport(privateMessageReport);

    final PrivateMessageReportView privateMessageReportView = lemmyPrivateMessageReportService.createPrivateMessageReportView(
        privateMessageReport);
    return PrivateMessageReportResponse.builder()
        .private_message_report_view(privateMessageReportView)
        .build();
  }

  @Operation(summary = "Resolve a report for a private message.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = PrivateMessageReportResponse.class))})})
  @PutMapping("report/resolve")
  PrivateMessageReportResponse reportResolve(
      @Valid @RequestBody ResolvePrivateMessageReport privateMessageReportForm,
      final JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionInstanceTypes.REPORT_INSTANCE_RESOLVE,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    final PrivateMessageReport privateMessageReport = privateMessageReportRepository.findById(
            (long) privateMessageReportForm.report_id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "private_message_report_not_found"));

    privateMessageReport.setResolved(privateMessageReportForm.resolved());
    privateMessageReport.setResolver(person);
    privateMessageReportService.updatePrivateMessage(privateMessageReport);

    final PrivateMessageReportView privateMessageReportView = lemmyPrivateMessageReportService.createPrivateMessageReportView(
        privateMessageReport);
    return PrivateMessageReportResponse.builder()
        .private_message_report_view(privateMessageReportView)
        .build();
  }

  @Operation(summary = "List private message reports.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200",
      description = "OK",
      content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ListPrivateMessageReportsResponse.class))})})
  @GetMapping("report/list")
  ListPrivateMessageReportsResponse reportList(
      @Valid final ListPrivateMessageReports listPrivateMessageReportsForm,
      final JwtPerson principal)
  {

    final Person person = getPersonOrThrowUnauthorized(principal);

    rolePermissionService.isPermitted(person, RolePermissionInstanceTypes.REPORT_INSTANCE_READ,
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));

    final int page = PaginationControllerUtils.getAbsoluteMinNumber(
        listPrivateMessageReportsForm.page(), 1);
    final int perPage = PaginationControllerUtils.getAbsoluteMinNumber(
        listPrivateMessageReportsForm.limit(), 20);

    final List<PrivateMessageReport> privateMessageReports = privateMessageReportRepository.allPrivateMessageReportsBySearchCriteria(
        PrivateMessageReportSearchCriteria.builder()
            .page(page)
            .perPage(perPage)
            .unresolvedOnly(listPrivateMessageReportsForm.unresolved_only() != null
                && listPrivateMessageReportsForm.unresolved_only())
            .build());

    final List<PrivateMessageReportView> privateMessageReportViews = new ArrayList<>();
    privateMessageReports.forEach(privateMessageReport -> privateMessageReportViews.add(
        lemmyPrivateMessageReportService.createPrivateMessageReportView(privateMessageReport)));

    return ListPrivateMessageReportsResponse.builder()
        .private_message_reports(privateMessageReportViews)
        .build();
  }
}
