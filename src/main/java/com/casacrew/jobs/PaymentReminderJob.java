package com.casacrew.jobs;

import com.casacrew.model.EmailTemplate;
import com.casacrew.model.Invoice;
import com.casacrew.model.Organization;
import com.casacrew.model.User;
import com.casacrew.repository.OrganizationRepository;
import com.casacrew.repository.UserRepository;
import com.casacrew.service.EmailTemplateService;
import com.casacrew.service.InvoiceService;
import com.casacrew.service.MailService;
import com.casacrew.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(value = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentReminderJob {

    private static final Logger log = LoggerFactory.getLogger(PaymentReminderJob.class);
    private static final Locale NL = Locale.forLanguageTag("nl-NL");
    private static final DateTimeFormatter MONTH_NL = DateTimeFormatter.ofPattern("MMMM yyyy", NL);
    private static final DateTimeFormatter DATE_NL = DateTimeFormatter.ofPattern("d MMMM yyyy", NL);

    private final InvoiceService invoiceService;
    private final MailService mailService;
    private final EmailTemplateService emailTemplateService;
    private final WhatsAppService whatsAppService;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public PaymentReminderJob(InvoiceService invoiceService,
                              MailService mailService,
                              EmailTemplateService emailTemplateService,
                              WhatsAppService whatsAppService,
                              OrganizationRepository organizationRepository,
                              UserRepository userRepository) {
        this.invoiceService = invoiceService;
        this.mailService = mailService;
        this.emailTemplateService = emailTemplateService;
        this.whatsAppService = whatsAppService;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }


    @Transactional
    @Scheduled(cron = "0 0 9 3 * *", zone = "Europe/Amsterdam")
    public void sendFirstReminders() {
        sendReminders(EmailTemplate.TemplateType.PAYMENT_REMINDER_1, 1);
    }

    @Transactional
    public void triggerFirstReminder() {
        sendReminders(EmailTemplate.TemplateType.PAYMENT_REMINDER_1, 1);
    }


    @Transactional
    @Scheduled(cron = "0 0 9 7 * *", zone = "Europe/Amsterdam")
    public void sendSecondReminders() {
        sendReminders(EmailTemplate.TemplateType.PAYMENT_REMINDER_2, 2);
    }

    @Transactional
    public void triggerSecondReminder() {
        sendReminders(EmailTemplate.TemplateType.PAYMENT_REMINDER_2, 2);
    }


    private void sendReminders(EmailTemplate.TemplateType templateType, int reminderNumber) {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        List<Organization> organizations = organizationRepository.findAll();
        log.info("PaymentReminderJob [{}] started (organizations={}, month={}/{})",
                templateType, organizations.size(), month, year);

        int totalProcessed = 0;
        for (Organization organization : organizations) {
            List<Invoice> unpaid = invoiceService.getUnpaidForMonth(organization.getId(), month, year);
            EmailTemplate template = loadTemplate(organization.getId(), templateType);
            List<String> adminPhones = adminPhoneNumbers(organization.getId());

            for (Invoice invoice : unpaid) {
                sendReminder(invoice, template, reminderNumber, organization, adminPhones);
            }
            totalProcessed += unpaid.size();
        }

        log.info("PaymentReminderJob [{}] finished ({} invoices processed)", templateType, totalProcessed);
    }

    private void sendReminder(Invoice invoice, EmailTemplate template, int reminderNumber,
                              Organization organization, List<String> adminPhones) {
        try {
            String email = invoice.getStudent().getEmail();
            String naam = invoice.getStudent().getUsername();
            String maand = LocalDate.of(invoice.getInvoiceYear(), invoice.getInvoiceMonth(), 1).format(MONTH_NL);
            String bedrag = formatBedrag(invoice.getAmount());
            String betaalLink = "";
            String vervaldatum = invoice.getDueDate() != null ? invoice.getDueDate().format(DATE_NL) : "";

            String subject, body;
            if (template != null) {
                subject = template.renderSubject(naam, bedrag, maand, betaalLink, vervaldatum);
                body = template.renderBody(naam, bedrag, maand, betaalLink, vervaldatum);
            } else {
                subject = "Herinnering huur " + maand + " voor CasaCrew";
                body = "Beste " + naam + ",\n\nJe huur van " + bedrag + " voor " + maand + " is nog niet betaald.\n\nMet vriendelijke groet,\nCasaCrew";
            }

            mailService.sendInvoiceReminderMail(email, subject, body);

            String phone = invoice.getStudent().getPhoneNumber();
            if (phone != null && !phone.isBlank()) {
                String betaalInstructie = paymentInstruction(organization);
                String waMsg = String.format(
                        "Hallo %s! Dit is herinnering %d voor je huur van %s voor %s. De huur is nog niet betaald. " +
                        "Maak het bedrag over vóór %s%s " +
                        "Heb je vragen? Neem dan gerust contact op.",
                        naam, reminderNumber, bedrag, maand, vervaldatum, betaalInstructie);
                whatsAppService.send(phone, waMsg);
            }
            whatsAppService.sendToAll(adminPhones,
                    "🔔 Herinnering " + reminderNumber + " verstuurd aan " + naam + " voor huur " + maand + " (" + bedrag + ").");

            invoice.setReminderCount(invoice.getReminderCount() + 1);
            invoice.setLastReminderSentAt(LocalDateTime.now());
            invoiceService.saveReminderMeta(invoice);

            log.info("Reminder {} sent for invoiceId={} to {}", reminderNumber, invoice.getId(), maskEmail(email));

        } catch (Exception e) {
            log.error("Failed to send reminder {} for invoiceId={}: {}", reminderNumber, invoice.getId(), e.getMessage());
        }
    }

    private String paymentInstruction(Organization organization) {
        String iban = organization.getIban();
        if (iban == null || iban.isBlank()) {
            return ".";
        }
        String holder = organization.getAccountHolderName();
        return holder != null && !holder.isBlank()
                ? " naar " + iban + " ten name van " + holder + "."
                : " naar " + iban + ".";
    }

    private List<String> adminPhoneNumbers(Long organizationId) {
        return userRepository.findByOrganization_IdAndRole(organizationId, User.Role.ADMIN)
                .stream()
                .map(User::getPhoneNumber)
                .filter(phone -> phone != null && !phone.isBlank())
                .toList();
    }

    private EmailTemplate loadTemplate(Long organizationId, EmailTemplate.TemplateType type) {
        try {
            return emailTemplateService.getByType(organizationId, type);
        } catch (Exception e) {
            log.error("Could not load template {} for organizationId={}: {}", type, organizationId, e.getMessage());
            return null;
        }
    }

    private String formatBedrag(java.math.BigDecimal amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(NL);
        return nf.format(amount);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(no-email)";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}