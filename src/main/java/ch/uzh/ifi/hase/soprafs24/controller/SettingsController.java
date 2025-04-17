package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultPermissionDTO;
import ch.uzh.ifi.hase.soprafs24.service.VaultService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class SettingsController {

    private final VaultService vaultService;
    private final JwtUtil jwtUtil;

    public SettingsController(VaultService vaultService, JwtUtil jwtUtil) {
        this.vaultService = vaultService;
        this.jwtUtil = jwtUtil;
    }

    // GET /vaults/{vaultId}/permissions
    @GetMapping("/vaults/{vaultId}/settings/permissions")
    public ResponseEntity<List<VaultPermissionDTO>> getPermissions(@PathVariable Long vaultId, HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        List<VaultPermissionDTO> result = vaultService.getPermissionsForVault(vaultId);
        return ResponseEntity.ok(result);
    }

    // POST /vaults/{vaultId}/permissions
    @PostMapping("/vaults/{vaultId}/settings/permissions")
    public ResponseEntity<List<VaultPermissionDTO>> addPermission(
            @PathVariable Long vaultId,
            @RequestBody VaultPermissionDTO dto,
            HttpServletRequest request) {

        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Yeni veya var olan kullanıcıya rol atama
        vaultService.addOrUpdatePermission(vaultId, dto);

        // Güncellenmiş listeyi geri dönüyoruz
        List<VaultPermissionDTO> updated = vaultService.getPermissionsForVault(vaultId);
        return ResponseEntity.ok(updated);
    }

    // İsteğin Authorization Header'ından token ayıklama
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " kısmını kırp
        }
        return null;
    }
}