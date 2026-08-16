package com.casacrew.service;

import com.casacrew.model.PushSubscription;
import com.casacrew.model.User;
import com.casacrew.repository.PushSubscriptionRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.List;
import java.util.Map;

/**
 * Stuurt pushmeldingen naar alle geregistreerde subscriptions van een
 * gebruiker: Web Push (VAPID) is meteen werkend zodra de VAPID-sleutels
 * geconfigureerd zijn; FCM blijft een no-op (gelogd, geen crash) totdat
 * FIREBASE_SERVICE_ACCOUNT_JSON is ingesteld -- er is nog geen Firebase-
 * project en geen native app die er tokens mee registreert.
 */
@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${vapid.public-key:}")
    private String vapidPublicKey;

    @Value("${vapid.private-key:}")
    private String vapidPrivateKey;

    @Value("${vapid.subject:mailto:admin@casacrew.nl}")
    private String vapidSubject;

    @Value("${firebase.service-account-json:}")
    private String firebaseServiceAccountJson;

    private PushService pushService;
    private boolean firebaseAvailable = false;

    public PushNotificationService(PushSubscriptionRepository pushSubscriptionRepository) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
    }

    @PostConstruct
    void init() {
        Security.addProvider(new BouncyCastleProvider());
        initWebPush();
        initFirebase();
    }

    private void initWebPush() {
        if (vapidPublicKey.isBlank() || vapidPrivateKey.isBlank()) {
            log.warn("VAPID_PUBLIC_KEY/VAPID_PRIVATE_KEY niet geconfigureerd -- Web Push-meldingen zijn uitgeschakeld");
            return;
        }
        try {
            pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
        } catch (Exception e) {
            log.error("Web Push-initialisatie mislukt: {}", e.getMessage());
        }
    }

    private void initFirebase() {
        if (firebaseServiceAccountJson.isBlank()) {
            log.info("FIREBASE_SERVICE_ACCOUNT_JSON niet geconfigureerd -- FCM-meldingen zijn no-ops totdat dit is ingesteld");
            return;
        }
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ByteArrayInputStream(firebaseServiceAccountJson.getBytes(StandardCharsets.UTF_8))))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            firebaseAvailable = true;
        } catch (Exception e) {
            log.error("Firebase-initialisatie mislukt, FCM-meldingen blijven no-ops: {}", e.getMessage());
        }
    }

    public void sendToUser(User user, String title, String body) {
        sendToUsers(List.of(user), title, body);
    }

    public void sendToUsers(List<User> users, String title, String body) {
        if (users == null || users.isEmpty()) {
            return;
        }
        List<PushSubscription> subscriptions = pushSubscriptionRepository.findByUserIn(users);
        for (PushSubscription subscription : subscriptions) {
            try {
                if (subscription.getPlatform() == PushSubscription.Platform.WEB) {
                    sendWebPush(subscription, title, body);
                } else {
                    sendFcm(subscription, title, body);
                }
            } catch (Exception e) {
                log.error("Pushmelding mislukt (subscriptionId={}): {}", subscription.getId(), e.getMessage());
            }
        }
    }

    private void sendWebPush(PushSubscription subscription, String title, String body) throws Exception {
        if (pushService == null) {
            return;
        }

        String payload = objectMapper.writeValueAsString(Map.of("title", title, "body", body));
        Notification notification = new Notification(
                subscription.getEndpoint(),
                subscription.getP256dh(),
                subscription.getAuth(),
                payload.getBytes(StandardCharsets.UTF_8)
        );

        HttpResponse response = pushService.send(notification);
        int status = response.getStatusLine().getStatusCode();
        if (status == 404 || status == 410) {
            log.info("Web Push-subscription verlopen, wordt verwijderd (subscriptionId={})", subscription.getId());
            pushSubscriptionRepository.delete(subscription);
        }
    }

    private void sendFcm(PushSubscription subscription, String title, String body) {
        if (!firebaseAvailable) {
            return;
        }

        Message message = Message.builder()
                .setToken(subscription.getFcmToken())
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                log.info("FCM-token niet meer geregistreerd, wordt verwijderd (subscriptionId={})", subscription.getId());
                pushSubscriptionRepository.delete(subscription);
            } else {
                log.error("FCM-verzending mislukt (subscriptionId={}): {}", subscription.getId(), e.getMessage());
            }
        }
    }
}
