package com.casacrew.repository;

import com.casacrew.model.PushSubscription;
import com.casacrew.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    List<PushSubscription> findByUser(User user);

    List<PushSubscription> findByUserIn(List<User> users);

    Optional<PushSubscription> findByUserAndEndpoint(User user, String endpoint);

    Optional<PushSubscription> findByUserAndFcmToken(User user, String fcmToken);

    void deleteByUserAndEndpoint(User user, String endpoint);

    void deleteByUserAndFcmToken(User user, String fcmToken);
}
