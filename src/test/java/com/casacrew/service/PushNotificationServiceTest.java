package com.casacrew.service;

import com.casacrew.model.Organization;
import com.casacrew.model.PushSubscription;
import com.casacrew.model.User;
import com.casacrew.repository.PushSubscriptionRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

    @Mock PushSubscriptionRepository pushSubscriptionRepository;
    @Mock PushService pushService;

    PushNotificationService service;

    @BeforeAll
    static void registerBouncyCastle() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @BeforeEach
    void setUp() {
        service = new PushNotificationService(pushSubscriptionRepository);
    }

    /** A real EC public key on the P-256 curve, base64url-encoded like a genuine VAPID p256dh value. */
    private static String validP256dh() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", "BC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair keyPair = generator.generateKeyPair();
        byte[] encoded = nl.martijndwars.webpush.Utils.encode((ECPublicKey) keyPair.getPublic());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encoded);
    }

    private static String validAuthSecret() {
        byte[] auth = new byte[16];
        new SecureRandom().nextBytes(auth);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(auth);
    }

    private Organization makeOrganization() {
        Organization organization = new Organization("CasaCrew", "casacrew");
        ReflectionTestUtils.setField(organization, "id", 1L);
        return organization;
    }

    private User makeUser(long id, String username, String email) {
        User user = new User(username, email, "hash", User.Role.STUDENT);
        user.setOrganization(makeOrganization());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }


    @Test
    void sendToUsers_nullList_doesNotTouchRepository() {
        assertDoesNotThrow(() -> service.sendToUsers(null, "Titel", "Bericht"));
        verifyNoInteractions(pushSubscriptionRepository);
    }

    @Test
    void sendToUsers_emptyList_doesNotTouchRepository() {
        assertDoesNotThrow(() -> service.sendToUsers(List.of(), "Titel", "Bericht"));
        verifyNoInteractions(pushSubscriptionRepository);
    }

    @Test
    void sendToUser_delegatesToSendToUsersWithSingleElementList() {
        User user = makeUser(1L, "student", "s@test.com");
        when(pushSubscriptionRepository.findByUserIn(List.of(user))).thenReturn(List.of());

        service.sendToUser(user, "Titel", "Bericht");

        verify(pushSubscriptionRepository).findByUserIn(List.of(user));
    }


    @Test
    void sendToUsers_webPushNotInitialized_doesNotThrowAndDoesNotDeleteSubscription() {
        User user = makeUser(1L, "student", "s@test.com");
        PushSubscription subscription = PushSubscription.forWebPush(user, user.getOrganization(), "https://endpoint", "p256dh", "auth");
        when(pushSubscriptionRepository.findByUserIn(List.of(user))).thenReturn(List.of(subscription));

        assertDoesNotThrow(() -> service.sendToUsers(List.of(user), "Titel", "Bericht"));

        verify(pushSubscriptionRepository, never()).delete(any());
    }

    @Test
    void sendToUsers_webPushSuccessResponse_doesNotDeleteSubscription() throws Exception {
        ReflectionTestUtils.setField(service, "pushService", pushService);
        User user = makeUser(1L, "student", "s@test.com");
        PushSubscription subscription = PushSubscription.forWebPush(user, user.getOrganization(), "https://endpoint", validP256dh(), validAuthSecret());
        when(pushSubscriptionRepository.findByUserIn(List.of(user))).thenReturn(List.of(subscription));
        HttpResponse response = mockHttpResponse(201);
        when(pushService.send(any())).thenReturn(response);

        service.sendToUsers(List.of(user), "Titel", "Bericht");

        verify(pushSubscriptionRepository, never()).delete(any());
    }

    @Test
    void sendToUsers_webPushGoneResponse_deletesSubscription() throws Exception {
        ReflectionTestUtils.setField(service, "pushService", pushService);
        User user = makeUser(1L, "student", "s@test.com");
        PushSubscription subscription = PushSubscription.forWebPush(user, user.getOrganization(), "https://endpoint", validP256dh(), validAuthSecret());
        when(pushSubscriptionRepository.findByUserIn(List.of(user))).thenReturn(List.of(subscription));
        HttpResponse response = mockHttpResponse(410);
        when(pushService.send(any())).thenReturn(response);

        service.sendToUsers(List.of(user), "Titel", "Bericht");

        verify(pushSubscriptionRepository).delete(subscription);
    }

    @Test
    void sendToUsers_webPushNotFoundResponse_deletesSubscription() throws Exception {
        ReflectionTestUtils.setField(service, "pushService", pushService);
        User user = makeUser(1L, "student", "s@test.com");
        PushSubscription subscription = PushSubscription.forWebPush(user, user.getOrganization(), "https://endpoint", validP256dh(), validAuthSecret());
        when(pushSubscriptionRepository.findByUserIn(List.of(user))).thenReturn(List.of(subscription));
        HttpResponse response = mockHttpResponse(404);
        when(pushService.send(any())).thenReturn(response);

        service.sendToUsers(List.of(user), "Titel", "Bericht");

        verify(pushSubscriptionRepository).delete(subscription);
    }

    @Test
    void sendToUsers_webPushThrowsException_isCaughtPerSubscription() throws Exception {
        ReflectionTestUtils.setField(service, "pushService", pushService);
        User user = makeUser(1L, "student", "s@test.com");
        PushSubscription subscription = PushSubscription.forWebPush(user, user.getOrganization(), "https://endpoint", validP256dh(), validAuthSecret());
        when(pushSubscriptionRepository.findByUserIn(List.of(user))).thenReturn(List.of(subscription));
        when(pushService.send(any())).thenThrow(new java.io.IOException("boom"));

        assertDoesNotThrow(() -> service.sendToUsers(List.of(user), "Titel", "Bericht"));
    }


    @Test
    void sendToUsers_fcmNotAvailable_doesNotThrowAndDoesNotDeleteSubscription() {
        User user = makeUser(1L, "student", "s@test.com");
        PushSubscription subscription = PushSubscription.forFcm(user, user.getOrganization(), "fcm-token");
        when(pushSubscriptionRepository.findByUserIn(List.of(user))).thenReturn(List.of(subscription));

        assertDoesNotThrow(() -> service.sendToUsers(List.of(user), "Titel", "Bericht"));

        verify(pushSubscriptionRepository, never()).delete(any());
    }

    @Test
    void sendToUsers_fcmAvailableAndSendSucceeds_doesNotDeleteSubscription() throws Exception {
        ReflectionTestUtils.setField(service, "firebaseAvailable", true);
        User user = makeUser(1L, "student", "s@test.com");
        PushSubscription subscription = PushSubscription.forFcm(user, user.getOrganization(), "fcm-token");
        when(pushSubscriptionRepository.findByUserIn(List.of(user))).thenReturn(List.of(subscription));

        FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
        when(firebaseMessaging.send(any(Message.class))).thenReturn("message-id");

        try (MockedStatic<FirebaseMessaging> mocked = mockStatic(FirebaseMessaging.class)) {
            mocked.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);

            service.sendToUsers(List.of(user), "Titel", "Bericht");
        }

        verify(pushSubscriptionRepository, never()).delete(any());
    }

    @Test
    void sendToUsers_fcmUnregisteredError_deletesSubscription() throws Exception {
        ReflectionTestUtils.setField(service, "firebaseAvailable", true);
        User user = makeUser(1L, "student", "s@test.com");
        PushSubscription subscription = PushSubscription.forFcm(user, user.getOrganization(), "fcm-token");
        when(pushSubscriptionRepository.findByUserIn(List.of(user))).thenReturn(List.of(subscription));

        FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        when(exception.getMessagingErrorCode()).thenReturn(MessagingErrorCode.UNREGISTERED);
        when(firebaseMessaging.send(any(Message.class))).thenThrow(exception);

        try (MockedStatic<FirebaseMessaging> mocked = mockStatic(FirebaseMessaging.class)) {
            mocked.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);

            assertDoesNotThrow(() -> service.sendToUsers(List.of(user), "Titel", "Bericht"));
        }

        verify(pushSubscriptionRepository).delete(subscription);
    }

    @Test
    void sendToUsers_fcmOtherError_doesNotDeleteSubscription() throws Exception {
        ReflectionTestUtils.setField(service, "firebaseAvailable", true);
        User user = makeUser(1L, "student", "s@test.com");
        PushSubscription subscription = PushSubscription.forFcm(user, user.getOrganization(), "fcm-token");
        when(pushSubscriptionRepository.findByUserIn(List.of(user))).thenReturn(List.of(subscription));

        FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        when(exception.getMessagingErrorCode()).thenReturn(MessagingErrorCode.INTERNAL);
        when(firebaseMessaging.send(any(Message.class))).thenThrow(exception);

        try (MockedStatic<FirebaseMessaging> mocked = mockStatic(FirebaseMessaging.class)) {
            mocked.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);

            assertDoesNotThrow(() -> service.sendToUsers(List.of(user), "Titel", "Bericht"));
        }

        verify(pushSubscriptionRepository, never()).delete(any());
    }

    private HttpResponse mockHttpResponse(int statusCode) {
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(statusCode);
        when(response.getStatusLine()).thenReturn(statusLine);
        return response;
    }
}
