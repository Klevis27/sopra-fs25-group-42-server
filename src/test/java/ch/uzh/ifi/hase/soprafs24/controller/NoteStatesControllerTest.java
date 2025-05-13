// package ch.uzh.ifi.hase.soprafs24.controller;

// import ch.uzh.ifi.hase.soprafs24.rest.dto.NoteStatePostDTO;
// import ch.uzh.ifi.hase.soprafs24.rest.dto.NoteStatePutDTO;
// import ch.uzh.ifi.hase.soprafs24.service.NoteStateService;
// import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mockito;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// import static org.hamcrest.Matchers.*;

// @WebMvcTest(NoteStatesController.class)
// public class NoteStatesControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private NoteStateService noteStateService;

//     @MockBean
//     private JwtUtil jwtUtil; // Needed if your endpoints require authentication

//     private String asJsonString(final Object object) {
//         try {
//             return new ObjectMapper().writeValueAsString(object);
//         } catch (JsonProcessingException e) {
//             throw new RuntimeException(e);
//         }
//     }

//     // ===== UPDATE NOTE STATE TESTS =====
//     @Test
//     public void updateNoteStateContent_validInput_success() throws Exception {
//         // Setup
//         Long noteId = 1L;
//         NoteStatePutDTO noteStatePutDTO = new NoteStatePutDTO();
//         byte[] content = new byte[1];
//         noteStatePutDTO.setNoteId(noteId);
//         noteStatePutDTO.setContent(content);

//         Mockito.when(noteStateService.updateNoteStateContent(Mockito.any())).thenReturn(true);
//         Mockito.when(jwtUtil.validateToken(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

//         // Execute & Verify
//         mockMvc.perform(put("/noteState/{note_Id}", noteId)
//                 .header("Authorization", "Bearer validToken")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(asJsonString(noteStatePutDTO)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$", is("Note state content updated successfully")));
//     }

//     @Test
//     public void updateNoteStateContent_idMismatch_badRequest() throws Exception {
//         // Setup
//         Long noteId = 1L;
//         NoteStatePutDTO noteStatePutDTO = new NoteStatePutDTO();
//         noteStatePutDTO.setNoteId(2L); // Different ID

//         // Execute & Verify
//         mockMvc.perform(put("/noteState/{note_Id}", noteId)
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(asJsonString(noteStatePutDTO)))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$", is("Note ID in URL does not match the one in the body")));
//     }

//     @Test
//     public void updateNoteStateContent_noteNotFound_notFound() throws Exception {
//         // Setup
//         Long noteId = 1L;
//         NoteStatePutDTO noteStatePutDTO = new NoteStatePutDTO();
//         noteStatePutDTO.setNoteId(noteId);

//         Mockito.when(noteStateService.updateNoteStateContent(Mockito.any())).thenReturn(false);

//         // Execute & Verify
//         mockMvc.perform(put("/noteState/{note_Id}", noteId)
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(asJsonString(noteStatePutDTO)))
//                 .andExpect(status().isNotFound())
//                 .andExpect(jsonPath("$", is("Note state not found")));
//     }

//     // ===== CREATE NOTE STATE TESTS =====
//     @Test
//     public void createNoteState_validInput_success() throws Exception {
//         // Setup
//         Long noteId = 1L;
//         NoteStatePostDTO noteStatePostDTO = new NoteStatePostDTO();
//         byte[] content = new byte[1];
//         noteStatePostDTO.setNoteId(noteId);
//         noteStatePostDTO.setContent(content);

//         Mockito.when(noteStateService.createNoteState(Mockito.any())).thenReturn(true);
//         Mockito.when(jwtUtil.validateToken(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

//         // Execute & Verify
//         mockMvc.perform(post("/noteState/{note_Id}", noteId)
//                 .header("Authorization", "Bearer validToken")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(asJsonString(noteStatePostDTO)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$", is("Note state created successfully")));
//     }

//     @Test
//     public void createNoteState_idMismatch_badRequest() throws Exception {
//         // Setup
//         Long noteId = 1L;
//         NoteStatePostDTO noteStatePostDTO = new NoteStatePostDTO();
//         noteStatePostDTO.setNoteId(2L); // Different ID

//         // Execute & Verify
//         mockMvc.perform(post("/noteState/{note_Id}", noteId)
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(asJsonString(noteStatePostDTO)))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$", is("Note ID in URL does not match the one in the body")));
//     }

//     @Test
//     public void createNoteState_creationFailed_notFound() throws Exception {
//         // Setup
//         Long noteId = 1L;
//         NoteStatePostDTO noteStatePostDTO = new NoteStatePostDTO();
//         noteStatePostDTO.setNoteId(noteId);

//         //Mockito.when(noteStateService.createNoteState(Mockito.any())).thenReturn(false);

//         // Execute & Verify
//         mockMvc.perform(post("/noteState/{note_Id}", noteId)
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(asJsonString(noteStatePostDTO)))
//                 .andExpect(status().isNotFound())
//                 .andExpect(jsonPath("$", is("Note state could not be created")));
//     }

//     // ===== SECURITY TESTS =====
//     @Test
//     public void updateNoteStateContent_unauthorized_returnsUnauthorized() throws Exception {
//         // Setup
//         Long noteId = 1L;
//         NoteStatePutDTO noteStatePutDTO = new NoteStatePutDTO();
//         noteStatePutDTO.setNoteId(noteId);

//         Mockito.when(jwtUtil.validateToken(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

//         // Execute & Verify
//         mockMvc.perform(put("/noteState/{note_Id}", noteId)
//                 .header("Authorization", "Bearer invalidToken")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(asJsonString(noteStatePutDTO)))
//                 .andExpect(status().isUnauthorized());
//     }
// }