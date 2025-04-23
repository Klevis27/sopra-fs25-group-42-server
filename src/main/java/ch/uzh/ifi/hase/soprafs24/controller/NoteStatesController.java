package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.NoteStatePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NoteStatePutDTO;
import ch.uzh.ifi.hase.soprafs24.service.NoteStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class NoteStatesController {

    
    private NoteStateService noteStateService;
    public NoteStatesController(NoteStateService noteStateService){
        this.noteStateService = noteStateService;
    }

    @PutMapping("/noteState/{note_Id}")
    public ResponseEntity<?> updateNoteStateContent(
            @PathVariable("note_Id") Long noteId, 
            @RequestBody NoteStatePutDTO noteStatePutDTO) {
        
        // Step 1: Check if the provided noteId in the DTO matches the URL noteId
        if (!noteId.equals(noteStatePutDTO.getNoteId())) {
            return ResponseEntity.badRequest().body("Note ID in URL does not match the one in the body");
        }

        // Step 2: Delegate to the service to update the NoteState content
        boolean isUpdated = noteStateService.updateNoteStateContent(noteStatePutDTO);

        // Step 3: Return appropriate response based on success or failure
        if (isUpdated) {
            return ResponseEntity.ok("Note state content updated successfully");
        } else {
            return ResponseEntity.status(404).body("Note state not found");
        }
    }

    @PostMapping("/noteState/{note_Id}")
    public ResponseEntity<?> createNoteState(
        @PathVariable("note_Id") Long noteId, 
        @RequestBody NoteStatePostDTO noteStatePostDTO){

                    // Step 1: Check if the provided noteId in the DTO matches the URL noteId
        if (!noteId.equals(noteStatePostDTO.getNoteId())) {
            return ResponseEntity.badRequest().body("Note ID in URL does not match the one in the body");
        }

        // Step 2: Delegate to the service to update the NoteState content
        boolean isUpdated = noteStateService.createNoteState(noteStatePostDTO);

        // Step 3: Return appropriate response based on success or failure
        if (isUpdated) {
            return ResponseEntity.ok("Note state created successfully");
        } else {
            return ResponseEntity.status(404).body("Note state could not be created");
        }


        }
}
