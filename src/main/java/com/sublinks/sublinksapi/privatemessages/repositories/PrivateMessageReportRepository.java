package com.sublinks.sublinksapi.privatemessages.repositories;

import com.sublinks.sublinksapi.privatemessages.dto.PrivateMessageReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivateMessageReportRepository extends JpaRepository<PrivateMessageReport, Long>,
    PrivateMessageReportSearchRepository {

}
