package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.entity.VaultPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaultPermissionRepository extends JpaRepository<VaultPermission, Long> {

    List<VaultPermission> findByVault(Vault vault);

    Optional<VaultPermission> findByVaultAndUser(Vault vault, User user);

    Optional<VaultPermission> findByVaultIdAndUserId(Long vaultId, Long userId);
}