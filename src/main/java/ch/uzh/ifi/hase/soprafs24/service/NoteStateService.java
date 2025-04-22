package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.beans.factory.annotation.Autowired;

import ch.uzh.ifi.hase.soprafs24.entity.NoteState;
import ch.uzh.ifi.hase.soprafs24.repository.NoteStatesRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NoteStatePutDTO;

public class NoteStateService {
    @Autowired
    private NoteStatesRepository noteStateRepository;

    public boolean updateNoteStateContent(NoteStatePutDTO noteStatePutDTO) {
        // Step 1: Find the NoteState by noteId
        NoteState noteState = noteStateRepository.findById(noteStatePutDTO.getNoteId());
        
        // Step 2: Check if the NoteState exists
        if (noteState == null) {
            return false;  // Return false if not found
        }

        // Step 3: Update the content of the NoteState
        noteState.setYjsState(noteStatePutDTO.getContent());

        // Step 4: Save the updated NoteState back to the repository
        noteStateRepository.save(noteState);

        return true;  // Return true on successful update
    }
}
