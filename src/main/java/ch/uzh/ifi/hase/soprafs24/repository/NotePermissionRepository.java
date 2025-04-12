package ch.uzh.ifi.hase.soprafs24.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import ch.uzh.ifi.hase.soprafs24.entity.NotePermission;

public interface NotePermissionRepository extends JpaRepository<NotePermission, Long> {
    boolean existsByUserIdAndNoteId(Long userId, Long noteId);
}

