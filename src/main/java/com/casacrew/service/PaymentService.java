package com.casacrew.service;

import com.casacrew.dto.PaymentRequestDTO;
import com.casacrew.dto.PaymentResponseDTO;
import com.casacrew.model.Payment;
import com.casacrew.model.User;
import com.casacrew.repository.PaymentRepository;
import com.casacrew.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository, UserService userService) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findByOrganization_IdOrderByIdDesc(userService.currentOrganizationId())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsForStudent(String studentEmail) {
        String email = normalizeEmail(studentEmail);

        return paymentRepository.findByOrganization_IdAndStudent_EmailIgnoreCaseOrderByIdDesc(
                        userService.currentOrganizationId(), email)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getOpenPaymentsForStudent(String studentEmail) {
        String email = normalizeEmail(studentEmail);

        List<Payment> open = paymentRepository.findByStudent_EmailIgnoreCaseAndStatusOrderByIdDesc(
                email,
                Payment.PaymentStatus.OPEN
        );

        List<Payment> pending = paymentRepository.findByStudent_EmailIgnoreCaseAndStatusOrderByIdDesc(
                email,
                Payment.PaymentStatus.PENDING
        );

        return concat(open, pending)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(Long id) {
        return toResponseDTO(findInCurrentOrganizationOrThrow(id));
    }

    public PaymentResponseDTO createPayment(PaymentRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("PaymentRequestDTO is required");
        }

        String email = normalizeEmail(dto.getStudentEmail());
        Long organizationId = userService.currentOrganizationId();
        User student = userRepository.findByEmailIgnoreCase(email)
                .filter(u -> u.getOrganization().getId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + email));

        Payment payment = new Payment(
                dto.getAmount(),
                null,
                Payment.PaymentStatus.OPEN,
                dto.getDescription(),
                student
        );
        payment.setOrganization(userService.currentOrganization());

        payment.setStatus(dto.getStatus());

        if (payment.getStatus() == Payment.PaymentStatus.PAID) {
            LocalDateTime paidAt = dto.getPaidAt() != null ? dto.getPaidAt() : LocalDateTime.now();
            payment.setPaidAt(paidAt);
        } else {
            payment.setPaidAt(dto.getPaidAt());
        }

        Payment saved = paymentRepository.save(payment);
        return toResponseDTO(saved);
    }

    public PaymentResponseDTO updateStatus(Long paymentId, String newStatus) {
        Payment payment = findInCurrentOrganizationOrThrow(paymentId);

        payment.setStatus(newStatus);

        if (payment.getStatus() == Payment.PaymentStatus.PAID && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        }

        if (payment.getStatus() != Payment.PaymentStatus.PAID) {
            payment.setPaidAt(null);
        }

        Payment saved = paymentRepository.save(payment);
        return toResponseDTO(saved);
    }

    public void deletePayment(Long id) {
        Payment payment = findInCurrentOrganizationOrThrow(id);
        paymentRepository.delete(payment);
    }

    private Payment findInCurrentOrganizationOrThrow(Long id) {
        Long organizationId = userService.currentOrganizationId();
        return paymentRepository.findById(requireId(id))
                .filter(p -> p.getOrganization().getId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + id));
    }

    private Long requireId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id is required");
        }
        return id;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("studentEmail is required");
        }
        return email.trim().toLowerCase();
    }

    private List<Payment> concat(List<Payment> a, List<Payment> b) {
        if (a.isEmpty()) return b;
        if (b.isEmpty()) return a;
        return java.util.stream.Stream.concat(a.stream(), b.stream()).toList();
    }

    private PaymentResponseDTO toResponseDTO(Payment payment) {
        String studentName = payment.getStudent() != null ? payment.getStudent().getUsername() : null;
        String studentEmail = payment.getStudent() != null ? payment.getStudent().getEmail() : null;

        return new PaymentResponseDTO(
                payment.getId(),
                payment.getAmount(),
                payment.getCreatedAt(),
                payment.getPaidAt(),
                payment.getStatus().name(),
                payment.getDescription(),
                studentName,
                studentEmail
        );
    }
}