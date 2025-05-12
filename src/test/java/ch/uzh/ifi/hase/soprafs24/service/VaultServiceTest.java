package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.entity.VaultPermission;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultPermissionRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultPermissionDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultPostDTO;
import ch.uzh.ifi.hase.soprafs24.constant.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class VaultServiceTest {
    @Mock
    private VaultRepository vaultRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VaultPermissionRepository vaultPermissionRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private VaultService vaultService;

    private User testUser;
    private Vault testVault;
    private VaultPostDTO testVaultPostDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        // setup test vault
        testVault = new Vault();
        testVault.setId(1L);
        testVault.setName("testVault");
        testVault.setOwner(testUser);
        testVault.setCreatedAt(LocalDateTime.now());

        // setup test DTO
        testVaultPostDTO = new VaultPostDTO();
        testVaultPostDTO.setName("testVault");

        when(userRepository.findUserById(1L)).thenReturn(testUser);
        when(vaultRepository.save(any())).thenReturn(testVault);
    }
    //-------------------------------------------------------------//
    //test if created vault exists, has correct data
    //has correct owner
    @Test
    void createVault_validInputs_success() {
        // when
        when(vaultRepository.findVaultByName(any())).thenReturn(null);
        Vault createdVault = vaultService.createVault("1", testVaultPostDTO);

        // then
        verify(vaultRepository, times(1)).save(any());
        verify(vaultPermissionRepository, times(1)).save(any());

        assertEquals(testVault.getId(), createdVault.getId());
        assertEquals(testVault.getName(), createdVault.getName());
        assertEquals(testUser, createdVault.getOwner());
        assertNotNull(createdVault.getCreatedAt());
    }
    //-------------------------------------------------------------//
    //test if error is thrown if name already exists
    @Test
    void createVault_duplicateName_returnsNull() {
        // when
        when(vaultRepository.findVaultByName(any())).thenReturn(testVault);
        Vault createdVault = vaultService.createVault("1", testVaultPostDTO);

        // then
        verify(vaultRepository, times(0)).save(any());
        assertNull(createdVault);
    }
    //-------------------------------------------------------------//
    //test if get of vault works for fetching by invalid ID for permision
    @Test
    void getPermissionsForVault_validVault_returnsPermissions() {
        // setup
        VaultPermission permission = new VaultPermission();
        permission.setVault(testVault);
        permission.setUser(testUser);
        permission.setRole(Role.OWNER);
        permission.setGrantedAt(LocalDateTime.now());

        List<VaultPermission> permissions = new ArrayList<>();
        permissions.add(permission);

        // when
        when(vaultRepository.findById(1L)).thenReturn(Optional.of(testVault));
        when(vaultPermissionRepository.findByVault(testVault)).thenReturn(permissions);
        List<VaultPermissionDTO> result = vaultService.getPermissionsForVault(1L);

        // then
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getUserId());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        assertEquals(Role.OWNER.name(), result.get(0).getRole());
    }
    //-------------------------------------------------------------//
    //test if get of vault works for fetching by invalid ID
    @Test
    void getPermissionsForVault_invalidVault_throwsException() {
        // when
        when(vaultRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        assertThrows(ResponseStatusException.class, () -> vaultService.getPermissionsForVault(1L));
    }
    //-------------------------------------------------------------//
    //test if permission for Vault is added or updated
    @Test
    void addOrUpdatePermission_newPermission_success() {
        // setup
        VaultPermissionDTO dto = new VaultPermissionDTO();
        dto.setUserId(1L);
        dto.setUsername("testUser");
        dto.setRole("EDITOR");

        // when
        when(vaultRepository.findById(1L)).thenReturn(Optional.of(testVault));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(vaultPermissionRepository.findByVaultAndUser(testVault, testUser)).thenReturn(Optional.empty());

        vaultService.addOrUpdatePermission(1L, dto);

        // then
        verify(vaultPermissionRepository, times(1)).save(any());
    }
    //--------------------------------------------------------------//
    //delete vault by owner
    @Test
    void deleteVault_validOwner_success() {
        // when
        when(vaultRepository.findById(1L)).thenReturn(Optional.of(testVault));
        boolean result = vaultService.deleteVault(1L, 1L);

        // then
        assertTrue(result);
        verify(vaultRepository, times(1)).delete(testVault);
    }
    //--------------------------------------------------------------//
    //delete vault by non-owner
    @Test
    void deleteVault_nonOwner_failure() {
        // when
        when(vaultRepository.findById(1L)).thenReturn(Optional.of(testVault));
        boolean result = vaultService.deleteVault(1L, 2L);

        // then
        assertFalse(result);
        verify(vaultRepository, times(0)).delete(any());
    }
    //--------------------------------------------------------------//
    //delete vault by invalid vault ID
    @Test
    void deleteVault_invalidVault_failure() {
        // when
        when(vaultRepository.findById(1L)).thenReturn(Optional.empty());
        boolean result = vaultService.deleteVault(1L, 1L);

        // then
        assertFalse(result);
        verify(vaultRepository, times(0)).delete(any());
    }
    //--------------------------------------------------------------//
    //test if vault is returned properly after update by owner
    @Test
    void updateVault_validOwner_success() {
        // setup
        VaultPostDTO updatedData = new VaultPostDTO();
        updatedData.setName("updatedName");

        // when
        when(vaultRepository.findById(1L)).thenReturn(Optional.of(testVault));
        boolean result = vaultService.updateVault(1L, updatedData, 1L);

        // then
        assertTrue(result);
        assertEquals("updatedName", testVault.getName());
        verify(vaultRepository, times(1)).save(testVault);
    }
    //--------------------------------------------------------------//
    //test if update fails if vault is updated by non-owner
    @Test
    void updateVault_nonOwner_failure() {
        // setup
        VaultPostDTO updatedData = new VaultPostDTO();
        updatedData.setName("updatedName");

        // when
        when(vaultRepository.findById(1L)).thenReturn(Optional.of(testVault));
        boolean result = vaultService.updateVault(1L, updatedData, 2L);

        // then
        assertFalse(result);
        assertEquals("testVault", testVault.getName());
        verify(vaultRepository, times(0)).save(any());
    }
    //--------------------------------------------------------------//
    //test if user gets all vaults with permision
    // TODO: has to be reviewed
    @Test
    void getVaultsForUser_validUser_returnsVaults() {
        // setup
        VaultPermission permission = new VaultPermission();
        permission.setVault(testVault);
        permission.setUser(testUser);
        permission.setRole(Role.OWNER);
        permission.setGrantedAt(LocalDateTime.now());

        List<VaultPermission> permissions = new ArrayList<>();
        permissions.add(permission);

        // when
        when(vaultPermissionRepository.findByUserId(1L)).thenReturn(permissions);
        List<VaultDTO> result = vaultService.getVaultsForUser("1");

        // then
        assertEquals(1, result.size());
        //assertion not possible no getID getName or getRole
/*        assertEquals(testVault.getId(), result.get(0).getId());
        assertEquals(testVault.getName(), result.get(0).getName());
        assertEquals(Role.OWNER.name(), result.get(0).getRole());*/
    }
    //--------------------------------------------------------------//
    //test if user gets empty list if no vaults are found
    @Test
    void getVaultsForUser_noVaults_returnsEmptyList() {
        // when
        when(vaultPermissionRepository.findByUserId(1L)).thenReturn(new ArrayList<>());
        List<VaultDTO> result = vaultService.getVaultsForUser("1");

        // then
        assertTrue(result.isEmpty());
    }

}
