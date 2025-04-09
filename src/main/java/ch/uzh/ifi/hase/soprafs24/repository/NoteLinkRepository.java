package ch.uzh.ifi.hase.soprafs24.repository;


import ch.uzh.ifi.hase.soprafs24.entity.NoteLink;
import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteLinkRepository extends JpaRepository<NoteLink, Long> {
    List<NoteLink> findAllByVault(Vault vault);
}
