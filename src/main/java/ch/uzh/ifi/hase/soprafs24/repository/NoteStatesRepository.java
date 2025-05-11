package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Note;
import ch.uzh.ifi.hase.soprafs24.entity.NoteState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteStatesRepository extends JpaRepository<NoteState, Long> {

    NoteState findNoteStateByNote(Note note);

    void deleteByNote(Note note);
}