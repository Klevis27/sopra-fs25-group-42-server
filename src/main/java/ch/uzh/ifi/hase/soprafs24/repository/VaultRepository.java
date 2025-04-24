package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.entity.VaultPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VaultRepository extends JpaRepository<Vault, Long> {
    Vault findVaultById(Long id);
    Vault findVaultByName(String name);
    List<Vault> findVaultByOwner(User owner);

    @Query("SELECT vp.vault FROM VaultPermission vp WHERE vp.user = :user")
    List<Vault> findVaultsByUserPermission(User user);
}