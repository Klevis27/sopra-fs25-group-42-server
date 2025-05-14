package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultInvitationDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultInviteCreateDTO;
import ch.uzh.ifi.hase.soprafs24.service.VaultInvitationService;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/invite")
public class VaultInvitationController {

    private final VaultInvitationService invitationService;
    private final JwtUtil jwtUtil;

    public VaultInvitationController(VaultInvitationService invitationService, JwtUtil jwtUtil) {
        this.invitationService = invitationService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/me")
    public ResponseEntity<List<VaultInvitationDTO>> getMyInvites(HttpServletRequest request) {
        String userToken = extractTokenFromRequest(request);
        if (userToken == null || !jwtUtil.validateToken(userToken, jwtUtil.extractId(userToken))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Long userId = Long.parseLong(jwtUtil.extractId(userToken));

        // String authHeader = request.getHeader("Authorization");
        // if (authHeader == null) {
        // throw new RuntimeException("Missing Authorization header");
        // }
        // Long userId = JwtUtil.getUserIdFromToken(authHeader);

        List<VaultInvitationDTO> vaultInvitations = invitationService.getInvitationsForUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(vaultInvitations);
    }

    @PostMapping("/{token}/accept")
    public ResponseEntity<Map<String, String>> acceptInvite(@PathVariable String token, HttpServletRequest request) {
        String userToken = extractTokenFromRequest(request);
        if (userToken == null || !jwtUtil.validateToken(userToken, jwtUtil.extractId(userToken))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Long userId = Long.parseLong(jwtUtil.extractId(userToken));

        // Old code, left it in in case someonee needs it
        // String authHeader = request.getHeader("Authorization");
        // if (authHeader == null) {
        // throw new RuntimeException("Missing Authorization header");
        // }
        // Long userId = JwtUtil.getUserIdFromToken(authHeader);

        invitationService.acceptInvitation(token, userId);
        return ResponseEntity.ok().body(Map.of("message", "Everything ok"));
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createInvite(@RequestBody VaultInviteCreateDTO dto) {

        if (dto.getUserId() == null || dto.getVaultId() == null || dto.getRole() == null) {
            return ResponseEntity.status((HttpStatus.BAD_REQUEST)).body(null);
        }
        invitationService.createInvitation(dto.getUserId(), dto.getVaultId(), dto.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Successfully created an invitation"));
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