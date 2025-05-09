package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Note;
import ch.uzh.ifi.hase.soprafs24.entity.NotePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotePermissionRepository extends JpaRepository<NotePermission, Long> {

    boolean existsByUserIdAndNoteId(Long userId, Long noteId);

    List<NotePermission> findByNoteId(Long noteId);

    List<NotePermission> findByUserId(Long userId);
    
    void deleteAllByNote(Note note);

    
}