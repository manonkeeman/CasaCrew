package com.casacrew.jobs;

import com.casacrew.model.Invoice;
import com.casacrew.model.Organization;
import com.casacrew.model.User;
import com.casacrew.repository.InvoiceRepository;
import com.casacrew.repository.OrganizationRepository;
import com.casacrew.repository.UserRepository;
import com.casacrew.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(value = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class BunqPaymentReminderJob {

    private static final Logger log = LoggerFactory.getLogger(BunqPaymentReminderJob.class);
    private static final Locale NL = Locale.forLanguageTag("nl-NL");
    private static final DateTimeFormatter MONTH_NL = DateTimeFormatter.ofPattern("MMMM yyyy", NL);
    private static final DateTimeFormatter DATE_NL = DateTimeFormatter.ofPattern("d MMMM yyyy", NL);

    private final InvoiceRepository invoiceRepository;
    private final WhatsAppService whatsAppService;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public BunqPaymentReminderJob(InvoiceRepository invoiceRepository,
                                  WhatsAppService whatsAppService,
                                  OrganizationRepository organizationRepository,
                                  UserRepository userRepository) {
        this.invoiceRepository = invoiceRepository;
        this.whatsAppService = whatsAppService;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 9 6 * *", zone = "Europe/Amsterdam")
    public void sendFirstReminder() {
        sendReminders(1);
    }

    @Scheduled(cron = "0 0 9 11 * *", zone = "Europe/Amsterdam")
    public void sendSecondReminder() {
        sendReminders(2);
    }

    private void sendReminders(int reminderNumber) {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();
        String maand = today.withDayOfMonth(1).format(MONTH_NL);

        List<Organization> organizations = organizationRepository.findAll();
        log.info("BunqPaymentReminderJob reminder={} maand={} organizations={}", reminderNumber, maand, organizations.size());

        for (Organization organization : organizations) {
            List<Invoice> openInvoices = invoiceRepository.findByOrganization_IdAndInvoiceMonthAndInvoiceYearAndStatusNotIn(
                    organization.getId(), month, year, List.of(Invoice.InvoiceStatus.PAID));
            List<String> adminPhones = adminPhoneNumbers(organization.getId());

            for (Invoice invoice : openInvoices) {
                sendReminder(invoice, organization, reminderNumber, maand, adminPhones);
            }
        }
    }

    private void sendReminder(Invoice invoice, Organization organization, int reminderNumber, String maand, List<String> adminPhones) {
        try {
            var student = invoice.getStudent();
            if (student == null) return;

            String phone = student.getPhoneNumber();
            if (phone == null || phone.isBlank()) return;

            String naam = student.getUsername();
            String bedrag = formatBedrag(invoice.getAmount());
            String vervaldatum = invoice.getDueDate() != null
                    ? invoice.getDueDate().format(DATE_NL)
                    : "zo snel mogelijk";

            String betaalInstructie = paymentInstruction(organization, invoice.getAmount(), maand);
            String waMsg = String.format(
                    "Hallo %s! Dit is herinnering %d voor je huur van %s voor %s. " +
                    "De betaling staat nog open. Maak het bedrag over vóór %s%s " +
                    "Heb je al betaald? Dan kun je dit bericht negeren.",
                    naam, reminderNumber, bedrag, maand, vervaldatum, betaalInstructie);

            whatsAppService.send(phone, waMsg);
            whatsAppService.sendToAll(adminPhones, "Bunq herinnering " + reminderNumber + " verstuurd aan "
                    + naam + " voor huur " + maand + " (" + bedrag + ").");

            log.info("BunqPaymentReminderJob reminder={} sent to student={}", reminderNumber, student.getId());
        } catch (Exception e) {
            log.error("BunqPaymentReminderJob failed for invoiceId={}: {}", invoice.getId(), e.getMessage());
        }
    }

    private String paymentInstruction(Organization organization, BigDecimal amount, String maand) {
        StringBuilder sb = new StringBuilder();
        String iban = organization.getIban();
        if (iban != null && !iban.isBlank()) {
            String holder = organization.getAccountHolderName();
            sb.append(" naar ").append(iban);
            if (holder != null && !holder.isBlank()) {
                sb.append(" ten name van ").append(holder);
            }
            sb.append(".");
        } else {
            sb.append(".");
        }
        String bunqLink = buildBunqLink(organization.getBunqMeUsername(), amount, maand);
        if (!bunqLink.isEmpty()) {
            sb.append(" Of betaal direct via bunq: ").append(bunqLink).append(".");
        }
        return sb.toString();
    }

    private String buildBunqLink(String bunqMeUsername, BigDecimal amount, String maand) {
        if (bunqMeUsername == null || bunqMeUsername.isBlank()) return "";
        try {
            String amountStr = amount.stripTrailingZeros().toPlainString();
            String desc = URLEncoder.encode("Huur " + maand + " CasaCrew", StandardCharsets.UTF_8)
                    .replace("+", "%20");
            return "https://bunq.me/" + bunqMeUsername + "/" + amountStr + "/" + desc;
        } catch (Exception e) {
            return "";
        }
    }

    private List<String> adminPhoneNumbers(Long organizationId) {
        return userRepository.findByOrganization_IdAndRole(organizationId, User.Role.ADMIN)
                .stream()
                .map(User::getPhoneNumber)
                .filter(phone -> phone != null && !phone.isBlank())
                .toList();
    }

    private String formatBedrag(BigDecimal amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(NL);
        return nf.format(amount);
    }
}
