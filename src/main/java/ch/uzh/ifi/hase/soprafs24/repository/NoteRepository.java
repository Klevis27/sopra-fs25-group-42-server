package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Note;
import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findAllByVault(Vault vault);
    Note findNoteById(Long id);
    @Query("SELECT n FROM Note n " +
       "WHERE n.id IN (SELECT np.noteId FROM NotePermission np WHERE np.userId = :userId) " +
       "OR n.vault.id IN (SELECT vp.vault.id FROM VaultPermission vp WHERE vp.user.id = :userId)")
List<Note> findAllNotesUserCanAccess(@Param("userId") Long userId);

}
