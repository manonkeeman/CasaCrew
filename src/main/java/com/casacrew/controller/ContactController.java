package com.casacrew.controller;

import com.casacrew.dto.ContactRequestDTO;
import com.casacrew.service.MailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(value = "/api/contact", produces = MediaType.APPLICATION_JSON_VALUE)
public class ContactController {

    private final MailService mailService;
    private final String contactToEmail;

    public ContactController(
            MailService mailService,
            @Value("${app.contact.to-email:info@casacrew.nl}") String contactToEmail
    ) {
        this.mailService = mailService;
        this.contactToEmail = contactToEmail;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> submitContactForm(@Valid @RequestBody ContactRequestDTO request) {
        String subject = "Nieuw contactformulier van " + request.name();
        String body = "Naam: " + request.name() + "\n"
                + "E-mail: " + request.email() + "\n\n"
                + request.message();

        mailService.sendMailWithRole("ADMIN", contactToEmail, subject, body);

        return ResponseEntity.noContent().build();
    }
}
