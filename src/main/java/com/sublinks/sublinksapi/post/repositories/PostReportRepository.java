package com.sublinks.sublinksapi.post.repositories;

import com.sublinks.sublinksapi.post.dto.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReportRepository extends JpaRepository<PostReport, Long>,
    PostReportRepositorySearch {

}
