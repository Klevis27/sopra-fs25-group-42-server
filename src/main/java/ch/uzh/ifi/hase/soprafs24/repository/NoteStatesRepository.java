package ch.uzh.ifi.hase.soprafs24.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import ch.uzh.ifi.hase.soprafs24.entity.Note;
import ch.uzh.ifi.hase.soprafs24.entity.NoteState;

public interface NoteStatesRepository extends JpaRepository<NoteState, Long> {
    Optional<NoteState> findByNote(Note note);

    void deleteByNote(Note note);
}