package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.NotePermission;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.NotePermissionRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;




@Service
public class NoteService {
    // private static final Logger log = LoggerFactory.getLogger(UserService.class); // Maybe useful later
    private final VaultRepository vaultRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private NotePermissionRepository notePermissionRepository;

    @Autowired
    public NoteService(UserRepository userRepository, JwtUtil jwtUtil,
                    VaultRepository vaultRepository, NotePermissionRepository notePermissionRepository) {
        this.jwtUtil = jwtUtil;
        this.vaultRepository = vaultRepository;
        this.userRepository = userRepository;
        this.notePermissionRepository = notePermissionRepository; // <-- add this line
    }


    public void inviteUserToNote(Long noteId, String username, String role) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Long userId = user.getId();

        // Check if already invited
        if (notePermissionRepository.existsByUserIdAndNoteId(userId, noteId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has permission to this note");
        }

        NotePermission permission = new NotePermission();
        permission.setNoteId(noteId);
        permission.setUserId(userId);
        permission.setRole(role != null ? role : "reader");

        notePermissionRepository.save(permission);
    }

}
