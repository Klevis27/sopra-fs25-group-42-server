package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NotePermissionDTO;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NoteServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
/*    private VaultRepository vaultRepository;
    @Mock*/
    private NotePermissionRepository notePermissionRepository;
    @Mock
    private NoteRepository noteRepository;
  /*  @Mock
    private JwtUtil jwtUtil;*/

    @InjectMocks
    private NoteService noteService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }
    //----------------------------------------------------------------------//
    // Tests for the status of an new note created by a user.
    // and that the "creator" is "Owner"
    @Test
    void testCreateNoteWithOwner_success() {
        Vault vault = new Vault();
        Note savedNote = new Note();
        savedNote.setId(1L);
        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        Note note = noteService.createNoteWithOwner("Test Note", vault, 100L);

        assertNotNull(note);
        verify(noteRepository).save(any(Note.class));
        verify(notePermissionRepository).save(any(NotePermission.class));
    }
    //----------------------------------------------------------------------//
    // test of role of invited user if recived invitation
    @Test
    void testInviteUserToNote_success() {
        User user = new User();
        user.setId(200L);
        when(userRepository.findByUsername("john")).thenReturn(user);
        when(notePermissionRepository.existsByUserIdAndNoteId(200L, 1L)).thenReturn(false);

        noteService.inviteUserToNote(1L, "john", "editor");

        verify(notePermissionRepository).save(argThat(p ->
                p.getNoteId().equals(1L) &&
                        p.getUserId().equals(200L) &&
                        p.getRole().equals("editor")
        ));
    }

    //----------------------------------------------------------------------//
    // test of sending invitation to non-existend user and thow error
    @Test
    void testInviteUserToNote_userNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                noteService.inviteUserToNote(1L, "missing", "reader")
        );

        assertEquals("404 NOT_FOUND \"User not found\"", exception.getMessage());
    }

    //----------------------------------------------------------------------//
    // test of sending invitation to user that already has permission to note
    @Test
    void testInviteUserToNote_userAlreadyHasPermission() {
        User user = new User();
        user.setId(300L);
        when(userRepository.findByUsername("alice")).thenReturn(user);
        when(notePermissionRepository.existsByUserIdAndNoteId(300L, 1L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                noteService.inviteUserToNote(1L, "alice", "reader")
        );

        assertEquals("409 CONFLICT \"User already has permission to this note\"", exception.getMessage());
    }
    //----------------------------------------------------------------------//
    // test of getting all notes with permision
    @Test
    void testGetNotePermissions_returnsList() {
        NotePermission perm = new NotePermission();
        perm.setUserId(10L);
        perm.setRole("reader");

        User user = new User();
        user.setId(10L);
        user.setUsername("bob");

        when(notePermissionRepository.findByNoteId(5L)).thenReturn(List.of(perm));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        List<NotePermissionDTO> result = noteService.getNotePermissions(5L);

        assertEquals(1, result.size());
        assertEquals("bob", result.get(0).getUsername());
        assertEquals("reader", result.get(0).getRole());
    }

    //----------------------------------------------------------------------//
    // Ensures that shared notes for a user (via permissions) are returned properly.
    @Test
    void testGetSharedNotesForUser_returnsNotes() {
        Note note = new Note();
        note.setId(1L);

        when(notePermissionRepository.findSharedNotesByUserId(42L)).thenReturn(List.of(note));

        List<Note> result = noteService.getSharedNotesForUser(42L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
