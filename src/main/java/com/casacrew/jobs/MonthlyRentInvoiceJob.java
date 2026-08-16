package com.casacrew.jobs;

import com.casacrew.model.EmailTemplate;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(value = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class MonthlyRentInvoiceJob {

    private static final Logger log = LoggerFactory.getLogger(MonthlyRentInvoiceJob.class);
    private static final Locale NL = Locale.forLanguageTag("nl-NL");
    private static final DateTimeFormatter MONTH_NL = DateTimeFormatter.ofPattern("MMMM yyyy", NL);
    private static final DateTimeFormatter DATE_NL = DateTimeFormatter.ofPattern("d MMMM yyyy", NL);

    private final UserRepository userRepository;
    private final InvoiceService invoiceService;
    private final MailService mailService;
    private final EmailTemplateService emailTemplateService;
    private final WhatsAppService whatsAppService;
    private final OrganizationRepository organizationRepository;

    @Value("${app.rent.amount:350.00}")
    private BigDecimal rentAmount;

    public MonthlyRentInvoiceJob(UserRepository userRepository,
                                 InvoiceService invoiceService,
                                 MailService mailService,
                                 EmailTemplateService emailTemplateService,
                                 WhatsAppService whatsAppService,
                                 OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.invoiceService = invoiceService;
        this.mailService = mailService;
        this.emailTemplateService = emailTemplateService;
        this.whatsAppService = whatsAppService;
        this.organizationRepository = organizationRepository;
    }

    @Scheduled(cron = "0 0 8 1 * *", zone = "Europe/Amsterdam")
    public void createMonthlyInvoices() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();
        LocalDate dueDate = today.plusDays(7); // due 8th of the month

        String maand = today.format(MONTH_NL);
        String vervaldatum = dueDate.format(DATE_NL);

        List<Organization> organizations = organizationRepository.findAll();
        log.info("MonthlyRentInvoiceJob started (maand={}, organizations={})", maand, organizations.size());

        for (Organization organization : organizations) {
            List<User> students = userRepository.findByOrganization_IdAndRole(organization.getId(), User.Role.STUDENT);
            List<String> adminPhones = adminPhoneNumbers(organization.getId());
            EmailTemplate template = loadTemplate(organization.getId());

            for (User student : students) {
                BigDecimal studentRent = student.getRentAmount() != null ? student.getRentAmount() : rentAmount;
                String studentBedrag = formatBedrag(studentRent);
                processStudent(student, organization, month, year, dueDate, maand, vervaldatum,
                        studentBedrag, studentRent, template, adminPhones);
            }
        }

        log.info("MonthlyRentInvoiceJob finished");
    }

    public void run() {
        createMonthlyInvoices();
    }


    private void processStudent(User student, Organization organization, int month, int year, LocalDate dueDate,
                                String maand, String vervaldatum, String bedragFormatted,
                                BigDecimal studentRent, EmailTemplate template, List<String> adminPhones) {
        try {
            var dto = new com.casacrew.dto.InvoiceRequestDTO();
            dto.setStudentEmail(student.getEmail());
            dto.setTitle("Huur " + maand);
            dto.setDescription("Maandelijkse huur voor " + maand);
            dto.setAmount(studentRent);
            dto.setIssueDate(LocalDate.now());
            dto.setDueDate(dueDate);

            com.casacrew.dto.InvoiceResponseDTO invoiceDTO;
            try {
                invoiceDTO = invoiceService.createInvoiceForOrganization(dto, organization);
            } catch (org.springframework.web.server.ResponseStatusException e) {
                if (e.getStatusCode().value() == 409) {
                    log.info("Invoice already exists for student={} month={}/{}", student.getEmail(), month, year);
                    return;
                }
                throw e;
            }

            Long invoiceId = invoiceDTO.getId();
            String betaalLink = "";

            String naam = student.getUsername();
            if (template != null) {
                String subject = template.renderSubject(naam, bedragFormatted, maand, betaalLink, vervaldatum);
                String body = template.renderBody(naam, bedragFormatted, maand, betaalLink, vervaldatum);
                mailService.sendInvoiceReminderMail(student.getEmail(), subject, body);
                log.info("PAYMENT_NEW email sent to {}", maskEmail(student.getEmail()));
            }

            String betaalInstructie = paymentInstruction(organization, studentRent, maand);
            String waMsg = String.format(
                    "Hallo %s! Je huurrekening van %s voor %s is aangemaakt. " +
                    "Je kunt betalen vóór %s%s" +
                    " Heb je vragen? Neem dan gerust contact op.",
                    naam, bedragFormatted, maand, vervaldatum, betaalInstructie);
            if (student.getPhoneNumber() != null && !student.getPhoneNumber().isBlank()) {
                whatsAppService.send(student.getPhoneNumber(), waMsg);
            }
            whatsAppService.sendToAll(adminPhones,
                    "Huur " + maand + " factuur aangemaakt voor " + naam + " (" + bedragFormatted + ").");

        } catch (Exception e) {
            log.error("Error processing student {} for month={}/{}: {}", maskEmail(student.getEmail()), month, year, e.getMessage(), e);
        }
    }


    private EmailTemplate loadTemplate(Long organizationId) {
        try {
            return emailTemplateService.getByType(organizationId, EmailTemplate.TemplateType.PAYMENT_NEW);
        } catch (Exception e) {
            log.error("Could not load PAYMENT_NEW template for organizationId={}: {}", organizationId, e.getMessage());
            return null;
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

    private String paymentInstruction(Organization organization, BigDecimal amount, String maand) {
        StringBuilder sb = new StringBuilder();
        String iban = organization.getIban();
        if (iban != null && !iban.isBlank()) {
            String holder = organization.getAccountHolderName();
            sb.append(" via overboeking naar ").append(iban);
            if (holder != null && !holder.isBlank()) {
                sb.append(" ten name van ").append(holder);
            }
            sb.append(".");
        } else {
            sb.append(".");
        }
        String bunqLink = buildBunqLink(organization.getBunqMeUsername(), amount, maand);
        if (!bunqLink.isEmpty()) {
            sb.append(" Of betaal via bunq: ").append(bunqLink).append(".");
        }
        return sb.toString();
    }

    String buildBunqLink(String bunqMeUsername, BigDecimal amount, String maand) {
        if (bunqMeUsername == null || bunqMeUsername.isBlank()) return "";
        try {
            String amountStr = amount.stripTrailingZeros().toPlainString();
            String desc = java.net.URLEncoder.encode("Huur " + maand + " CasaCrew",
                    java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
            return "https://bunq.me/" + bunqMeUsername + "/" + amountStr + "/" + desc;
        } catch (Exception e) {
            return "";
        }
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(no-email)";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}