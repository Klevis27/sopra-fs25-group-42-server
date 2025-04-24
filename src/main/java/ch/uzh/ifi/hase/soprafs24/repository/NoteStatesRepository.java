package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.NoteState;
import ch.uzh.ifi.hase.soprafs24.entity.Note;

import org.springframework.data.jpa.repository.JpaRepository;


public interface NoteStatesRepository extends JpaRepository<NoteState, Long>{
    NoteState findNoteStateByNote(Note note);
}
