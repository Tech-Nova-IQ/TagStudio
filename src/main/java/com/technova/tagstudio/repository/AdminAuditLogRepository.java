package com.technova.tagstudio.repository;

import com.technova.tagstudio.model.AdminAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

    List<AdminAuditLog> findAllByOrderByCreatedAtDesc();

    List<AdminAuditLog> findTop50ByOrderByCreatedAtDesc();
}

