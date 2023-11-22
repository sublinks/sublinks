package com.sublinks.sublinksapi.privatemessages.events;

import com.sublinks.sublinksapi.privatemessages.dto.PrivateMessage;
import com.sublinks.sublinksapi.privatemessages.dto.PrivateMessageReport;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PrivateMessageReportUpdatedEvent extends ApplicationEvent {

  private final PrivateMessageReport privateMessageReport;

  public PrivateMessageReportUpdatedEvent(final Object source, final PrivateMessageReport privateMessageReport) {

    super(source);
    this.privateMessageReport = privateMessageReport;
  }
}
