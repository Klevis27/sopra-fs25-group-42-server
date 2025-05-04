package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultInvitationDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultInviteCreateDTO;
import ch.uzh.ifi.hase.soprafs24.service.VaultInvitationService;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/invite")
public class VaultInvitationController {

    private final VaultInvitationService invitationService;

    public VaultInvitationController(VaultInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @GetMapping("/me")
    public List<VaultInvitationDTO> getMyInvites(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            throw new RuntimeException("Missing Authorization header");
        }

        Long userId = JwtUtil.getUserIdFromToken(authHeader);
        return invitationService.getInvitationsForUser(userId);
    }

    @PostMapping("/{token}/accept")
    public void acceptInvite(@PathVariable String token, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            throw new RuntimeException("Missing Authorization header");
        }

        Long userId = JwtUtil.getUserIdFromToken(authHeader);
        invitationService.acceptInvitation(token, userId);
    }

    @PostMapping("/create")
    public void createInvite(@RequestBody VaultInviteCreateDTO dto) {
        invitationService.createInvitation(dto.getUserId(), dto.getVaultId(), dto.getRole());
    }
}