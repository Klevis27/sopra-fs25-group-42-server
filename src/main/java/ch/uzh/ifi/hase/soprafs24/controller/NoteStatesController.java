package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.NoteStatePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NoteStatePutDTO;
import ch.uzh.ifi.hase.soprafs24.service.NoteStateService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notes")
public class NoteStatesController {
    private final NoteStateService noteStateService;

    public NoteStatesController(NoteStateService noteStateService) {
        this.noteStateService = noteStateService;
    }

    @GetMapping(value = "/{noteId}/state", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getNoteState(@PathVariable Long noteId) {
        byte[] state = noteStateService.loadState(noteId);
        if (state.length == 0) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(state);
    }

    @PutMapping(value = "/{noteId}/state", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> updateNoteState(
            @PathVariable Long noteId,
            @RequestBody byte[] content
    ) {
        NoteStatePutDTO dto = new NoteStatePutDTO();
        dto.setNoteId(noteId);
        dto.setContent(content);

        boolean updated = noteStateService.updateNoteStateContent(dto);
        if (updated) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/{noteId}/state", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> createNoteState(
            @PathVariable Long noteId,
            @RequestBody byte[] content
    ) {
        NoteStatePostDTO dto = new NoteStatePostDTO();
        dto.setNoteId(noteId);
        dto.setContent(content);

        boolean created = noteStateService.createNoteState(dto);
        if (created) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Note state exists or note not found");
    }
}