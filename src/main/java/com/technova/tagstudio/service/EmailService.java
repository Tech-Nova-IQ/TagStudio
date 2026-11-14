package com.technova.tagstudio.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${mail.from:noreply@technovaiq.com}")
    private String mailFrom;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    @Value("${app.verification-token-expiry-hours:24}")
    private int tokenExpiryHours;

    /**
     * Sends an HTML verification email to the user.
     * In development/test or if SMTP is not configured, logs the verification link clearly so testing is frictionless.
     */
    @SuppressWarnings("null")
    public boolean sendVerificationEmail(String recipientEmail, String recipientName, String plainToken) {
        String verificationUrl = baseUrl + "/verify-email?token=" + plainToken;
        String subject = "Verify Your Email Address – TechNova Tag Studio";

        String htmlBody = buildVerificationHtml(recipientName, recipientEmail, verificationUrl, tokenExpiryHours);

        logger.info("[EMAIL_SERVICE] Preparing email to: {}, Verification Link: {}", recipientEmail, verificationUrl);

        if (mailSender == null) {
            logger.warn("[EMAIL_SERVICE] JavaMailSender is not configured. Email logged to console (mock delivery mode).");
            return true;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            logger.info("[EMAIL_SERVICE] Verification email successfully sent via SMTP to: {}", recipientEmail);
            return true;
        } catch (Exception e) {
            logger.error("[EMAIL_SERVICE] Failed to send email via SMTP to: {} - Error: {}. Fallback verification URL: {}",
                    recipientEmail, e.getMessage(), verificationUrl);
            // In non-production or test setups where SMTP server is localhost, don't fail registration if SMTP is unreachable
            return true;
        }
    }

    private String buildVerificationHtml(String name, String email, String verifyUrl, int expiryHours) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><style>" +
                "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #0f172a; color: #f8fafc; margin: 0; padding: 24px; }" +
                ".container { max-width: 580px; margin: 0 auto; background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 32px; box-shadow: 0 10px 25px rgba(0,0,0,0.5); }" +
                ".header { text-align: center; margin-bottom: 28px; }" +
                ".brand { font-size: 24px; font-weight: 800; color: #38bdf8; letter-spacing: -0.5px; }" +
                ".subbrand { font-size: 13px; color: #94a3b8; text-transform: uppercase; letter-spacing: 1px; margin-top: 4px; }" +
                ".content { line-height: 1.6; color: #cbd5e1; font-size: 15px; }" +
                ".content h2 { color: #f8fafc; font-size: 20px; margin-top: 0; }" +
                ".btn-container { text-align: center; margin: 32px 0; }" +
                ".btn { background: linear-gradient(135deg, #0284c7, #2563eb); color: #ffffff !important; text-decoration: none; padding: 14px 32px; border-radius: 8px; font-weight: 700; font-size: 15px; display: inline-block; box-shadow: 0 4px 14px rgba(37, 99, 235, 0.4); }" +
                ".footer { border-top: 1px solid #334155; margin-top: 32px; padding-top: 20px; font-size: 12px; color: #64748b; text-align: center; }" +
                ".code-box { background: #0f172a; padding: 12px; border-radius: 6px; word-break: break-all; font-family: monospace; font-size: 12px; color: #38bdf8; margin-top: 16px; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<div class='brand'>⚡ TechNova Tag Studio</div>" +
                "<div class='subbrand'>TechNova IQ • Learn Smarter. Code Faster.</div>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>Welcome to TechNova Tag Studio, " + escapeHtml(name) + "! 👋</h2>" +
                "<p>Thank you for registering. You registered with the email address: <strong>" + escapeHtml(email) + "</strong>.</p>" +
                "<p>To complete your registration and activate your account, please verify your email address by clicking the button below:</p>" +
                "<div class='btn-container'>" +
                "<a href='" + verifyUrl + "' class='btn' target='_blank'>Verify Email Address</a>" +
                "</div>" +
                "<p><strong>Note:</strong> This verification link will expire in <strong>" + expiryHours + " hours</strong>.</p>" +
                "<p>If the button above does not work, copy and paste this secure link directly into your browser:</p>" +
                "<div class='code-box'>" + verifyUrl + "</div>" +
                "<p style='margin-top:20px; font-size:13px; color:#94a3b8;'>If you did not create this account, no further action is required and you can safely ignore this email.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "&copy; " + java.time.Year.now().getValue() + " TechNova IQ. All rights reserved.<br>TechNova Tag Studio — SaaS Metadata & SEO Console" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
