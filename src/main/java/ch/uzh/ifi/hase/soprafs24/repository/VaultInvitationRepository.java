package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.VaultInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VaultInvitationRepository extends JpaRepository<VaultInvitation, Long> {
    List<VaultInvitation> findByTargetUserId(Long userId);
    Optional<VaultInvitation> findByToken(String token);
}