package ch.uzh.ifi.hase.soprafs24.service;

import java.util.List;
import java.util.Optional;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.entity.VaultPermission;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.repository.VaultPermissionRepository;
import ch.uzh.ifi.hase.soprafs24.constant.Role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;

import ch.uzh.ifi.hase.soprafs24.entity.Note;
import ch.uzh.ifi.hase.soprafs24.entity.NotePermission;
import ch.uzh.ifi.hase.soprafs24.repository.NotePermissionRepository;
import ch.uzh.ifi.hase.soprafs24.repository.NoteRepository;
import java.util.HashSet;


@Service
public class VaultService {
    // private static final Logger log = LoggerFactory.getLogger(UserService.class); // Maybe useful later
    private final VaultRepository vaultRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final VaultPermissionRepository vaultPermissionRepository;
    private final NoteRepository noteRepository;
    private final NotePermissionRepository notePermissionRepository;


    @Autowired
    public VaultService(UserRepository userRepository, JwtUtil jwtUtil, VaultRepository vaultRepository, VaultPermissionRepository vaultPermissionRepository, NoteRepository noteRepository, NotePermissionRepository notePermissionRepository) {
        this.jwtUtil = jwtUtil;
        this.vaultRepository = vaultRepository;
        this.userRepository = userRepository;
        this.vaultPermissionRepository = vaultPermissionRepository;
        this.noteRepository = noteRepository;
        this.notePermissionRepository = notePermissionRepository;
    }

    public Vault createVault(String userId, VaultPostDTO vaultPostDTO) {
        // Check if the name already exists in the database
        if (vaultRepository.findVaultByName(vaultPostDTO.getName()) != null) {
            return null;
        }
    
        // Fetch user
        User owner = userRepository.findUserById(Long.valueOf(userId));
    
        // Set vault fields
        Vault newVault = new Vault();
        newVault.setName(vaultPostDTO.getName());
        newVault.setOwner(owner);
        newVault.setCreatedAt(LocalDateTime.now());
    
        // Save vault first to get ID
        Vault savedVault = vaultRepository.save(newVault);
    
        // Save OWNER permission for creator
        VaultPermission permission = new VaultPermission();
        permission.setVault(savedVault);
        permission.setUser(owner);
        permission.setRole(Role.OWNER); // make sure this imports from constant
        permission.setGrantedAt(LocalDateTime.now());
    
        vaultPermissionRepository.save(permission);
    
        return savedVault;
    }

    public List<VaultPermissionDTO> getPermissionsForVault(Long vaultId) {
        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vault not found."));

        List<VaultPermission> permissions = vaultPermissionRepository.findByVault(vault);
        List<VaultPermissionDTO> result = new ArrayList<>();

        for (VaultPermission vp : permissions) {
            VaultPermissionDTO dto = new VaultPermissionDTO();
            dto.setUserId(vp.getUser().getId());
            dto.setUsername(vp.getUser().getUsername());
            dto.setRole(vp.getRole().name());
            result.add(dto);
        }

        return result;
    }

    public void addOrUpdatePermission(Long vaultId, VaultPermissionDTO dto) {
        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vault not found."));
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        Optional<VaultPermission> existing = vaultPermissionRepository.findByVaultAndUser(vault, user);
    
        VaultPermission permission = existing.orElse(new VaultPermission());
        permission.setVault(vault);
        permission.setUser(user);
        permission.setRole(Role.valueOf(dto.getRole()));
    
        if (permission.getGrantedAt() == null) {
            permission.setGrantedAt(LocalDateTime.now());
        }
    
        // 1. Save vault permission
        vaultPermissionRepository.save(permission);
    
        // 2. Automatically assign permissions to all notes in the vault
        List<Note> notesInVault = noteRepository.findAllByVault(vault);
    
        for (Note note : notesInVault) {
            boolean alreadyExists = notePermissionRepository.existsByUserIdAndNoteId(user.getId(), note.getId());
    
            if (!alreadyExists) {
                NotePermission notePermission = new NotePermission();
                notePermission.setNoteId(note.getId());
                notePermission.setUserId(user.getId());
                notePermission.setRole(dto.getRole()); // inherit same role (OWNER, EDITOR, VIEWER)
    
                notePermissionRepository.save(notePermission);
            }
        }
    }
    
    public boolean deleteVault(Long vaultId, Long userId) {
        Optional<Vault> vaultOpt = vaultRepository.findById(vaultId);
        if (vaultOpt.isEmpty()) return false;
    
        Vault vault = vaultOpt.get();
    
        if (!vault.getOwner().getId().equals(userId)) {
            return false;
        }
    
        vaultRepository.delete(vault);
        return true;
    }
    
    public boolean updateVault(Long vaultId, VaultPostDTO updatedData, Long userId) {
        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vault not found"));
        System.out.println("Vault ID: " + vaultId);
        System.out.println("Vault owner ID: " + vault.getOwner().getId());
        System.out.println("Requesting user ID: " + userId);
    
        if (!vault.getOwner().getId().equals(userId)) {
            return false;
        }
    
        if (updatedData.getName() != null) {
            vault.setName(updatedData.getName());
        }
    
        vaultRepository.save(vault);
        return true;
    }
    // New ✅
    public List<VaultDTO> getVaultsForUser(String userId) {
        Long uid = Long.parseLong(userId);
        List<VaultPermission> permissions = vaultPermissionRepository.findByUserId(uid);
    
        List<VaultDTO> result = new ArrayList<>();
    
        for (VaultPermission perm : permissions) {
            Vault vault = perm.getVault();
            String role = perm.getRole().name(); // OWNER, EDITOR, VIEWER
            VaultDTO dto = VaultDTO.fromEntityWithRole(vault, role);
            result.add(dto);
        }
    
        return result;
    
    
    }
    


}