package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.entity.VaultPermission;
import ch.uzh.ifi.hase.soprafs24.entity.VaultPermission.Role;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultPermissionRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultPermissionDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultPostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Service
public class VaultService {
    private final VaultRepository vaultRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final VaultPermissionRepository vaultPermissionRepository;

    @Autowired
    public VaultService(UserRepository userRepository, JwtUtil jwtUtil,
                        VaultRepository vaultRepository, VaultPermissionRepository vaultPermissionRepository) {
        this.jwtUtil = jwtUtil;
        this.vaultRepository = vaultRepository;
        this.userRepository = userRepository;
        this.vaultPermissionRepository = vaultPermissionRepository;
    }

    public Vault createVault(String userId, VaultPostDTO vaultPostDTO) {
        if (vaultRepository.findVaultByName(vaultPostDTO.getName()) != null) {
            return null;
        }

        Vault newVault = new Vault();
        newVault.setName(vaultPostDTO.getName());
        newVault.setState(vaultPostDTO.getState());
        newVault.setOwner(userRepository.findUserById(Long.valueOf(userId)));
        newVault.setCreatedAt(LocalDateTime.now());

        User ownerUser = userRepository.findUserById(Long.valueOf(userId));
        VaultPermission permission = new VaultPermission();
        permission.setVault(newVault);
        permission.setUser(ownerUser);
        permission.setRole(Role.OWNER);
        permission.setGrantedAt(LocalDateTime.now());

        Vault savedVault = vaultRepository.save(newVault);
        permission.setVault(savedVault);
        vaultPermissionRepository.save(permission);

        return savedVault;
    }

    public List<Vault> getVaultsForUser(String userId) {
        User user = userRepository.findUserById(Long.valueOf(userId));
        return vaultRepository.findVaultsByUserPermission(user);
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

        vaultPermissionRepository.save(permission);
    }

    public Vault getVaultById(Long id) {
        return vaultRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vault not found"));
    }

    public void deleteVault(Long vaultId, String userId) {
        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vault not found."));

        if (!vault.getOwner().getId().toString().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can delete the vault.");
        }

        vaultRepository.delete(vault);
    }
    
    @Transactional
    public void updateVault(Long vaultId, String userId, VaultPostDTO updatedData) {
        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vault not found"));

        if (!vault.getOwner().getId().toString().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can update the vault.");
        }

        if (updatedData.getName() != null) {
            vault.setName(updatedData.getName());
        }
        if (updatedData.getState() != null) {
            vault.setState(updatedData.getState());
        }

        vaultRepository.save(vault);
    }
}