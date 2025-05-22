package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Note;
import ch.uzh.ifi.hase.soprafs24.entity.NoteLink;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.security.SecurityConfig;
import ch.uzh.ifi.hase.soprafs24.service.NoteService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.service.VaultService;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.NoteLinkRepository;
import ch.uzh.ifi.hase.soprafs24.repository.NotePermissionRepository;
import ch.uzh.ifi.hase.soprafs24.repository.NoteRepository;
import ch.uzh.ifi.hase.soprafs24.repository.NoteStatesRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultPermissionRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NotePermissionDTO;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.Arrays;
import java.util.stream.Collectors;
import static org.hamcrest.Matchers.hasSize;
import org.mockito.verification.VerificationMode;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
@Import(SecurityConfig.class)
public class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private VaultService vaultService;

    @MockBean
    private NoteService noteService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private VaultRepository vaultRepository;

    @MockBean
    private VaultPermissionRepository vaultPermissionRepository;

    @MockBean
    private NoteRepository noteRepository;

    @MockBean
    private NoteStatesRepository noteStatesRepository;

    @MockBean
    private NoteLinkRepository noteLinkRepository;

    @MockBean
    private NotePermissionRepository notePermissionRepository;

    @MockBean
    private BCryptPasswordEncoder encoder;

    // Helper method to convert objects to JSON
    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        }
        catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }

    // Helper to create valid auth header
    private MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder builder) {
        return builder.header("Authorization", "Bearer validToken");
    }

    // Test for GET "/vaults/{vault_id}/notes"
    // Valid Input
    // Stellen Sie sicher, dass der Controller diese DTOs zurückgibt denn Json Kommt leer a
    // was wascheinlich ein Problem der Impklementierung NotestateController ist
    // warten auf Klevis
    @Test
    public void getNotes_validInput_Ok() throws Exception {
        // given
        Vault vault = new Vault();
        vault.setId(1L);

        Note note1 = new Note();
        note1.setId(1L);
        note1.setTitle("Note1");
        note1.setVault(vault);

        Note note2 = new Note();
        note2.setId(2L);
        note2.setTitle("Note2");
        note2.setVault(vault);

        List<Note> noteList = Arrays.asList(note1, note2);

        // Stellen Sie sicher, dass der Controller diese DTOs zurückgibt
        List<NotesGetDTO> dtoList = noteList.stream()
                .map(note -> {
                    NotesGetDTO dto = new NotesGetDTO();
                    dto.setId(note.getId());
                    dto.setTitle(note.getTitle());
                    return dto;
                })
                .collect(Collectors.toList());

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(vaultRepository.findById(1L)).willReturn(Optional.of(vault));
        given(noteRepository.findAllByVault(vault)).willReturn(noteList);


        MockHttpServletRequestBuilder getRequest = get("/vaults/1/notes")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Note1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Note2")));
    }

    // Test for GET "/vaults/{vault_id}/notes"
    // User unauthorized
    @Test
    public void getNotes_userUnauthorized_Unauthorized() throws Exception {
        // given

        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(false);

        MockHttpServletRequestBuilder getRequest = get("/vaults/1/notes")
                .header("Authorization", "Bearer invalidToken")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // Test for GET "/vaults/{vault_id}/notes"
    // vault is empty
    @Test
    public void getNotes_vaultNotFound_NotFound() throws Exception {
        // given

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);

        MockHttpServletRequestBuilder getRequest = get("/vaults/1/notes")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------------------------\\

    // Test for GET "/vaults/{vault_id}/note_links"
    // valid Input
    @Test
    public void getNoteLinks_validInput_Ok() throws Exception {
        // given
        Vault vault = new Vault();
        vault.setId(1L);

        Note note1 = new Note();
        note1.setId(1L);
        note1.setTitle("Note1");
        note1.setVault(vault);

        Note note2 = new Note();
        note2.setId(2L);
        note2.setTitle("Note2");
        note2.setVault(vault);

        NoteLink noteLink1 = new NoteLink();
        noteLink1.setId(1L);
        noteLink1.setVault(vault);
        noteLink1.setSourceNote(note1);
        noteLink1.setTargetNote(note2);

        NoteLinksGetDTO noteLinksGetDTO1 = new NoteLinksGetDTO();
        noteLinksGetDTO1.setId(1L);
        noteLinksGetDTO1.setSourceNoteId(note1);
        noteLinksGetDTO1.setTargetNoteId(note2);

        NoteLink noteLink2 = new NoteLink();
        noteLink2.setId(2L);
        noteLink2.setVault(vault);
        noteLink2.setSourceNote(note2);
        noteLink2.setTargetNote(note1);

        NoteLinksGetDTO noteLinksGetDTO2 = new NoteLinksGetDTO();
        noteLinksGetDTO2.setId(1L);
        noteLinksGetDTO2.setSourceNoteId(note2);
        noteLinksGetDTO2.setTargetNoteId(note1);

        List<NoteLink> links = List.of(noteLink1, noteLink2);

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(vaultRepository.findById(1L)).willReturn(Optional.of(vault));
        given(noteLinkRepository.findAllByVault(vault)).willReturn(links);

        MockHttpServletRequestBuilder getRequest = get("/vaults/1/note_links")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                // .andExpect(jsonPath("$[0].vault.id", is(1)))
                .andExpect(jsonPath("$[0].sourceNoteId", is(1)))
                .andExpect(jsonPath("$[0].targetNoteId", is(2)))
                .andExpect(jsonPath("$[1].id", is(2)))
                // .andExpect(jsonPath("$[1].vault.id", is(1)))
                .andExpect(jsonPath("$[1].sourceNoteId", is(2)))
                .andExpect(jsonPath("$[1].targetNoteId", is(1)));
    }

    @Test
    public void getNoteLinks_userUnauthorized_Unauthorized() throws Exception {
        // given

        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(false);

        MockHttpServletRequestBuilder getRequest = get("/vaults/1/note_links")
                .header("Authorization", "Bearer invalidToken")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getNoteLinks_vaultNotFound_NotFound() throws Exception {
        // given

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);

        MockHttpServletRequestBuilder getRequest = get("/vaults/1/note_links")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------------------------\\

    @Test
    public void createNote_validInput_Created() throws Exception {
        //given
        User owner = new User();
        owner.setId(1L);

        Vault vault = new Vault();
        vault.setOwner(owner);

        NotesPostDTO notesPostDTO = new NotesPostDTO();
        notesPostDTO.setId(1L);
        notesPostDTO.setTitle("TestDTO");

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(vaultRepository.findById(1L)).willReturn(Optional.of(vault));


        MockHttpServletRequestBuilder postRequest = post("/vaults/1/notes")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(notesPostDTO));

        mockMvc.perform(postRequest)
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void createNote_userUnauthorized_Unauthorized() throws Exception {
        //given
        NotesPostDTO notesPostDTO = new NotesPostDTO();

        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(false);

        MockHttpServletRequestBuilder postRequest = post("/vaults/1/notes")
                .header("Authorization", "Bearer invalidToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(notesPostDTO));

        mockMvc.perform(postRequest)
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void createNote_vaultNotFound_NotFound() throws Exception {
        //given
        NotesPostDTO notesPostDTO = new NotesPostDTO();


        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);

        MockHttpServletRequestBuilder postRequest = post("/vaults/1/notes")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(notesPostDTO));

        mockMvc.perform(postRequest)
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------------------------\\

    //Test for DELETE /notes/{note_id}
    @Test
    public void deleteNote_validInput_Ok() throws Exception {

        User owner = new User();
        owner.setId(1L);

        Vault vault = new Vault();
        vault.setOwner(owner);

        Note note = new Note();
        note.setVault(vault);

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(noteRepository.findById(1L)).willReturn(Optional.of(note));

        MockHttpServletRequestBuilder deleteRequest = delete("/notes/1")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(deleteRequest)
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void deleteNote_userUnauthorized_Unauthorized() throws Exception {

        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(false);

        MockHttpServletRequestBuilder deleteRequest = delete("/notes/1")
                .header("Authorization", "Bearer invalidToken")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(deleteRequest)
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteNote_noteNotFound_NotFound() throws Exception {


        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);

        MockHttpServletRequestBuilder deleteRequest = delete("/notes/1")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(deleteRequest)
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // Test Fails beacause it expects 401 Unauthrized but gets 403 Forbidden:
    @Test
    public void deleteNote_userIsNotOwner_Unauthorized() throws Exception {

        User owner = new User();
        owner.setId(1L);

        Vault vault = new Vault();
        vault.setOwner(owner);

        Note note = new Note();
        note.setVault(vault);

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("2");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("2"))).willReturn(true);
        given(noteRepository.findById(2L)).willReturn(Optional.of(note));

        MockHttpServletRequestBuilder deleteRequest = delete("/notes/2")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(deleteRequest)
                .andDo(print())
                .andExpect(status().isForbidden());// Change to .isForbidden?
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------------------------\\

    //Test for POST /notes/{noteId}/invite
    @Test
    public void inviteUserToNote_validInput_Ok() throws Exception {

        NotesInvitePostDTO notesInvitePostDTO = new NotesInvitePostDTO();
        notesInvitePostDTO.setUsername("TestName");
        notesInvitePostDTO.setRole("TestRole");

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);


        MockHttpServletRequestBuilder postRequest = post("/notes/1/invite")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(notesInvitePostDTO));

        mockMvc.perform(postRequest)
                .andDo(print())
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------------------------\\

    //Tests for GET /notes/{noteId}/permissions
    @Test
    public void getNotePermissions_validInput_Ok() throws Exception {

        List<NotePermissionDTO> permissions = new ArrayList<NotePermissionDTO>();

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(noteService.getNotePermissions(1L)).willReturn(permissions);


        MockHttpServletRequestBuilder getRequest = get("/notes/1/permissions")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getNotePermissions_userUnauthorized_Unauthorized() throws Exception {

        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(false);

        MockHttpServletRequestBuilder getRequest = get("/notes/1/permissions")
                .header("Authorization", "Bearer invalidToken")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // Additional tests for NoteControllerTest.java

    @Test
    public void createNoteLink_validInput_Created() throws Exception {
        // Setup
        Vault vault = new Vault();
        vault.setId(1L);

        Note sourceNote = new Note();
        sourceNote.setId(1L);
        Note targetNote = new Note();
        targetNote.setId(2L);

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(vaultRepository.findById(1L)).willReturn(Optional.of(vault));
        given(noteRepository.findById(1L)).willReturn(Optional.of(sourceNote));
        given(noteRepository.findById(2L)).willReturn(Optional.of(targetNote));

        MockHttpServletRequestBuilder postRequest = post("/vaults/1/note_links")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"source\":\"1\", \"target\":\"2\"}");

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated());
    }
    @Test
    public void getNote_validInput_Ok() throws Exception {
        Note note = new Note();
        note.setId(1L);
        note.setTitle("MyNote");
        Vault vault = new Vault();
        vault.setOwner(new User());
        note.setVault(vault);

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(noteRepository.findById(1L)).willReturn(Optional.of(note));

        MockHttpServletRequestBuilder getRequest = get("/notes/1")
                .header("Authorization", "Bearer validToken");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("MyNote")));
    }
    @Test
    public void getNote_noteNotFound_NotFound() throws Exception {
        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(noteRepository.findById(1L)).willReturn(Optional.empty());

        MockHttpServletRequestBuilder getRequest = get("/notes/1")
                .header("Authorization", "Bearer validToken");

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }
    @Test
    public void renameNote_validInput_Ok() throws Exception {
        Note note = new Note();
        Vault vault = new Vault();
        User owner = new User();
        owner.setId(1L);
        vault.setOwner(owner);
        note.setVault(vault);
        note.setId(1L);

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(noteRepository.findById(1L)).willReturn(Optional.of(note));

        MockHttpServletRequestBuilder putRequest = put("/notes/1")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated Title\"}");

        mockMvc.perform(putRequest)
                .andExpect(status().isOk())
                .andExpect(content().string("Title updated"));
    }
    @Test
    public void renameNote_userIsNotOwner_Forbidden() throws Exception {
        Note note = new Note();
        Vault vault = new Vault();
        User owner = new User();
        owner.setId(2L); // Not same as token user
        vault.setOwner(owner);
        note.setVault(vault);
        note.setId(1L);

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(noteRepository.findById(1L)).willReturn(Optional.of(note));

        MockHttpServletRequestBuilder putRequest = put("/notes/1")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"New Title\"}");

        mockMvc.perform(putRequest)
                .andExpect(status().isForbidden());
    }
    @Test
    public void renameNote_emptyTitle_BadRequest() throws Exception {
        Note note = new Note();
        Vault vault = new Vault();
        User owner = new User();
        owner.setId(1L);
        vault.setOwner(owner);
        note.setVault(vault);
        note.setId(1L);

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(noteRepository.findById(1L)).willReturn(Optional.of(note));

        MockHttpServletRequestBuilder putRequest = put("/notes/1")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\"}");

        mockMvc.perform(putRequest)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Title cannot be empty"));
    }
    @Test
    public void getSharedNotes_validInput_Ok() throws Exception {
        Note note = new Note();
        note.setId(1L);
        note.setTitle("SharedNote");
        Vault vault = new Vault();
        vault.setId(1L);
        vault.setOwner(new User());
        note.setVault(vault);

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(noteService.getSharedNotesForUser(1L)).willReturn(List.of(note));

        MockHttpServletRequestBuilder getRequest = get("/notes/shared")
                .header("Authorization", "Bearer validToken");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("SharedNote")));
    }
    @Test
    public void getSharedNotes_invalidToken_Unauthorized() throws Exception {
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(false);

        MockHttpServletRequestBuilder getRequest = get("/notes/shared")
                .header("Authorization", "Bearer invalidToken");

        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
    }
    @Test
    public void createNote_emptyTitle_BadRequest() throws Exception {
        Vault vault = new Vault();
        User owner = new User();
        owner.setId(1L);
        vault.setOwner(owner);

        given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
        given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);
        given(vaultRepository.findById(1L)).willReturn(Optional.of(vault));

        MockHttpServletRequestBuilder postRequest = post("/vaults/1/notes")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\"}");

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Title cannot be empty"));
    }

}