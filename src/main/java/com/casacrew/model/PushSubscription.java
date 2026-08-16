package com.casacrew.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "push_subscriptions")
public class PushSubscription {

    public enum Platform {
        WEB, FCM
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Platform platform;

    @Column(columnDefinition = "TEXT")
    private String endpoint;

    @Column(length = 255)
    private String p256dh;

    @Column(length = 255)
    private String auth;

    @Column(name = "fcm_token", length = 255)
    private String fcmToken;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected PushSubscription() {}

    public static PushSubscription forWebPush(User user, Organization organization, String endpoint, String p256dh, String auth) {
        PushSubscription subscription = new PushSubscription();
        subscription.user = user;
        subscription.organization = organization;
        subscription.platform = Platform.WEB;
        subscription.endpoint = endpoint;
        subscription.p256dh = p256dh;
        subscription.auth = auth;
        return subscription;
    }

    public static PushSubscription forFcm(User user, Organization organization, String fcmToken) {
        PushSubscription subscription = new PushSubscription();
        subscription.user = user;
        subscription.organization = organization;
        subscription.platform = Platform.FCM;
        subscription.fcmToken = fcmToken;
        return subscription;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Organization getOrganization() { return organization; }
    public Platform getPlatform() { return platform; }
    public String getEndpoint() { return endpoint; }
    public String getP256dh() { return p256dh; }
    public String getAuth() { return auth; }
    public String getFcmToken() { return fcmToken; }
    public Instant getCreatedAt() { return createdAt; }
}
