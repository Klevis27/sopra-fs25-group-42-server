package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Note;
import ch.uzh.ifi.hase.soprafs24.entity.NoteLink;
import ch.uzh.ifi.hase.soprafs24.entity.NoteState;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.NoteLinkRepository;
import ch.uzh.ifi.hase.soprafs24.repository.NoteRepository;
import ch.uzh.ifi.hase.soprafs24.repository.NoteStatesRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NoteLinksGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NotesGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NotesInvitePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NotesPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.NoteService;
import ch.uzh.ifi.hase.soprafs24.service.VaultService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
public class NoteController {
    private final JwtUtil jwtUtil;
    private final VaultRepository vaultRepository;
    private final NoteRepository noteRepository;
    private final NoteLinkRepository noteLinkRepository;
    private final NoteStatesRepository noteStatesRepository;
    private NoteService noteService;


    public NoteController(VaultService vaultService, JwtUtil jwtUtil, VaultRepository vaultRepository,
            NoteRepository noteRepository, NoteStatesRepository noteStatesRepository, NoteService noteService, UserRepository userRepository,
            NoteLinkRepository noteLinkRepository) {
        this.jwtUtil = jwtUtil;
        this.vaultRepository = vaultRepository;
        this.noteRepository = noteRepository;
        this.noteLinkRepository = noteLinkRepository;
        this.noteStatesRepository = noteStatesRepository;
        this.noteService = noteService;

    }

    // Get Notes
    @GetMapping("/vaults/{vault_id}/notes")
    public ResponseEntity<List<NotesGetDTO>> getNotes(@PathVariable("vault_id") Long id, HttpServletRequest request) {
        // Authentication
        String token = extractTokenFromRequest(request);

        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Check if vault exists
        Optional<Vault> vaultOptional = vaultRepository.findById(id);
        if (vaultOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Get Vault
        Vault vault = vaultOptional.get();

        // Check if user has right to vault
        // Also commented out for testing
        // Has to do with access rights so I dunno
        /*
         * User user = vault.getOwner();
         * if (!Objects.equals(jwtUtil.extractId(token), user.getId().toString())) {
         * return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
         * }
         */

        // TODO check if user has right to vault also in permissions table

        // Fetch notes in vault, map and return
        List<Note> notes = noteRepository.findAllByVault(vault);
        List<NotesGetDTO> notesGetDTOs = new ArrayList<>();
        for (Note note : notes) {
            notesGetDTOs.add(DTOMapper.INSTANCE.convertEntityToNotesGetDTO(note));
        }
        return ResponseEntity.ok(notesGetDTOs);
    }

    @GetMapping("/vaults/{vault_id}/note_links")
    public ResponseEntity<List<NoteLinksGetDTO>> getNoteLinks(@PathVariable("vault_id") Long id, HttpServletRequest request) {

        // Extract token from the Authorization header
        String token = extractTokenFromRequest(request);

        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        // Check if Vault exists
        Optional<Vault> vaultOptional = vaultRepository.findById(id);
        if (vaultOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Vault vault = vaultOptional.get();

        List<NoteLink> links = noteLinkRepository.findAllByVault(vault);

        List<NoteLinksGetDTO> noteLinksGetDTOs = new ArrayList<>();

        for (NoteLink link : links) {
            noteLinksGetDTOs.add(DTOMapper.INSTANCE.convertEntityToNoteLinksGetDTO(link));
        }

        return ResponseEntity.ok(noteLinksGetDTOs);
    }
      
    // POST /vaults/{vault_id}/notes
    @PostMapping("/vaults/{vault_id}/notes")
    public ResponseEntity<Map<String, Object>> createNote(@PathVariable("vault_id") Long vaultId,
                                        @RequestBody Map<String, String> body,
                                        HttpServletRequest request) {
        // Authentication
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid or missing token"));
        }

        // Check if vault exists
        Optional<Vault> vaultOptional = vaultRepository.findById(vaultId);
        if (vaultOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Vault not found"));
        }

        // Check if user has right to vault
        Vault vault = vaultOptional.get();
        User owner = vault.getOwner();
        if (!Objects.equals(jwtUtil.extractId(token), owner.getId().toString())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "You do not have access to this vault"));
        }

        // Check title
        String title = body.get("title");
        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Title cannot be empty."));
        }

        // Create note with title and save
        Note note = new Note();
        note.setTitle(title.trim());
        note.setVault(vault);
        noteRepository.save(note);

        //Create corresponding note state for the note
        NoteState noteState = new NoteState();
        noteState.setNote(note);
        noteState.setYjsState("".getBytes(StandardCharsets.UTF_8));
        noteStatesRepository.save(noteState);

        NotesPostDTO responseNote = DTOMapper.INSTANCE.convertEntityToNotesPostDTO(note);

        // Return response
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Creation of Note successful", "note", responseNote));
    }


    // DELETE /notes/{note_id}
    @DeleteMapping("/notes/{note_id}")
    public ResponseEntity<?> deleteNote(@PathVariable("note_id") Long noteId,
                                        HttpServletRequest request) {
        // TODO Overwork

        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Note> noteOptional = noteRepository.findById(noteId);
        if (noteOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Note note = noteOptional.get();
        Vault vault = note.getVault();
        User owner = vault.getOwner();

        if (!Objects.equals(jwtUtil.extractId(token), owner.getId().toString())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        noteRepository.delete(note);
        return ResponseEntity.ok().build();
    }

    // Invite user to note
    @PostMapping("/notes/{noteId}/invite")
    public ResponseEntity<?> inviteUserToNote(
        @PathVariable Long noteId,
        @RequestBody NotesInvitePostDTO  inviteRequest
    ) {
        noteService.inviteUserToNote(noteId, inviteRequest.getUsername(), inviteRequest.getRole());
        return ResponseEntity.ok("User invited to note successfully");
    }


    // Helper method to extract token from the Authorization header
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }

}
