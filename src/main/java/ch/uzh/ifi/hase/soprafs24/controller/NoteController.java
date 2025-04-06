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
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
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
    private final UserRepository userRepository;

    public NoteController(VaultService vaultService, JwtUtil jwtUtil, VaultRepository vaultRepository,
            NoteRepository noteRepository, NoteService noteService, UserRepository userRepository,
            NoteLinkRepository noteLinkRepository) {
        this.vaultService = vaultService;
        this.jwtUtil = jwtUtil;
        this.vaultRepository = vaultRepository;
        this.noteRepository = noteRepository;
        this.noteService = noteService;
        this.userRepository = userRepository;
        this.noteLinkRepository = noteLinkRepository;
    }

    // Get Notes
    @GetMapping("/vaults/{vault_id}/notes")
    public ResponseEntity<List<NotesGetDTO>> profile(@PathVariable("vault_id") Long id, HttpServletRequest request) {
        // Extract token from the Authorization header
        // String token = extractTokenFromRequest(request);

        // Temporarily commented out authentification so testing is easier
        /*
         * if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token)))
         * {
         * return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
         * }
         */

        // Check if Vault exists
        Optional<Vault> vaultOptional = vaultRepository.findById(id);
        if (vaultOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Vault vault = vaultOptional.get();

        // Check if user has right to vault
        // Also commented out for testing
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
    public ResponseEntity<List<NoteLinksGetDTO>> profile(@PathVariable("vault_id") Long id) {

        // Check if Vault exists
        Optional<Vault> vaultOptional = vaultRepository.findById(id);
        if (vaultOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Vault vault = vaultOptional.get();

        List<NoteLink> links = noteLinkRepository.findAllByVault(vault);

        List<NoteLinksGetDTO> noteLinksGetDTOs = new ArrayList<>();

        for (NoteLink link : links){
            noteLinksGetDTOs.add(DTOMapper.INSTANCE.convertEntityToNoteLinksGetDTO(link));
        }

        return ResponseEntity.ok(noteLinksGetDTOs);
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
