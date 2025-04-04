package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Note;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VaultRepository extends JpaRepository<Vault, Long> {
    Vault findVaultById(Long id);
    Vault findVaultByName(String name);
    List<Vault> findVaultByOwner(User owner);
}
