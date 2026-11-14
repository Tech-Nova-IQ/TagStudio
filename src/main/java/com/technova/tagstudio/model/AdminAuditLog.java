package com.technova.tagstudio.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_audit_logs", indexes = {
        @Index(name = "idx_audit_admin", columnList = "administrator_id"),
        @Index(name = "idx_audit_created", columnList = "created_at")
})
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "administrator_id", nullable = false)
    private Long administratorId;

    @Column(name = "administrator_email", nullable = false, length = 180)
    private String administratorEmail;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "target_user_email", length = 180)
    private String targetUserEmail;

    @Column(nullable = false, length = 1000)
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public AdminAuditLog() {
    }

    public AdminAuditLog(Long administratorId, String administratorEmail, String action, Long targetUserId, String targetUserEmail, String details) {
        this.administratorId = administratorId;
        this.administratorEmail = administratorEmail;
        this.action = action;
        this.targetUserId = targetUserId;
        this.targetUserEmail = targetUserEmail;
        this.details = details;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAdministratorId() {
        return administratorId;
    }

    public void setAdministratorId(Long administratorId) {
        this.administratorId = administratorId;
    }

    public String getAdministratorEmail() {
        return administratorEmail;
    }

    public void setAdministratorEmail(String administratorEmail) {
        this.administratorEmail = administratorEmail;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUserEmail() {
        return targetUserEmail;
    }

    public void setTargetUserEmail(String targetUserEmail) {
        this.targetUserEmail = targetUserEmail;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

