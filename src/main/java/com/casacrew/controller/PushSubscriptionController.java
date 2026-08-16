package com.casacrew.controller;

import com.casacrew.dto.FcmSubscriptionRequestDTO;
import com.casacrew.dto.PushUnsubscribeRequestDTO;
import com.casacrew.dto.WebPushSubscriptionRequestDTO;
import com.casacrew.model.PushSubscription;
import com.casacrew.model.User;
import com.casacrew.repository.PushSubscriptionRepository;
import com.casacrew.repository.UserRepository;
import com.casacrew.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/api/push/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
@Transactional
public class PushSubscriptionController {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public PushSubscriptionController(PushSubscriptionRepository pushSubscriptionRepository,
                                      UserRepository userRepository,
                                      UserService userService) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PostMapping(value = "/web", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> subscribeWeb(@Valid @RequestBody WebPushSubscriptionRequestDTO request) {
        User me = currentUser();

        PushSubscription subscription = pushSubscriptionRepository.findByUserAndEndpoint(me, request.endpoint())
                .orElseGet(() -> PushSubscription.forWebPush(me, me.getOrganization(), request.endpoint(),
                        request.keys().p256dh(), request.keys().auth()));

        pushSubscriptionRepository.save(subscription);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(value = "/fcm", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> subscribeFcm(@Valid @RequestBody FcmSubscriptionRequestDTO request) {
        User me = currentUser();

        PushSubscription subscription = pushSubscriptionRepository.findByUserAndFcmToken(me, request.token())
                .orElseGet(() -> PushSubscription.forFcm(me, me.getOrganization(), request.token()));

        pushSubscriptionRepository.save(subscription);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> unsubscribe(@RequestBody PushUnsubscribeRequestDTO request) {
        User me = currentUser();
        if (request.endpoint() != null && !request.endpoint().isBlank()) {
            pushSubscriptionRepository.deleteByUserAndEndpoint(me, request.endpoint());
        }
        if (request.token() != null && !request.token().isBlank()) {
            pushSubscriptionRepository.deleteByUserAndFcmToken(me, request.token());
        }
        return ResponseEntity.noContent().build();
    }

    private User currentUser() {
        Long id = userService.getMyId();
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Gebruiker niet gevonden."));
    }
}
