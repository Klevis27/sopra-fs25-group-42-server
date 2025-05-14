package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultInvitationDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultInviteCreateDTO;
import ch.uzh.ifi.hase.soprafs24.security.SecurityConfig;
import ch.uzh.ifi.hase.soprafs24.service.VaultInvitationService;
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

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VaultInvitationController.class)
@Import(SecurityConfig.class)
public class VaultInvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VaultInvitationService invitationService;

    @MockBean
    private JwtUtil jwtUtil;

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // Test for GET "/invite/me"
    @Test
    public void getMyInvites_validToken_Ok() throws Exception {
        Long userId = 1L;
        
        VaultInvitationDTO invite1 = new VaultInvitationDTO();
        invite1.setId(1L);
        VaultInvitationDTO invite2 = new VaultInvitationDTO();
        invite2.setId(2L);
        List<VaultInvitationDTO> expectedInvites = Arrays.asList(invite1, invite2);

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(invitationService.getInvitationsForUser(userId)).willReturn(expectedInvites);

        MockHttpServletRequestBuilder getRequest = get("/invite/me")
                .header("Authorization", "Bearer validToken");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    // Test for "/invite/me"
    @Test
    public void getMyInvites_missingToken_Unauthorized() throws Exception {

        MockHttpServletRequestBuilder getRequest = get("/invite/me");

        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
    }

    // Test for POST "/invite/{token}/accept"
    @Test
    public void acceptInvite_validTokenAndInvite_Ok() throws Exception {
        Long userId = 1L;
        String inviteToken = "Invite Token";

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);

        MockHttpServletRequestBuilder postRequest = post("/invite/{token}/accept", inviteToken)
                .header("Authorization", "Bearer validToken");
    
        mockMvc.perform(postRequest)
                .andExpect(status().isOk());
        
        Mockito.verify(invitationService, Mockito.times(1))
               .acceptInvitation(inviteToken, userId);
    }

    // Test for POST "/invite/{token}/accept"
    @Test
    public void acceptInvite_missingToken_Unauthoirzed() throws Exception {

        MockHttpServletRequestBuilder postrequest = post("/invite/{token}/accept", "invite123");

        mockMvc.perform(postrequest)
                .andExpect(status().isUnauthorized());
    }

    // Test for POST "/invite/create"
    @Test
    public void createInvite_validInput_Created() throws Exception {
        VaultInviteCreateDTO dto = new VaultInviteCreateDTO();
        dto.setUserId(2L);
        dto.setVaultId(1L);
        dto.setRole("EDITOR");

        Mockito.doNothing().when(invitationService)
               .createInvitation(dto.getUserId(), dto.getVaultId(), dto.getRole());

        MockHttpServletRequestBuilder postRequest = post("/invite/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated());
        
        Mockito.verify(invitationService, Mockito.times(1))
               .createInvitation(dto.getUserId(), dto.getVaultId(), dto.getRole());
    }

    // Test for POST "/invite/create"
    @Test
    public void createInvite_invalidInput_BadRequest() throws Exception {
        VaultInviteCreateDTO invalidDto = new VaultInviteCreateDTO();

        mockMvc.perform(post("/invite/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}