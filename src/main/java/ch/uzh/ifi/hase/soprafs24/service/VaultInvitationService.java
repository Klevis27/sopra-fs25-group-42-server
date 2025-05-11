package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultInvitationDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ch.uzh.ifi.hase.soprafs24.constant.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VaultInvitationService {

    private final VaultInvitationRepository invitationRepository;
    private final VaultRepository vaultRepository;
    private final UserRepository userRepository;
    private final VaultPermissionRepository permissionRepository;

    public VaultInvitationService(
            VaultInvitationRepository invitationRepository,
            VaultRepository vaultRepository,
            UserRepository userRepository,
            VaultPermissionRepository permissionRepository
    ) {
        this.invitationRepository = invitationRepository;
        this.vaultRepository = vaultRepository;
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<VaultInvitationDTO> getInvitationsForUser(Long userId) {
        List<VaultInvitation> invitations = invitationRepository.findByTargetUserId(userId);
        return invitations.stream().map(inv -> {
            VaultInvitationDTO dto = new VaultInvitationDTO();
            dto.setId(inv.getId());
            dto.setToken(inv.getToken());
            dto.setVaultName(inv.getVault().getName());
            dto.setRole(inv.getRole()); // still string in DTO
            dto.setCreatedAt(inv.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    public void acceptInvitation(String token, Long userId) {
        VaultInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));

        if (!invitation.getTargetUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This invitation is not for you");
        }

        VaultPermission permission = new VaultPermission();
        permission.setUser(invitation.getTargetUser());
        permission.setVault(invitation.getVault());
        permission.setRole(Role.valueOf(invitation.getRole()));
        permission.setGrantedAt(LocalDateTime.now()); // <-- kritik satır

        permissionRepository.save(permission);
        invitationRepository.delete(invitation);
    }

    public void createInvitation(Long targetUserId, Long vaultId, String role) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vault not found"));

        // check for duplicate invitation
        boolean alreadyInvited = invitationRepository.findByTargetUserId(targetUserId).stream()
                .anyMatch(inv -> inv.getVault().getId().equals(vaultId));
        if (alreadyInvited) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already invited");
        }

        VaultInvitation invitation = new VaultInvitation();
        invitation.setTargetUser(user);
        invitation.setVault(vault);
        invitation.setRole(role);

        invitationRepository.save(invitation);
    }
}