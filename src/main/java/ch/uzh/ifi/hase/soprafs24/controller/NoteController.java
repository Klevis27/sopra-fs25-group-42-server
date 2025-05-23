package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.NoteService;
import ch.uzh.ifi.hase.soprafs24.service.VaultService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping // base URL boş – tüm mapping’ler aynı kalıyor
public class NoteController {

    private final JwtUtil jwtUtil;
    private final VaultRepository vaultRepository;
    private final NoteRepository noteRepository;
    private final NoteLinkRepository noteLinkRepository;
    private final NoteStatesRepository noteStatesRepository;
    private final NotePermissionRepository notePermissionRepository;
    private final VaultPermissionRepository vaultPermissionRepository;
    private final NoteService noteService;
    private final VaultService vaultService;

    public NoteController(JwtUtil jwtUtil,
                          
                  VaultRepository vaultRepository,
                  NoteRepository noteRepository,
                  NoteLinkRepository noteLinkRepository,
                  NoteStatesRepository noteStatesRepository,
                  NotePermissionRepository notePermissionRepository,
                  VaultPermissionRepository vaultPermissionRepository,
                  NoteService noteService,
                  VaultService vaultService   /* yalnızca wiring için */) {

        this.jwtUtil = jwtUtil;
        this.vaultRepository = vaultRepository;
        this.noteRepository = noteRepository;
        this.noteLinkRepository = noteLinkRepository;
        this.noteStatesRepository = noteStatesRepository;
        this.notePermissionRepository = notePermissionRepository;
        this.vaultPermissionRepository = vaultPermissionRepository;
        this.noteService = noteService;
        this.vaultService = vaultService;
    }

    /*
     * -------------------------------------------------
     * LIST – notes & links
     * -------------------------------------------------
     */

       @GetMapping("/vaults/{vaultId}/notes")
       public ResponseEntity<List<NotesGetDTO>> getNotes(@PathVariable Long vaultId,
                                                         HttpServletRequest request) {
           String token = extractTokenFromRequest(request);
           if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
           }
       
           Vault vault = vaultRepository.findById(vaultId).orElse(null);
           if (vault == null) {
               return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
           }
       
           Long userId = Long.parseLong(jwtUtil.extractId(token));
       
           // ✅ Get all notes in this vault
           List<Note> allNotesInVault = noteRepository.findAllByVault(vault);
       
           // ✅ Filter: only notes the user has access to (directly OR via vault permission)
           List<Note> accessibleNotes = allNotesInVault.stream()
               .filter(note -> notePermissionRepository.existsByUserIdAndNoteId(userId, note.getId())
                            || vaultPermissionRepository.findByUserId(userId).stream()
                                 .anyMatch(vp -> vp.getVault().getId().equals(vaultId)))
               .collect(Collectors.toList());
       
           List<NotesGetDTO> result = accessibleNotes.stream()
               .map(DTOMapper.INSTANCE::convertEntityToNotesGetDTO)
               .collect(Collectors.toList());
       
           return ResponseEntity.ok(result);
       }
       

    // ------------------------------------NoteLinks--------------------------------//

    @GetMapping("/vaults/{vaultId}/note_links")
    public ResponseEntity<List<NoteLinksGetDTO>> getNoteLinks(@PathVariable Long vaultId,
            HttpServletRequest request) {

        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Vault vault = vaultRepository.findById(vaultId)
                .orElse(null);
        if (vault == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<NoteLinksGetDTO> dto = noteLinkRepository.findAllByVault(vault).stream()
                .map(DTOMapper.INSTANCE::convertEntityToNoteLinksGetDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/vaults/{vaultId}/note_links")
    public ResponseEntity<?> createNoteLink(@PathVariable Long vaultId,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        Vault vault = vaultRepository.findById(vaultId).orElse(null);
        if (vault == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vault not found");
        }

        NoteLink noteLink = new NoteLink();
        Note targetNote = noteRepository.findById(Long.parseLong(body.get("target"))).orElse(null);
        Note sourceNote = noteRepository.findById(Long.parseLong(body.get("source"))).orElse(null);

        if (targetNote == null || sourceNote == null) {
            System.err.println("Either source note or target note do not exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Either source note or target note do not exist");
        }
        noteLink.setTargetNote(targetNote);
        noteLink.setSourceNote(sourceNote);
        noteLink.setVault(vault);
        noteLink.setLinkType("internal");

        List<NoteLink> links = noteLinkRepository.findAllByVault(vault);

        boolean linkExists = false;
        for (NoteLink link : links) {
            if (noteLink.getSourceNote().getId() == link.getSourceNote().getId() &&
                    noteLink.getTargetNote().getId() == link.getTargetNote().getId()) {
                linkExists = true;
            }
        }

        if (linkExists) {
            System.err.println("Link exists already");
            return ResponseEntity.status(HttpStatus.OK).body("NoteLink already exists. Continuing as normal");
        }
        noteLinkRepository.save(noteLink);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Note created"));
    }

    @DeleteMapping("/vaults/{vaultId}/{sourceNoteId}/{targetNoteId}/note_links")
    public ResponseEntity<?> deleteNoteLink(@PathVariable Long vaultId,
                                            @PathVariable Long sourceNoteId,
                                            @PathVariable Long targetNoteId,
                                            HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        Vault vault = vaultRepository.findById(vaultId).orElse(null);
        if (vault == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vault not found");
        }

        Note targetNote = noteRepository.findById(targetNoteId).orElse(null);
        Note sourceNote = noteRepository.findById(sourceNoteId).orElse(null);

        if (targetNote == null || sourceNote == null) {
            System.err.println("Either source note or target note do not exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Either source note or target note do not exist");
        }

        NoteLink noteLink = new NoteLink();
        noteLink.setTargetNote(targetNote);
        noteLink.setSourceNote(sourceNote);
        noteLink.setVault(vault);
        noteLink.setLinkType("internal");

        List<NoteLink> links = noteLinkRepository.findAllByVault(vault);

        boolean linkExists = false;
        NoteLink linkToDelete = new NoteLink();
        for (NoteLink link : links) {
            if (noteLink.getSourceNote().getId() == link.getSourceNote().getId() &&
                    noteLink.getTargetNote().getId() == link.getTargetNote().getId()) {
                linkToDelete = link;
                linkExists = true;
            }
        }

        if (linkExists){
            noteLinkRepository.delete(linkToDelete);
        }
        
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    // ------------------------------------NoteLinks--------------------------------//

    /*
     * -------------------------------------------------
     * CREATE
     * -------------------------------------------------
     */

    @PostMapping("/vaults/{vaultId}/notes")
    public ResponseEntity<?> createNote(@PathVariable Long vaultId,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        Vault vault = vaultRepository.findById(vaultId).orElse(null);
        if (vault == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vault not found");
        }

        if (!vault.getOwner().getId().toString().equals(jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not the vault owner");
        }

        String title = Optional.ofNullable(body.get("title")).orElse("").trim();
        if (title.isEmpty()) {
            return ResponseEntity.badRequest().body("Title cannot be empty");
        }

        Note note = noteService.createNoteWithOwner(title, vault, vault.getOwner().getId());

        NoteState state = new NoteState();
        state.setNote(note);
        state.setYjsState(new byte[0]);
        noteStatesRepository.save(state);

        // Makes no sense to create a PostDTO here to send back to the frontend
        NotesPostDTO dto = DTOMapper.INSTANCE.convertEntityToNotesPostDTO(note);
        
        // Überprüfung auf null-Werte hinzugefügt
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.CREATED)
                               .body(Collections.singletonMap("message", "Note created"));
        }
        
        // Verwendung von HashMap statt Map.of() für mehr Flexibilität
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Note created");
        response.put("note", dto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
     * -------------------------------------------------
     * DELETE
     * -------------------------------------------------
     */

    @Transactional
    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<?> deleteNote(@PathVariable Long noteId,
            HttpServletRequest request) {

        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Note note = noteRepository.findById(noteId).orElse(null);
        if (note == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!note.getVault().getOwner().getId().toString().equals(jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        /*  Önce bağımlı tabloları temizle  */
        noteStatesRepository.deleteByNote(note);
        noteLinkRepository.deleteAllByNote(note);
        notePermissionRepository.deleteAllByNote(note);

        /*  Sonra asıl notu sil  */
        noteRepository.delete(note);
        return ResponseEntity.ok().build();
    }

    /*
     * -------------------------------------------------
     * PERMISSIONS & UTILS
     * -------------------------------------------------
     */

    @PostMapping("/notes/{noteId}/invite")
    public ResponseEntity<?> invite(@PathVariable Long noteId,
            @RequestBody NotesInvitePostDTO dto) {

        noteService.inviteUserToNote(noteId, dto.getUsername(), dto.getRole());
        return ResponseEntity.ok("User invited");
    }

    @GetMapping("/notes/{noteId}/permissions")
    public ResponseEntity<List<NotePermissionDTO>> permissions(@PathVariable Long noteId,
            HttpServletRequest request) {

        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(noteService.getNotePermissions(noteId));
    }

    @GetMapping("/notes/{noteId}")
    public ResponseEntity<NotesGetDTO> read(@PathVariable Long noteId,
            HttpServletRequest request) {

        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return noteRepository.findById(noteId)
                .map(DTOMapper.INSTANCE::convertEntityToNotesGetDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/notes/{noteId}")
    public ResponseEntity<?> rename(@PathVariable Long noteId,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Note note = noteRepository.findById(noteId).orElse(null);
        if (note == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!note.getVault().getOwner().getId().toString().equals(jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String newTitle = Optional.ofNullable(body.get("title")).orElse("").trim();
        if (newTitle.isEmpty()) {
            return ResponseEntity.badRequest().body("Title cannot be empty");
        }

        note.setTitle(newTitle);
        noteRepository.save(note);
        return ResponseEntity.ok("Title updated");
    }

    @GetMapping("/notes/shared")
public ResponseEntity<List<NotesGetDTO>> getSharedNotes(HttpServletRequest request) {
    String token = extractTokenFromRequest(request);
    if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Long userId = Long.parseLong(jwtUtil.extractId(token));
    List<Note> sharedNotes = noteService.getSharedNotesForUser(userId);

    List<NotesGetDTO> result = new ArrayList<>();

    for (Note note : sharedNotes) {
        if (note.getVault() == null) {
            System.err.println("❌ Note " + note.getId() + " has no vault!");
            continue;
        }

        NotesGetDTO dto = DTOMapper.INSTANCE.convertEntityToNotesGetDTO(note);
        result.add(dto);
    }

    return ResponseEntity.ok(result);
}


    /* ------------------------------------------------- */
    private String extractTokenFromRequest(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        return header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
    }
}
