package com.technova.tagstudio.dto;

import java.util.List;

public class DashboardStatsDTO {

    private long totalUsers;
    private long verifiedUsers;
    private long pendingVerifications;
    private long activeUsers;
    private long disabledUsers;
    private long lockedUsers;
    private long adminCount;
    private List<AdminUserViewDTO> recentUsers;

    public DashboardStatsDTO() {
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getVerifiedUsers() {
        return verifiedUsers;
    }

    public void setVerifiedUsers(long verifiedUsers) {
        this.verifiedUsers = verifiedUsers;
    }

    public long getPendingVerifications() {
        return pendingVerifications;
    }

    public void setPendingVerifications(long pendingVerifications) {
        this.pendingVerifications = pendingVerifications;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getDisabledUsers() {
        return disabledUsers;
    }

    public void setDisabledUsers(long disabledUsers) {
        this.disabledUsers = disabledUsers;
    }

    public long getLockedUsers() {
        return lockedUsers;
    }

    public void setLockedUsers(long lockedUsers) {
        this.lockedUsers = lockedUsers;
    }

    public long getAdminCount() {
        return adminCount;
    }

    public void setAdminCount(long adminCount) {
        this.adminCount = adminCount;
    }

    public List<AdminUserViewDTO> getRecentUsers() {
        return recentUsers;
    }

    public void setRecentUsers(List<AdminUserViewDTO> recentUsers) {
        this.recentUsers = recentUsers;
    }
}

