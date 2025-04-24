package ch.uzh.ifi.hase.soprafs24.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs24.entity.NoteState;
import ch.uzh.ifi.hase.soprafs24.entity.Note;
import ch.uzh.ifi.hase.soprafs24.repository.NoteStatesRepository;
import ch.uzh.ifi.hase.soprafs24.repository.NoteRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NoteStatePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NoteStatePutDTO;

@Service
public class NoteStateService {
    @Autowired
    private NoteStatesRepository noteStateRepository;
    @Autowired
    private NoteRepository noteRepository;

    public boolean updateNoteStateContent(NoteStatePutDTO noteStatePutDTO) {
        // Step 1: Find the NoteState by noteId
        Long noteId = noteStatePutDTO.getNoteId();
        Note note = noteRepository.findNoteById(noteId);
        if (note == null) return false;

        NoteState noteState = noteStateRepository.findNoteStateByNote(note);
        
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

    public boolean createNoteState(NoteStatePostDTO noteStatePostDTO){

        Long noteId = noteStatePostDTO.getNoteId();
        Note note = noteRepository.findNoteById(noteId);
        if (note == null) return false;

        NoteState noteState = noteStateRepository.findNoteStateByNote(note);

        if (noteState != null){
            return false;
        }

        noteState = new NoteState();
        noteState.setNote(note);
        noteState.setYjsState("Test content".getBytes(StandardCharsets.UTF_8));
        noteStateRepository.save(noteState);

        return true;


    }
}
