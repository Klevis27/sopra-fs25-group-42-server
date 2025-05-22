package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.service.NoteStateService;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.BDDMockito.given;


@WebMvcTest(NoteStatesController.class)
@AutoConfigureMockMvc(addFilters = false) 
public class NoteStatesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteStateService noteStateService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    public void getNoteState_validInput_returnsState() throws Exception {
        Long noteId = 1L;
        byte[] content = new byte[] { 1, 2, 3 };

        Mockito.when(noteStateService.loadState(noteId)).thenReturn(content);
        Mockito.when(jwtUtil.validateToken(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        mockMvc.perform(get("/notes/{noteId}/state", noteId)
                .header("Authorization", "Bearer validToken"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(content));
    }

    @Test
    public void getNoteState_emptyState_returnsEmpty() throws Exception {
        Long noteId = 1L;
        byte[] emptyContent = new byte[0];

        given(noteStateService.loadState(noteId)).willReturn(emptyContent);
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.anyString())).willReturn(true);

        mockMvc.perform(get("/notes/1/state")
                .header("Authorization", "Bearer validToken"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(emptyContent));
    }

    @Test
    public void upsertNoteState_updateSuccess_returnsNoContent() throws Exception {
        Long noteId = 1L;
        byte[] content = new byte[] { 1, 2, 3 };

        Mockito.when(noteStateService.updateNoteStateContent(Mockito.any())).thenReturn(true);
        Mockito.when(jwtUtil.validateToken(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        mockMvc.perform(put("/notes/{noteId}/state", noteId)
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(content))
                .andExpect(status().isNoContent());
    }

    @Test
    public void upsertNoteState_createSuccess_returnsCreated() throws Exception {
        Long noteId = 1L;
        byte[] content = new byte[] { 1, 2, 3 };

        Mockito.when(noteStateService.updateNoteStateContent(Mockito.any())).thenReturn(false);
        Mockito.when(noteStateService.createNoteState(Mockito.any())).thenReturn(true);
        Mockito.when(jwtUtil.validateToken(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        mockMvc.perform(put("/notes/{noteId}/state", noteId)
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(content))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/notes/" + noteId + "/state"));
    }

    @Test
    public void upsertNoteState_neitherUpdateNorCreate_returnsNotFound() throws Exception {
        Long noteId = 1L;
        byte[] content = new byte[] { 1, 2, 3 };

        Mockito.when(noteStateService.updateNoteStateContent(Mockito.any())).thenReturn(false);
        Mockito.when(noteStateService.createNoteState(Mockito.any())).thenReturn(false);
        Mockito.when(jwtUtil.validateToken(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        mockMvc.perform(put("/notes/{noteId}/state", noteId)
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(content))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getNoteState_unauthorized_returnsUnauthorized() throws Exception {
        Long noteId = 1L;

        Mockito.when(jwtUtil.validateToken(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        mockMvc.perform(get("/notes/{noteId}/state", noteId)
                .header("Authorization", "Bearer invalidToken"))
                .andExpect(status().isUnauthorized());
    }
}