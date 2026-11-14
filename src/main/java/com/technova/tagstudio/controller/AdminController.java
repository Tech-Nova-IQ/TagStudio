package com.technova.tagstudio.controller;

import com.technova.tagstudio.dto.AdminUserViewDTO;
import com.technova.tagstudio.dto.DashboardStatsDTO;
import com.technova.tagstudio.model.AccountStatus;
import com.technova.tagstudio.model.AdminAuditLog;
import com.technova.tagstudio.model.User;
import com.technova.tagstudio.repository.UserRepository;
import com.technova.tagstudio.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;

    public AdminController(AdminService adminService, UserRepository userRepository) {
        this.adminService = adminService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(@RequestParam(value = "query", required = false) String query,
                                     @RequestParam(value = "status", required = false) AccountStatus status,
                                     Authentication authentication,
                                     Model model) {
        DashboardStatsDTO stats = adminService.getDashboardStats();
        List<AdminUserViewDTO> users = adminService.searchUsers(query, status);
        List<AdminAuditLog> auditLogs = adminService.getRecentAuditLogs();

        String currentAdminEmail = authentication != null ? authentication.getName() : "";
        User currentAdmin = userRepository.findByEmailIgnoreCase(currentAdminEmail).orElse(null);

        model.addAttribute("stats", stats);
        model.addAttribute("users", users);
        model.addAttribute("auditLogs", auditLogs);
        model.addAttribute("query", query);
        model.addAttribute("statusFilter", status);
        model.addAttribute("statuses", AccountStatus.values());
        model.addAttribute("currentAdmin", currentAdmin);

        return "admin/dashboard";
    }

    @PostMapping("/users/{id}/activate")
    public String activateUser(@PathVariable("id") Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentAdmin(authentication);
            adminService.activateUser(id, admin);
            redirectAttributes.addFlashAttribute("successMessage", "User account activated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/{id}/disable")
    public String disableUser(@PathVariable("id") Long id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentAdmin(authentication);
            adminService.disableUser(id, admin);
            redirectAttributes.addFlashAttribute("successMessage", "User account disabled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/{id}/lock")
    public String lockUser(@PathVariable("id") Long id,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentAdmin(authentication);
            adminService.lockUser(id, admin);
            redirectAttributes.addFlashAttribute("successMessage", "User account locked successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/{id}/unlock")
    public String unlockUser(@PathVariable("id") Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentAdmin(authentication);
            adminService.unlockUser(id, admin);
            redirectAttributes.addFlashAttribute("successMessage", "User account unlocked successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/{id}/promote")
    public String promoteToAdmin(@PathVariable("id") Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentAdmin(authentication);
            adminService.promoteToAdmin(id, admin);
            redirectAttributes.addFlashAttribute("successMessage", "User successfully promoted to Administrator.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/{id}/demote")
    public String demoteToUser(@PathVariable("id") Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentAdmin(authentication);
            adminService.demoteToUser(id, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Administrator demoted to regular User.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentAdmin(authentication);
            adminService.deleteUser(id, admin);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted permanently.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    private User getCurrentAdmin(Authentication authentication) {
        if (authentication == null) return null;
        return userRepository.findByEmailIgnoreCase(authentication.getName()).orElse(null);
    }
}
