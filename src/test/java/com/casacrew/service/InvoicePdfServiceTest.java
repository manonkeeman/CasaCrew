package com.casacrew.service;

import com.casacrew.model.Invoice;
import com.casacrew.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvoicePdfServiceTest {

    private final InvoicePdfService service = new InvoicePdfService();

    private User makeStudent(String username, String email) {
        User user = new User(username, email, "hash", User.Role.STUDENT);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private Invoice makeInvoice(User student, String description) {
        Invoice invoice = new Invoice("Huur maart", description, new BigDecimal("350.00"),
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), 3, 2025, Invoice.InvoiceStatus.OPEN, student);
        ReflectionTestUtils.setField(invoice, "id", 42L);
        return invoice;
    }

    @Test
    void generate_validInvoice_returnsNonEmptyPdfBytes() {
        Invoice invoice = makeInvoice(makeStudent("student", "student@test.com"), "Maandhuur");

        byte[] pdf = service.generate(invoice);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    @Test
    void generate_withoutDescription_returnsNonEmptyPdfBytes() {
        Invoice invoice = makeInvoice(makeStudent("student", "student@test.com"), null);

        byte[] pdf = service.generate(invoice);

        assertThat(pdf).isNotEmpty();
    }

    @Test
    void generate_invoiceWithoutStudent_throwsRuntimeException() {
        Invoice invoice = makeInvoice(null, "Maandhuur");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.generate(invoice));
        assertThat(ex.getMessage()).isEqualTo("PDF generatie mislukt");
        assertThat(ex.getCause()).isInstanceOf(NullPointerException.class);
    }
}
