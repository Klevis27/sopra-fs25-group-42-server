package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.NoteStatePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NoteStatePutDTO;
import ch.uzh.ifi.hase.soprafs24.service.NoteStateService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
        // always return an octet‐stream, even if empty
        return ResponseEntity.ok().contentLength(state.length).body(state);
    }

    @PutMapping(value = "/{noteId}/state",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> upsertNoteState(
            @PathVariable Long noteId,
            @RequestBody byte[] content
    ) {
        // attempt update first
        NoteStatePutDTO putDto = new NoteStatePutDTO();
        putDto.setNoteId(noteId);
        putDto.setContent(content);

        if (noteStateService.updateNoteStateContent(putDto)) {
            return ResponseEntity.noContent().build();  // 204
        }

        // fallback to create
        NoteStatePostDTO postDto = new NoteStatePostDTO();
        postDto.setNoteId(noteId);
        postDto.setContent(content);

        if (noteStateService.createNoteState(postDto)) {
            // newly created
            return ResponseEntity
                    .created(URI.create("/notes/" + noteId + "/state"))
                    .build();
        }

        // neither update nor create succeeded → note doesn’t exist
        return ResponseEntity.notFound().build();
    }
}