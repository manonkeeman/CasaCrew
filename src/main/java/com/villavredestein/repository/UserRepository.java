package com.villavredestein.repository;

import com.villavredestein.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Bewust org-loos: wordt gebruikt tijdens login, vóórdat de organisatie
    // van de aanroeper bekend is (de organisatie wordt juist van deze user
    // afgeleid, niet andersom). Email blijft platformbreed uniek, dus dit
    // is een veilige, ondubbelzinnige lookup.
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findAllByOrderByIdAsc();

    List<User> findByRole(User.Role role);

    List<User> findByOrganization_IdOrderByIdAsc(Long organizationId);

    List<User> findByOrganization_IdAndRole(Long organizationId, User.Role role);

    boolean existsByOrganization_IdAndUsernameIgnoreCase(Long organizationId, String username);
}