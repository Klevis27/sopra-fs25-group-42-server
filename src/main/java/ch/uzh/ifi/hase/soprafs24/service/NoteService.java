package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Note;
import ch.uzh.ifi.hase.soprafs24.entity.NotePermission;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.entity.VaultPermission;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.NotePermissionRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultPermissionRepository;
import ch.uzh.ifi.hase.soprafs24.repository.NoteRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NotePermissionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Set;


@Service
public class NoteService {

    private final VaultRepository vaultRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final NotePermissionRepository notePermissionRepository;
    private final VaultPermissionRepository vaultPermissionRepository;
    private final NoteRepository noteRepository;

    @Autowired
    public NoteService(UserRepository userRepository,
                       JwtUtil jwtUtil,
                       VaultRepository vaultRepository,
                       NotePermissionRepository notePermissionRepository,
                       VaultPermissionRepository vaultPermissionRepository,
                       NoteRepository noteRepository) {
        this.jwtUtil = jwtUtil;
        this.vaultRepository = vaultRepository;
        this.userRepository = userRepository;
        this.notePermissionRepository = notePermissionRepository;
        this.vaultPermissionRepository = vaultPermissionRepository;
        this.noteRepository = noteRepository;
    }

    /**
     * Creates a note and assigns the creator as OWNER.
     */
    public Note createNoteWithOwner(String title, Vault vault, Long creatorId) {
        Note note = new Note();
        note.setTitle(title);
        note.setVault(vault);
        noteRepository.save(note);

        NotePermission permission = new NotePermission();
        permission.setNoteId(note.getId());
        permission.setUserId(creatorId);
        permission.setRole("OWNER");
        notePermissionRepository.save(permission);

        return note;
    }

    /**
     * Invites a user to an existing note.
     */
    public void inviteUserToNote(Long noteId, String username, String role) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Long userId = user.getId();
        if (notePermissionRepository.existsByUserIdAndNoteId(userId, noteId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has permission to this note");
        }

        NotePermission permission = new NotePermission();
        permission.setNoteId(noteId);
        permission.setUserId(userId);
        permission.setRole(role != null ? role : "reader");
        notePermissionRepository.save(permission);
    }

    /**
     * Returns all permissions for a note.
     */
    public List<NotePermissionDTO> getNotePermissions(Long noteId) {
    Note note = noteRepository.findNoteById(noteId);
    Vault vault = note.getVault();

    // 1. Explicit note permissions
    List<NotePermission> notePermissions = notePermissionRepository.findByNoteId(noteId);

    // 2. Vault-level permissions
    List<VaultPermission> vaultPermissions = vaultPermissionRepository.findByVault(vault);

    // Track explicit note permission user IDs
    Set<Long> explicitUserIds = notePermissions.stream()
            .map(NotePermission::getUserId)
            .collect(Collectors.toSet());

    List<NotePermissionDTO> results = new ArrayList<>();

    // Add explicit note permissions
    for (NotePermission np : notePermissions) {
        Optional<User> user = userRepository.findById(np.getUserId());
        user.ifPresent(u -> results.add(new NotePermissionDTO(u.getUsername(), np.getRole())));
    }

    // Add inherited vault permissions (if not already in the notePermissions)
    for (VaultPermission vp : vaultPermissions) {
        if (!explicitUserIds.contains(vp.getUser().getId())) {
            results.add(new NotePermissionDTO(vp.getUser().getUsername(), vp.getRole().name()));
        }
    }

    return results;
}


    public List<Note> getSharedNotesForUser(Long userId) {
        List<Note> allAccessibleNotes = noteRepository.findAllNotesUserCanAccess(userId);
    
        return allAccessibleNotes.stream()
            .filter(note -> !note.getVault().getOwner().getId().equals(userId)) // ⛔ not owned
            .collect(Collectors.toList());
    }
    
    
}