package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.Role;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.security.SecurityConfig;
import ch.uzh.ifi.hase.soprafs24.service.VaultService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VaultPermissionController.class)
@Import(SecurityConfig.class)
public class VaultPermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VaultService vaultService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private VaultRepository vaultRepository;

    @MockBean
    private VaultPermissionRepository vaultPermissionRepository;

    @MockBean
    private UserRepository userRepository;

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    
    // Test for GET "/vaults/{vaultId}/settings/permissions"
    @Test
    public void getPermissions_validRequest_Ok() throws Exception {
        Long vaultId = 1L;
        String userId = "1";

        VaultPermissionDTO permissionDTO = new VaultPermissionDTO();
        permissionDTO.setUserId(2L);
        permissionDTO.setRole("EDITOR");

        given(jwtUtil.extractId("validToken")).willReturn(userId);
        given(jwtUtil.validateToken("validToken", userId)).willReturn(true);
        given(vaultService.getPermissionsForVault(vaultId)).willReturn(List.of(permissionDTO));

        MockHttpServletRequestBuilder getRequest = get("/vaults/{vaultId}/settings/permissions", vaultId)
                .header("Authorization", "Bearer validToken");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId", is(2)))
                .andExpect(jsonPath("$[0].role", is("EDITOR")));
    }

    // Test for GET "/vaults/{vaultId}/settings/permissions"
    @Test
    public void getPermissions_unauthorized_Unauthorized() throws Exception {

        MockHttpServletRequestBuilder getRequest = get("/vaults/{vaultId}/settings/permissions", 1L);

        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
    }

    // Test for POST "/vaults/{vaultId}/settings/permissions"
    @Test
    public void addPermission_validRequest_Ok() throws Exception {
        Long vaultId = 1L;
        String userId = "1";

        VaultPermissionDTO permissionDTO = new VaultPermissionDTO();
        permissionDTO.setUserId(2L);
        permissionDTO.setRole("EDITOR");

        VaultPermissionDTO updatedPermission = new VaultPermissionDTO();
        updatedPermission.setUserId(2L);
        updatedPermission.setRole("EDITOR");

        given(jwtUtil.extractId("validToken")).willReturn(userId);
        given(jwtUtil.validateToken("validToken", userId)).willReturn(true);
        given(vaultService.getPermissionsForVault(vaultId)).willReturn(List.of(updatedPermission));


        MockHttpServletRequestBuilder postRequest = post("/vaults/{vaultId}/settings/permissions", vaultId)
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(permissionDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId", is(2)))
                .andExpect(jsonPath("$[0].role", is("EDITOR")));
    }

    // Test for DELEE "/vaults/{vaultId}/settings/permissions/{userId}"
    @Test
    public void deletePermission_validRequestAsOwner_Ok() throws Exception {
        Long vaultId = 1L;
        Long targetUserId = 2L;
        String currentUserId = "1";

        User owner = new User();
        owner.setId(1L);

        User targetUser = new User();
        targetUser.setId(2L);

        Vault vault = new Vault();
        vault.setId(vaultId);
        vault.setOwner(owner);

        VaultPermission permission = new VaultPermission();
        Role role = Role.OWNER;
        permission.setUser(targetUser);
        permission.setVault(vault);
        permission.setRole(role);

        given(jwtUtil.extractId("validToken")).willReturn(currentUserId);
        given(jwtUtil.validateToken("validToken", currentUserId)).willReturn(true);
        given(vaultRepository.findById(vaultId)).willReturn(Optional.of(vault));
        given(userRepository.findUserById(targetUserId)).willReturn(targetUser);
        given(vaultPermissionRepository.findByVaultAndUser(vault, targetUser)).willReturn(Optional.of(permission));


        MockHttpServletRequestBuilder deleteRequest = delete("/vaults/{vaultId}/settings/permissions/{userId}", vaultId, targetUserId)
                .header("Authorization", "Bearer validToken");

        mockMvc.perform(deleteRequest)
                .andExpect(status().isOk())
                .andExpect(content().string("Permission deleted"));
    }

    // Test for DELEE "/vaults/{vaultId}/settings/permissions/{userId}"
    @Test
    public void deletePermission_tryRemoveOwner_Forbidden() throws Exception {
        Long vaultId = 1L;
        Long targetUserId = 2L;
        String currentUserId = "2";

        User owner = new User();
        owner.setId(1L);

        User targetUser = new User();
        targetUser.setId(2L);

        Vault vault = new Vault();
        vault.setId(vaultId);
        vault.setOwner(owner);

        VaultPermission permission = new VaultPermission();
        Role role = Role.OWNER;
        permission.setUser(targetUser);
        permission.setVault(vault);
        permission.setRole(role);

        given(jwtUtil.extractId("validToken")).willReturn(currentUserId);
        given(jwtUtil.validateToken("validToken", currentUserId)).willReturn(true);
        given(vaultRepository.findById(vaultId)).willReturn(Optional.of(vault));
        given(userRepository.findUserById(targetUserId)).willReturn(targetUser);
        given(vaultPermissionRepository.findByVaultAndUser(vault, targetUser)).willReturn(Optional.of(permission));


        MockHttpServletRequestBuilder deleteRequest = delete("/vaults/{vaultId}/settings/permissions/{userId}", vaultId, targetUserId)
                .header("Authorization", "Bearer validToken");

        mockMvc.perform(deleteRequest)
                .andExpect(status().isForbidden())
                .andExpect(content().string("Only the vault owner can remove permissions"));
    }


    // Test for PUT "/vaults/{vaultId}"
    @Test
    public void updateVault_validRequest_Ok() throws Exception {
        Long vaultId = 1L;

        VaultPostDTO vaultPostDTO = new VaultPostDTO();
        vaultPostDTO.setName("Updated Vault");

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(vaultService.updateVault(Mockito.any(), Mockito.any(), Mockito.any())).willReturn(true);

        MockHttpServletRequestBuilder putRequest = put("/vaults/{vaultId}", vaultId)
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(vaultPostDTO));

        mockMvc.perform(putRequest)
                .andExpect(status().isOk());
    }

    // Test for PUT "/vaults/{vaultId}"
    @Test
    public void updateVault_unauthorizedUser_Forbidden() throws Exception {
        Long vaultId = 1L;
        String userId = "1";

        VaultPostDTO vaultPostDTO = new VaultPostDTO();
        vaultPostDTO.setName("Updated Vault");

        given(jwtUtil.extractId("validToken")).willReturn(userId);
        given(jwtUtil.validateToken("validToken", userId)).willReturn(true);
        given(vaultService.updateVault(vaultId, vaultPostDTO, Long.parseLong(userId))).willReturn(false);


        MockHttpServletRequestBuilder putRequest = put("/vaults/{vaultId}", vaultId)
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(vaultPostDTO));

        mockMvc.perform(putRequest)
                .andExpect(status().isForbidden());
    }


    // Test for DELETE "/vaults/{vaultId}/settings/delete"
    @Test
    public void deleteVault_validRequest_NoContent() throws Exception {
        Long vaultId = 1L;
        String userId = "1";

        given(jwtUtil.extractId("validToken")).willReturn(userId);
        given(jwtUtil.validateToken("validToken", userId)).willReturn(true);
        given(vaultService.deleteVault(vaultId, Long.parseLong(userId))).willReturn(true);


        MockHttpServletRequestBuilder deleteRequest = delete("/vaults/{vaultId}/settings/delete", vaultId)
                .header("Authorization", "Bearer validToken");

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNoContent());
    }


    // Test for DELETE "/vaults/{vaultId}/settings/delete"
    @Test
    public void deleteVault_unauthorizedUser_Forbidden() throws Exception {
        Long vaultId = 1L;
        String userId = "1";

        given(jwtUtil.extractId("validToken")).willReturn(userId);
        given(jwtUtil.validateToken("validToken", userId)).willReturn(true);
        given(vaultService.deleteVault(vaultId, Long.parseLong(userId))).willReturn(false);


        MockHttpServletRequestBuilder deleteRequest = delete("/vaults/{vaultId}/settings/delete", vaultId)
                .header("Authorization", "Bearer validToken");

        mockMvc.perform(deleteRequest)
                .andExpect(status().isForbidden());
    }
}