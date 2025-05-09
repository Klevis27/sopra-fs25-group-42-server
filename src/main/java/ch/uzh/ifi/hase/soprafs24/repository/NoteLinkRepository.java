package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Note;
import ch.uzh.ifi.hase.soprafs24.entity.NoteLink;
import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface NoteLinkRepository extends JpaRepository<NoteLink, Long> {
    List<NoteLink> findAllByVault(Vault vault);

    @Modifying
    @Transactional
    @Query("""
           DELETE FROM NoteLink nl
           WHERE nl.sourceNote = :note
              OR nl.targetNote = :note
           """)
    void deleteAllByNote(@Param("note") Note note);
}