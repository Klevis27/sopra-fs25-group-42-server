package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Note;
import ch.uzh.ifi.hase.soprafs24.entity.NoteLink;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.NoteLinkRepository;
import ch.uzh.ifi.hase.soprafs24.repository.NoteRepository;
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
import java.util.*;

@RestController
public class NoteController {

    private final VaultService vaultService;
    private final JwtUtil jwtUtil;
    private final VaultRepository vaultRepository;
    private final NoteRepository noteRepository;
    private final NoteLinkRepository noteLinkRepository;
    private final NoteService noteService;

    public NoteController(VaultService vaultService,
                          JwtUtil jwtUtil,
                          VaultRepository vaultRepository,
                          NoteRepository noteRepository,
                          NoteService noteService,
                          UserRepository userRepository,
                          NoteLinkRepository noteLinkRepository) {
        this.vaultService = vaultService;
        this.jwtUtil = jwtUtil;
        this.vaultRepository = vaultRepository;
        this.noteRepository = noteRepository;
        this.noteLinkRepository = noteLinkRepository;
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
        User user = vault.getOwner();
        if (!Objects.equals(jwtUtil.extractId(token), user.getId().toString())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // TODO check if user has right to vault also in permissions table

        // Fetch notes in vault, map and return
        List<Note> notes = noteRepository.findAllByVault(vault);
        List<NotesGetDTO> notesGetDTOs = new ArrayList<>();
        for (Note note : notes) {
            notesGetDTOs.add(DTOMapper.INSTANCE.convertEntityToNotesGetDTO(note));
        }

        return ResponseEntity.ok(notesGetDTOs);
    }

    // GET Note Links
    @GetMapping("/vaults/{vault_id}/note_links")
    public ResponseEntity<List<NoteLinksGetDTO>> getNoteLinks(@PathVariable("vault_id") Long id, HttpServletRequest request) {
        // Authorization
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

        // Get all note links in Vault
        List<NoteLink> links = noteLinkRepository.findAllByVault(vault);

        // Map and return
        List<NoteLinksGetDTO> noteLinksGetDTOs = new ArrayList<>();
        for (NoteLink link : links) {
            noteLinksGetDTOs.add(DTOMapper.INSTANCE.convertEntityToNoteLinksGetDTO(link));
        }
        return ResponseEntity.ok(noteLinksGetDTOs);
    }

    // POST a new note into a vault
    @PostMapping("/vaults/{vault_id}/notes")
    public ResponseEntity<Map<String, Object>> createNote(@PathVariable("vault_id") Long vaultId,
                                                          @RequestBody Map<String, String> body,
                                                          HttpServletRequest request) {
        // Authentication
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Check if vault exists
        Optional<Vault> vaultOptional = vaultRepository.findById(vaultId);
        if (vaultOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Check if user has right to vault
        Vault vault = vaultOptional.get();
        User owner = vault.getOwner();
        if (!Objects.equals(jwtUtil.extractId(token), owner.getId().toString())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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

        // Map and return
        NotesPostDTO responseNote = DTOMapper.INSTANCE.convertEntityToNotesPostDTO(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Note created", "note", responseNote));
    }

    // DELETE a note in in a vault
    @DeleteMapping("/notes/{note_id}")
    public ResponseEntity<?> deleteNote(@PathVariable("note_id") Long noteId,
                                        HttpServletRequest request) {
        // Authentication
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Check if note exists and get vault and owner of said vault
        Optional<Note> noteOptional = noteRepository.findById(noteId);
        if (noteOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Note note = noteOptional.get();
        Vault vault = note.getVault();
        User owner = vault.getOwner();

        // Can user delete this note? (Is user owner of note)
        if (!Objects.equals(jwtUtil.extractId(token), owner.getId().toString())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Delete note and return
        noteRepository.delete(note);
        return ResponseEntity.ok().build();
    }

    // POST invite user to note
    @PostMapping("/notes/{noteId}/invite")
    public ResponseEntity<?> inviteUserToNote(@PathVariable Long noteId,
                                              @RequestBody NotesInvitePostDTO inviteRequest) {
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
