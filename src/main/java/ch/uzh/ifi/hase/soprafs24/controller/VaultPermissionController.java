package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.entity.VaultPermission;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.VaultPermissionRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultPermissionDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.VaultService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
public class VaultPermissionController {

    private final VaultService vaultService;
    private final JwtUtil jwtUtil;
    private final VaultRepository vaultRepository;
    private final VaultPermissionRepository vaultPermissionRepository;

    public VaultPermissionController(VaultService vaultService,
                                     JwtUtil jwtUtil,
                                     VaultRepository vaultRepository,
                                     VaultPermissionRepository vaultPermissionRepository) {
        this.vaultService = vaultService;
        this.jwtUtil = jwtUtil;
        this.vaultRepository = vaultRepository;
        this.vaultPermissionRepository = vaultPermissionRepository;
    }

    @GetMapping("/vaults/{vaultId}/settings/permissions")
    public ResponseEntity<List<VaultPermissionDTO>> getPermissions(@PathVariable Long vaultId, HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        List<VaultPermissionDTO> result = vaultService.getPermissionsForVault(vaultId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/vaults/{vaultId}/settings/permissions")
    public ResponseEntity<List<VaultPermissionDTO>> addPermission(
            @PathVariable Long vaultId,
            @RequestBody VaultPermissionDTO dto,
            HttpServletRequest request) {

        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        vaultService.addOrUpdatePermission(vaultId, dto);

        List<VaultPermissionDTO> updated = vaultService.getPermissionsForVault(vaultId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/vaults/{vaultId}/settings/permissions/{userId}")
    public ResponseEntity<?> deletePermission(@PathVariable Long vaultId,
                                              @PathVariable Long userId,
                                              HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long currentUserId = Long.parseLong(jwtUtil.extractId(token));
        Optional<Vault> vaultOptional = vaultRepository.findById(vaultId);
        if (vaultOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vault not found");
        }

        Vault vault = vaultOptional.get();
        if (!Objects.equals(vault.getOwner().getId(), currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the vault owner can remove permissions");
        }

        Optional<VaultPermission> permissionOpt = vaultPermissionRepository.findByVaultIdAndUserId(vaultId, userId);
        if (permissionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Permission not found");
        }

        if (permissionOpt.get().getRole().equals("OWNER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot remove OWNER role");
        }

        vaultPermissionRepository.delete(permissionOpt.get());
        return ResponseEntity.ok("Permission deleted");
    }

    @PutMapping("/vaults/{vaultId}")
    public ResponseEntity<Void> updateVault(@PathVariable Long vaultId,
                                            @RequestBody VaultPostDTO vaultPostDTO,
                                            HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        String userId = jwtUtil.extractId(token);

        if (token == null || !jwtUtil.validateToken(token, userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean updated = vaultService.updateVault(vaultId, vaultPostDTO, Long.parseLong(userId));

        if (!updated) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/vaults/{vaultId}/settings/delete")
    public ResponseEntity<Void> deleteVault(@PathVariable Long vaultId, HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        String userId = jwtUtil.extractId(token);

        if (token == null || !jwtUtil.validateToken(token, userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean deleted = vaultService.deleteVault(vaultId, Long.parseLong(userId));

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.noContent().build();
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}