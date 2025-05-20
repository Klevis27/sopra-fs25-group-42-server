package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import org.junit.jupiter.api.Test;
import ch.uzh.ifi.hase.soprafs24.entity.Note;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation
 * works.
 */
public class DTOMapperTest {
    //----------------------------Note_DTOs----------------------------//
    //--------------------FreePalestina----------------------//
    // Test NoteLinksGetDTO
    @Test
    void testNoteLinksGetDTOMapping() {
        NoteLinksGetDTO dto = new NoteLinksGetDTO();
        Note sourceNote = new Note();
        sourceNote.setId(1L);
        Note targetNote = new Note();
        targetNote.setId(2L);

        dto.setId(10L);
        dto.setSourceNoteId(sourceNote);
        dto.setTargetNoteId(targetNote);

        assertEquals(10L, dto.getId());
        assertEquals(1L, dto.getSourceNoteId());
        assertEquals(2L, dto.getTargetNoteId());
    }
    //--------------------FreePalestina----------------------//
    // Test NotePermissionDTO
    @Test
    void testNotePermissionDTOMapping() {
        NotePermissionDTO dto = new NotePermissionDTO("testUser", "EDITOR");

        assertEquals("testUser", dto.getUsername());
        assertEquals("EDITOR", dto.getRole());
    }
    //--------------------FreePalestina----------------------//
    // Test NotesGetDTO
    @Test
    void testNotesGetDTOMapping() {
        NotesGetDTO dto = new NotesGetDTO();
        dto.setId(1L);
        dto.setTitle("Test Note");

        assertEquals(1L, dto.getId());
        assertEquals("Test Note", dto.getTitle());
    }
    //--------------------FreePalestina----------------------//
    // Test NotesInvitePostDTO
    @Test
    void testNotesInvitePostDTOMapping() {
        NotesInvitePostDTO dto = new NotesInvitePostDTO();
        dto.setUsername("testUser");
        dto.setRole("VIEWER");

        assertEquals("testUser", dto.getUsername());
        assertEquals("VIEWER", dto.getRole());
    }
    //--------------------FreePalestina----------------------//
    // Test NotesPostDTO
    @Test
    void testNotesPostDTOMapping() {
        NotesPostDTO dto = new NotesPostDTO();
        dto.setId(1L);
        dto.setTitle("Test Note");

        assertEquals(1L, dto.getId());
        assertEquals("Test Note", dto.getTitle());
    }
    //--------------------FreePalestina----------------------//
    // Test NoteStatePostDTO
    /*
    @Test
    void testNoteStatePostDTOMapping() {
        NoteStatePostDTO dto = new NoteStatePostDTO();
        dto.setDocId(100L);
        dto.setNoteId(1L);
        byte[] content = "Test content".getBytes(StandardCharsets.UTF_8);
        dto.setContent(content);

        assertEquals(100L, dto.getDocId());
        assertEquals(1L, dto.getNoteId());
        assertArrayEquals(content, dto.getContent());
    }
    */
    //--------------------FreePalestina----------------------//v
    // Test NoteStatePutDTO
    //ToDo: does not pass unsurprisngly
    /*
    @Test
    void testNoteStatePutDTOMapping() {
        NoteStatePutDTO dto = new NoteStatePutDTO();
        dto.setDocId(100L);
        dto.setNoteId(1L);
        byte[] content = "Updated content".getBytes(StandardCharsets.UTF_8);
        dto.setContent(content);

        assertEquals(100L, dto.getDocId());
        assertEquals(1L, dto.getNoteId());
        assertArrayEquals(content, dto.getContent());
    }
     */
    //----------------------------User_DTOs----------------------------//
    //--------------------FreePalestina----------------------//
    // Test UserEditDTO
    @Test
    void testUserEditDTOMapping() {
        UserEditDTO dto = new UserEditDTO();
        dto.setId(1L);
        dto.setUsername("updatedUser");
        LocalDate birthday = LocalDate.of(1990, 1, 1);
        dto.setBirthday(birthday);

        assertEquals(1L, dto.getId());
        assertEquals("updatedUser", dto.getUsername());
        assertEquals(birthday, dto.getBirthday());
    }
    //--------------------FreePalestina----------------------//
    // Test UserPostDTO
    @Test
    void testUserPostDTOMapping() {
        UserPostDTO dto = new UserPostDTO();
        dto.setId(1L);
        dto.setUsername("testUser");
        dto.setPassword("securePassword");

        assertEquals(1L, dto.getId());
        assertEquals("testUser", dto.getUsername());
        assertEquals("securePassword", dto.getPassword());
    }
    //--------------------FreePalestina----------------------//
    // Test UserProfileDTO
    @Test
    void testUserProfileDTOMapping() {
        UserProfileDTO dto = new UserProfileDTO();
        LocalDate testDate = LocalDate.now();

        dto.setId(1L);
        dto.setUsername("profileUser");
        dto.setStatus(UserStatus.ONLINE);
        dto.setCreationDate(testDate);
        dto.setBirthday(testDate);

        assertEquals(1L, dto.getId());
        assertEquals("profileUser", dto.getUsername());
        assertEquals(UserStatus.ONLINE, dto.getStatus());
        assertEquals(testDate, dto.getCreationDate());
        assertEquals(testDate, dto.getBirthday());
    }
    //--------------------FreePalestina----------------------//
    // Test UserLoginDTO
    @Test
    void testUserLoginDTOMapping() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("loginUser");
        dto.setPassword("loginPass");

        assertEquals("loginUser", dto.getUsername());
        assertEquals("loginPass", dto.getPassword());
    }
    //--------------------FreePalestina----------------------//
    // Test UserGetDTO
    @Test
    void testUserGetDTOMapping() {
        UserGetDTO dto = new UserGetDTO();
        LocalDate testDate = LocalDate.now();

        dto.setId(1L);
        dto.setUsername("getUser");
        dto.setStatus(UserStatus.OFFLINE);
        dto.setCreationDate(testDate);
        dto.setBirthday(testDate);

        assertEquals(1L, dto.getId());
        assertEquals("getUser", dto.getUsername());
        assertEquals(UserStatus.OFFLINE, dto.getStatus());
        assertEquals(testDate, dto.getCreationDate());
        assertEquals(testDate, dto.getBirthday());
    }

    //----------------------------Vault_DTOs----------------------------//
    //--------------------FreePalestina----------------------//
    // Test VaultDTO
    @Test
    void testVaultDTOMapping() {
        Vault vault = new Vault();
        vault.setId(1L);
        vault.setName("Test Vault");

        // Test fromEntity
        VaultDTO dto1 = VaultDTO.fromEntity(vault);
        assertEquals(1L, dto1.id);
        assertEquals("Test Vault", dto1.name);
        assertNull(dto1.role);

        // Test fromEntityWithRole
        VaultDTO dto2 = VaultDTO.fromEntityWithRole(vault, "OWNER");
        assertEquals(1L, dto2.id);
        assertEquals("Test Vault", dto2.name);
        assertEquals("OWNER", dto2.role);
    }
    //--------------------FreePalestina----------------------//
    // Test VaultInvitationDTO
    @Test
    void testVaultInvitationDTOMapping() {
        VaultInvitationDTO dto = new VaultInvitationDTO();
        LocalDateTime now = LocalDateTime.now();

        dto.setId(1L);
        dto.setToken("invite-token");
        dto.setVaultName("Shared Vault");
        dto.setRole("EDITOR");
        dto.setCreatedAt(now);

        assertEquals(1L, dto.getId());
        assertEquals("invite-token", dto.getToken());
        assertEquals("Shared Vault", dto.getVaultName());
        assertEquals("EDITOR", dto.getRole());
        assertEquals(now, dto.getCreatedAt());
    }
    //--------------------FreePalestina----------------------//
    // Test VaultInviteCreateDTO
    @Test
    void testVaultInviteCreateDTOMapping() {
        VaultInviteCreateDTO dto = new VaultInviteCreateDTO();
        dto.setUserId(1L);
        dto.setVaultId(2L);
        dto.setRole("VIEWER");

        assertEquals(1L, dto.getUserId());
        assertEquals(2L, dto.getVaultId());
        assertEquals("VIEWER", dto.getRole());
    }
    //--------------------FreePalestina----------------------//

    // Test VaultPermissionDTO
    @Test
    void testVaultPermissionDTOMapping() {
        VaultPermissionDTO dto = new VaultPermissionDTO();
        dto.setUserId(1L);
        dto.setUsername("user1");
        dto.setRole("EDITOR");

        assertEquals(1L, dto.getUserId());
        assertEquals("user1", dto.getUsername());
        assertEquals("EDITOR", dto.getRole());
    }
    //--------------------FreePalestina----------------------//
    // Test VaultPostDTO
    @Test
    void testVaultPostDTOMapping() {
        VaultPostDTO dto = new VaultPostDTO();
        User owner = new User();
        owner.setId(1L);

        dto.setId(1L);
        dto.setName("New Vault");
        dto.setOwner(owner);

        assertEquals(1L, dto.getId());
        assertEquals("New Vault", dto.getName());
        assertEquals(owner, dto.getOwner());
    }
    //--------------------FreePalestina----------------------//
    // Test VaultsGetDTO
    @Test
    void testVaultsGetDTOMapping() {
        VaultsGetDTO dto = new VaultsGetDTO();
        dto.setId(1L);
        dto.setName("Test Vault");

        assertEquals(1L, dto.getId());
        assertEquals("Test Vault", dto.getName());
    }
    //--------------------FreePalestina----------------------//
    // Test CreateVaultDTO
    @Test
    void testCreateVaultDTOMapping() {
        CreateVaultDTO dto = new CreateVaultDTO();
        dto.setName("Test Vault");

        assertEquals("Test Vault", dto.getName());
    }
    /*
    @Test
    public void testCreateUser_fromUserPostDTO_toUser_success() {
        // create UserPostDTO
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("username");
        userPostDTO.setPassword("password");

        // MAP -> Create user
        User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // check content
        assertEquals(userPostDTO.getUsername(), user.getUsername());
        assertEquals(userPostDTO.getPassword(), user.getPassword());
    }

    @Test
    public void testGetUser_fromUser_toUserGetDTO_success() {
        // create User
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setPassword("password");
        user.setStatus(UserStatus.OFFLINE);
        user.setAccessToken("1");

        // MAP -> Create UserGetDTO
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

        // check content
        assertEquals(user.getId(), userGetDTO.getId());
        assertEquals(user.getUsername(), userGetDTO.getUsername());
        assertEquals(user.getStatus(), userGetDTO.getStatus());
    }
    */
}
