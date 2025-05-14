package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;


    @Mock
    private BCryptPasswordEncoder encoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserPostDTO testUserPostDTO;
    private UserLoginDTO testUserLoginDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        testUser.setStatus(UserStatus.OFFLINE);
        testUser.setCreationDate(LocalDate.now());

        // Setup DTOs
        testUserPostDTO = new UserPostDTO();
        testUserPostDTO.setUsername("testUsername");
        testUserPostDTO.setPassword("testPassword");

        testUserLoginDTO = new UserLoginDTO();
        testUserLoginDTO.setUsername("testUsername");
        testUserLoginDTO.setPassword("testPassword");
    }
    //-------------------------------------------------------------//
    //test if created user exists, has correct data
    // is set ONLINE, Token exists (does not pass)
    @Test
    void createUser_validInputs_success() {
        // Given
        when(userRepository.findByUsername(any())).thenReturn(null);
        when(userRepository.save(any())).thenReturn(testUser);
        when(jwtUtil.generateAccessToken(any())).thenReturn("testToken");
        when(encoder.encode(any())).thenReturn("encodedPassword");

        // When
        User createdUser = userService.createUser(testUserPostDTO);

        // Then
        assertNotNull(createdUser);
        assertEquals(testUser.getId(), createdUser.getId());
        assertEquals(testUser.getUsername(), createdUser.getUsername());
        assertEquals(UserStatus.ONLINE, createdUser.getStatus());
        assertNotNull(createdUser.getAccessToken());

        verify(userRepository, times(2)).save(any());
        verify(userRepository, times(1)).findByUsername(any());
    }
    //-------------------------------------------------------------//
    // test if error is thown if username already exists
    // and it doesnt save
    @Test
    void createUser_duplicateUsername_returnsNull() {
        // Given
        when(userRepository.findByUsername(any())).thenReturn(testUser);

        // When
        User createdUser = userService.createUser(testUserPostDTO);

        // Then
        assertNull(createdUser);
        verify(userRepository, never()).save(any());
    }
    //-------------------------------------------------------------//
    // test valid credentials login (does not pass)
    @Test
    void login_validCredentials_success() {
        // Given
        when(userRepository.findByUsername(any())).thenReturn(testUser);
        when(encoder.matches(any(), any())).thenReturn(true);
        when(jwtUtil.generateAccessToken(any(Long.class))).thenReturn("testToken");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User loggedInUser = userService.login(testUserLoginDTO);

        // Then
        assertNotNull(loggedInUser);
        assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
        assertNotNull(loggedInUser.getAccessToken());
        verify(userRepository, times(1)).save(any());
    }

    //-------------------------------------------------------------//
    // test invalide username login
    @Test
    void login_invalidUsername_returnsNull() {
        // Given
        when(userRepository.findByUsername(any())).thenReturn(null);

        // When
        User loggedInUser = userService.login(testUserLoginDTO);

        // Then
        assertNull(loggedInUser);
        verify(userRepository, never()).save(any());
    }
    //-------------------------------------------------------------//
    // test invalide password login
    @Test
    void login_invalidPassword_returnsNull() {
        // Given
        when(userRepository.findByUsername(any())).thenReturn(testUser);
        when(encoder.matches(any(), any())).thenReturn(false);

        // When
        User loggedInUser = userService.login(testUserLoginDTO);

        // Then
        assertNull(loggedInUser);
        verify(userRepository, never()).save(any());
    }
    //-------------------------------------------------------------//
    // test if edit of user works
    @Test
    void editUser_validInputs_success() {
        // Given
        UserEditDTO editDTO = new UserEditDTO();
        editDTO.setId(1L);
        editDTO.setUsername("newUsername");
        editDTO.setBirthday(LocalDate.of(1990, 1, 1));

        when(userRepository.findById(any())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        // When
        User editedUser = userService.editUser(editDTO);

        // Then
        assertNotNull(editedUser);
        assertEquals("newUsername", editedUser.getUsername());
        assertEquals(LocalDate.of(1990, 1, 1), editedUser.getBirthday());
        verify(userRepository, times(1)).save(any());
    }
    //-------------------------------------------------------------//
    // test if edit of non-user
    @Test
    void editUser_userNotFound_returnsNull() {
        // Given
        UserEditDTO editDTO = new UserEditDTO();
        editDTO.setId(99L);

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        // When
        User editedUser = userService.editUser(editDTO);

        // Then
        assertNull(editedUser);
        verify(userRepository, never()).save(any());
    }
    //-------------------------------------------------------------//
    // test if get of user works for fetching ID
    @Test
    void getUser_validId_success() {
        // Given
        when(userRepository.findById(any())).thenReturn(Optional.of(testUser));

        // When
        User foundUser = userService.getUser(1L);

        // Then
        assertNotNull(foundUser);
        assertEquals(testUser.getId(), foundUser.getId());
        verify(userRepository, times(1)).findById(any());
    }
    //-------------------------------------------------------------//
    // test if get of user works for fetching by invalid ID
    @Test
    void getUser_invalidId_returnsNull() {
        // Given
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        // When
        User foundUser = userService.getUser(99L);

        // Then
        assertNull(foundUser);
    }
    //-------------------------------------------------------------//
    //test logout works
    @Test
    void logout_success() {
        // Given
        UserLogoutDTO logoutDTO = new UserLogoutDTO();
        logoutDTO.setId(1L);

        testUser.setStatus(UserStatus.ONLINE);
        testUser.setAccessToken("testToken");

        when(userRepository.findUserById(any())).thenReturn(testUser);
        when(userRepository.save(any())).thenReturn(testUser);

        // When
        userService.logout(logoutDTO);

        // Then
        assertEquals(UserStatus.OFFLINE, testUser.getStatus());
        assertNull(testUser.getAccessToken());
        verify(userRepository, times(1)).save(any());
    }
  /*
  @Test
  public void createUser_validInputs_success() {
    // when -> any object is being saved in the userRepository -> return the dummy
    // testUser
    User createdUser = userService.createUser(testUser);

    // then
    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

    assertEquals(testUser.getId(), createdUser.getId());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertEquals(testUser.getPassword(), createdUser.getPassword());
    assertNotNull(createdUser.getAccessToken());
    assertEquals(UserStatus.OFFLINE, createdUser.getStatus());
  }
  */

    /*
  @Test
  public void createUser_duplicateName_throwsException() {
    // given -> a first user has already been created
    userService.createUser(testUser);

    // when -> setup additional mocks for UserRepository
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

    // then -> attempt to create second user with same user -> check that an error
    // is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
  }
     */

    /*
    @Test
    public void createUser_duplicateInputs_throwsException() {
        // given -> a first user has already been created
        userService.createUser(testUser);

        // when -> setup additional mocks for UserRepository
        // Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

        // then -> attempt to create second user with same user -> check that an error
        // is thrown
        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }

     */

}
