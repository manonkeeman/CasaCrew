package com.casacrew.controller;

import com.casacrew.model.EmailTemplate;
import com.casacrew.model.Invoice;
import com.casacrew.model.User;
import com.casacrew.repository.InvoiceRepository;
import com.casacrew.repository.UserRepository;
import com.casacrew.service.EmailTemplateService;
import com.casacrew.service.InvoiceService;
import com.casacrew.service.MailService;
import com.casacrew.service.UserService;
import com.casacrew.service.WhatsAppService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/admin/email", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ADMIN')")
public class AdminEmailController {

    private static final Logger log = LoggerFactory.getLogger(AdminEmailController.class);
    private static final Locale NL = Locale.forLanguageTag("nl-NL");
    private static final DateTimeFormatter MONTH_NL = DateTimeFormatter.ofPattern("MMMM yyyy", NL);
    private static final DateTimeFormatter DATE_NL   = DateTimeFormatter.ofPattern("d MMMM yyyy", NL);

    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final MailService mailService;
    private final EmailTemplateService emailTemplateService;
    private final WhatsAppService whatsAppService;
    private final UserService userService;

    public AdminEmailController(UserRepository userRepository,
                                InvoiceRepository invoiceRepository,
                                InvoiceService invoiceService,
                                MailService mailService,
                                EmailTemplateService emailTemplateService,
                                WhatsAppService whatsAppService,
                                UserService userService) {
        this.userRepository      = userRepository;
        this.invoiceRepository   = invoiceRepository;
        this.invoiceService      = invoiceService;
        this.mailService         = mailService;
        this.emailTemplateService = emailTemplateService;
        this.whatsAppService     = whatsAppService;
        this.userService         = userService;
    }

    // POST /api/admin/email/send

    public record SendReminderRequest(Long userId, String templateType) {}

    @PostMapping(value = "/send", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> sendReminder(@RequestBody SendReminderRequest request) {

        Long organizationId = userService.currentOrganizationId();
        User student = userRepository.findById(request.userId())
                .filter(u -> u.getOrganization().getId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Student niet gevonden: id=" + request.userId()));

        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year  = today.getYear();

        List<Invoice> invoices = invoiceRepository.findByStudentAndInvoiceMonthAndInvoiceYear(student, month, year);
        Invoice invoice = invoices.stream()
                .filter(i -> i.getStatus() != Invoice.InvoiceStatus.PAID
                          && i.getStatus() != Invoice.InvoiceStatus.CANCELLED)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Geen openstaande factuur gevonden voor " + student.getUsername()
                        + " in " + month + "/" + year));

        String maand       = LocalDate.of(year, month, 1).format(MONTH_NL);
        String vervaldatum = invoice.getDueDate() != null ? invoice.getDueDate().format(DATE_NL) : "";
        String bedrag      = formatBedrag(invoice.getAmount());
        String betaalLink  = "";

        EmailTemplate.TemplateType templateType = resolveTemplateType(request.templateType());
        EmailTemplate template = loadTemplate(templateType);

        String naam = student.getUsername();
        String subject, body;
        if (template != null) {
            subject = template.renderSubject(naam, bedrag, maand, betaalLink, vervaldatum);
            body    = template.renderBody(naam, bedrag, maand, betaalLink, vervaldatum);
        } else {
            subject = "Herinnering huur " + maand + " voor CasaCrew";
            body    = "Beste " + naam + ",\n\nJe huur van " + bedrag + " voor " + maand
                    + " is nog niet betaald.\n\nBetaal via: " + betaalLink
                    + "\n\nMet vriendelijke groet,\nCasaCrew";
        }

        mailService.sendInvoiceReminderMail(student.getEmail(), subject, body);

        String phone = student.getPhoneNumber();
        if (phone != null && !phone.isBlank()) {
            String waMsg = String.format(
                    "Hallo %s! Je huur van %s voor %s is nog niet betaald. " +
                    "Maak het bedrag over vóór %s naar NL94 INGB 0660 8510 83 ten name van M. Staal. " +
                    "Vragen? Neem gerust contact op.",
                    naam, bedrag, maand, vervaldatum);
            whatsAppService.send(phone, waMsg);
        }

        invoice.setReminderCount(invoice.getReminderCount() + 1);
        invoice.setLastReminderSentAt(LocalDateTime.now());
        invoiceService.saveReminderMeta(invoice);

        log.info("Admin manual reminder ({}) sent to {} for invoiceId={}",
                templateType, maskEmail(student.getEmail()), invoice.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Herinnering verstuurd naar " + naam + " (" + student.getEmail() + ")",
                "invoiceId", String.valueOf(invoice.getId()),
                "betaalLink", betaalLink
        ));
    }

    // Helpers

    private EmailTemplate.TemplateType resolveTemplateType(String raw) {
        if (raw == null) return EmailTemplate.TemplateType.PAYMENT_REMINDER_1;
        try {
            return EmailTemplate.TemplateType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown templateType '{}', defaulting to PAYMENT_REMINDER_1", raw);
            return EmailTemplate.TemplateType.PAYMENT_REMINDER_1;
        }
    }

    private EmailTemplate loadTemplate(EmailTemplate.TemplateType type) {
        try {
            return emailTemplateService.getByTypeInCurrentOrganization(type);
        } catch (Exception e) {
            log.error("Could not load template {}: {}", type, e.getMessage());
            return null;
        }
    }

    private String formatBedrag(java.math.BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(NL).format(amount);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(no-email)";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}
