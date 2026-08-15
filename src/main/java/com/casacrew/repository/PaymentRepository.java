package com.casacrew.repository;

import com.casacrew.model.Payment;
import com.casacrew.model.Payment.PaymentStatus;
import com.casacrew.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByOrderByIdDesc();

    List<Payment> findByStudent_EmailIgnoreCaseOrderByIdDesc(String email);

    List<Payment> findByStudent_EmailIgnoreCaseAndStatusOrderByIdDesc(String email, PaymentStatus status);

    List<Payment> findByStatusOrderByIdDesc(PaymentStatus status);

    List<Payment> findByStudent(User student);

    List<Payment> findByOrganization_IdOrderByIdDesc(Long organizationId);

    List<Payment> findByOrganization_IdAndStudent_EmailIgnoreCaseOrderByIdDesc(Long organizationId, String email);
}