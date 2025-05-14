package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultInvitationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VaultInvitationServiceTest {

    @Mock
    private VaultInvitationRepository invitationRepository;

    @Mock
    private VaultRepository vaultRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VaultPermissionRepository permissionRepository;

    @InjectMocks
    private VaultInvitationService invitationService;

    private User testUser;
    private Vault testVault;
    private VaultInvitation testInvitation;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        // Setup test vault
        testVault = new Vault();
        testVault.setId(1L);
        testVault.setName("testVault");

        // Setup test invitation
        testInvitation = new VaultInvitation();
        //testInvitation.setId(1L);
        //testInvitation.setToken("test-token");
        testInvitation.setTargetUser(testUser);
        testInvitation.setVault(testVault);
        testInvitation.setRole("EDITOR");
        testInvitation.setCreatedAt(LocalDateTime.now());
    }
    //-------------------------------------------------------------//
    //Verify that the service correctly retrieves invitations
    //for a user and maps them to DTOs.

    @Test
    void getInvitationsForUser_validUserId_returnsInvitations() {
        // Arrange
        List<VaultInvitation> invitations = List.of(testInvitation);
        when(invitationRepository.findByTargetUserId(1L)).thenReturn(invitations);

        // Act
        List<VaultInvitationDTO> result = invitationService.getInvitationsForUser(1L);

        // Assert
        assertEquals(1, result.size());
        VaultInvitationDTO dto = result.get(0);
        assertEquals(testInvitation.getId(), dto.getId());
        assertEquals(testInvitation.getToken(), dto.getToken());
        assertEquals(testVault.getName(), dto.getVaultName());
        assertEquals(testInvitation.getRole(), dto.getRole());
        assertEquals(testInvitation.getCreatedAt(), dto.getCreatedAt());

        verify(invitationRepository, times(1)).findByTargetUserId(1L);
    }
    //-------------------------------------------------------------//
    //Verify that the service correctly accepts an invitation.
    //rates a permission and deletes the invitation.

    @Test
    void acceptInvitation_validTokenAndUser_createsPermissionAndDeletesInvitation() {
        // Arrange
        when(invitationRepository.findByToken("test-token")).thenReturn(Optional.of(testInvitation));

        // Act
        invitationService.acceptInvitation("test-token", 1L);

        // Assert
        verify(permissionRepository, times(1)).save(any(VaultPermission.class));
        verify(invitationRepository, times(1)).delete(testInvitation);
    }
    //-------------------------------------------------------------//
    //Verify that the service throws an exception if the invitation
    //does not exist or the user is not the target user.

    @Test
    void acceptInvitation_invalidToken_throwsNotFound() {
        // Arrange
        when(invitationRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> invitationService.acceptInvitation("invalid-token", 1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Invitation not found", exception.getReason());
    }
    //-------------------------------------------------------------//
    //Verify that users can't accept invitations meant for others.
    @Test
    void acceptInvitation_wrongUser_throwsForbidden() {
        // Arrange
        when(invitationRepository.findByToken("test-token")).thenReturn(Optional.of(testInvitation));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> invitationService.acceptInvitation("test-token", 2L)); // Different user ID

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("This invitation is not for you", exception.getReason());
    }
    //-------------------------------------------------------------//
    //Verify that the service correctly creates a new invitation works with valid inputs.
    @Test
    void createInvitation_validInputs_createsInvitation() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(vaultRepository.findById(1L)).thenReturn(Optional.of(testVault));
        when(invitationRepository.findByTargetUserId(1L)).thenReturn(List.of()); // No existing invitations

        // Act
        invitationService.createInvitation(1L, 1L, "EDITOR");

        // Assert
        verify(invitationRepository, times(1)).save(any(VaultInvitation.class));
    }
    //-------------------------------------------------------------//
    //Verify that the service throws an exception if the invitation is duplicated
    @Test
    void createInvitation_duplicateInvitation_throwsConflict() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(vaultRepository.findById(1L)).thenReturn(Optional.of(testVault));
        when(invitationRepository.findByTargetUserId(1L)).thenReturn(List.of(testInvitation)); // Existing invitation

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> invitationService.createInvitation(1L, 1L, "EDITOR"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Already invited", exception.getReason());
    }
    //-------------------------------------------------------------//
    //Verify that the service throws an exception if the user or vault do not exist.
    @Test
    void createInvitation_invalidUserOrVault_throwsNotFound() {
        // User not found case
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class,
                () -> invitationService.createInvitation(99L, 1L, "EDITOR"));

        // Vault not found case
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(vaultRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class,
                () -> invitationService.createInvitation(1L, 99L, "EDITOR"));
    }

}