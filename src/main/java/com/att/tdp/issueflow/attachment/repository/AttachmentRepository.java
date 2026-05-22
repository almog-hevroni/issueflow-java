package com.att.tdp.issueflow.attachment.repository;

import com.att.tdp.issueflow.attachment.entity.Attachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

	List<Attachment> findAllByTicket_IdOrderByCreatedAtDesc(Long ticketId);
}
