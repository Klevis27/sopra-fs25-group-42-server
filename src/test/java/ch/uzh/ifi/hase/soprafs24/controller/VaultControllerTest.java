package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultPostDTO;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VaultController.class)
@Import(SecurityConfig.class)
public class VaultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VaultService vaultService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private VaultRepository vaultRepository;

    @MockBean
    private UserRepository userRepository;

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // Test for POST "/vaults"
    @Test
    public void createVault_validInput_Created() throws Exception {
        VaultPostDTO vaultPostDTO = new VaultPostDTO();
        vaultPostDTO.setName("Test Vault");

        Vault createdVault = new Vault();
        createdVault.setId(1L);
        createdVault.setName("Test Vault");

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(vaultService.createVault(Mockito.anyString(), Mockito.any())).willReturn(createdVault);

        MockHttpServletRequestBuilder postRequest = post("/vaults")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(vaultPostDTO));

        
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Registration successful")))
                .andExpect(jsonPath("$.id", is("1")));
    }

    // Test for POST "/vaults"
    @Test
    public void createVault_unauthorized_Unauthorized() throws Exception {
        VaultPostDTO vaultPostDTO = new VaultPostDTO();
        vaultPostDTO.setName("Test Vault");

        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.anyString())).willReturn(false);

         MockHttpServletRequestBuilder postRequest = post("/vaults")
                .header("Authorization", "Bearer invalidToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(vaultPostDTO));
        
        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    // Test for POST "/vaults"
    @Test
    public void createVault_duplicateName_Conflict() throws Exception {
        VaultPostDTO vaultPostDTO = new VaultPostDTO();
        vaultPostDTO.setName("Duplicate Vault");

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(vaultService.createVault(Mockito.anyString(), Mockito.any())).willReturn(null);

        MockHttpServletRequestBuilder postRequest = post("/vaults")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(vaultPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.Error", is("Creation of vault failed because vault name was already taken")));
    }

    // Test for GET "/vaults"
    @Test
    public void getAllVaults_validToken_Ok() throws Exception {
        Vault vault = new Vault();
        vault.setId(1L);
        vault.setName("Test Vault");
        VaultDTO vaultDTO = VaultDTO.fromEntity(vault);

        // This is how one should be able to create a DTO
        // vaultDTO.setId(1L);
        // vaultDTO.setName("Test Vault");

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.anyString())).willReturn(true);
        given(vaultService.getVaultsForUser(Mockito.anyString())).willReturn(List.of(vaultDTO));


        MockHttpServletRequestBuilder getRequest = get("/vaults")
                .header("Authorization", "Bearer validToken");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Vault")));
    }

    // Test for GET "/vaults"
    @Test
    public void getAllVaults_invalidToken_Unauthorized() throws Exception {
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.anyString())).willReturn(false);


        MockHttpServletRequestBuilder getRequest = get("/vaults")
                .header("Authorization", "Bearer invalidToken");

        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
    }

    // Test for GET "/vaults/{vault_id}"
    @Test
    public void getVaultById_validRequest_Ok() throws Exception {
        Vault vault = new Vault();
        vault.setId(1L);
        vault.setName("Test Vault");

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.anyString())).willReturn(true);
        given(vaultRepository.findVaultById(Mockito.anyLong())).willReturn(vault);


        MockHttpServletRequestBuilder getRequest = get("/vaults/{vault_id}", 1L)
                .header("Authorization", "Bearer validToken");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Vault")));
    }

    // Test for GET "/vaults/{vault_id}"
    @Test
    public void getVaultById_invalidToken_Unauthorized() throws Exception {
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.anyString())).willReturn(false);


        MockHttpServletRequestBuilder getRequest = get("/vaults/{vault_id}", 1L)
                .header("Authorization", "Bearer invalidToken");

        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
    }

    // Test for GET "/vaults/{vault_id}/name"
    @Test
    public void getVaultName_validRequest_Ok() throws Exception {
        Vault vault = new Vault();
        vault.setId(1L);
        vault.setName("Test Vault");

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.anyString())).willReturn(true);
        given(vaultRepository.findVaultById(Mockito.anyLong())).willReturn(vault);


        MockHttpServletRequestBuilder getRequest = get("/vaults/{vault_id}/name", 1L)
                .header("Authorization", "Bearer validToken");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Vault")));
    }


    // Test for GET "/vaults/{vault_id}/name"
    @Test
    public void getVaultName_vaultNotFound_NotFound() throws Exception {
        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(vaultRepository.findVaultById(Mockito.anyLong())).willReturn(null);

        MockHttpServletRequestBuilder getRequest = get("/vaults/{vault_id}/name", 999L)
                .header("Authorization", "Bearer validToken");

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }
}