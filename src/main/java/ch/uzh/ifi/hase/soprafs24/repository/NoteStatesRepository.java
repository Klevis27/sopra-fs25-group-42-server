package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.NoteState;

import org.springframework.data.jpa.repository.JpaRepository;


public interface NoteStatesRepository extends JpaRepository<NoteState, Long>{
    NoteState findNoteStateById(Long id);
}
