package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    /*
    @Test
    public void createUser_validInputs_success() {
        // given
        assertNull(userRepository.findByUsername("testUsername"));

        UserPostDTO testUser = new UserPostDTO();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");

        // when
        User createdUser = userService.createUser(testUser);

        // then
        assertEquals(testUser.getUsername(), createdUser.getUsername());
        assertNotNull(createdUser.getAccessToken());
        assertEquals(UserStatus.ONLINE, createdUser.getStatus());
    }

    @Test
    public void createUser_duplicateUsername_throwsException() {
        assertNull(userRepository.findByUsername("testUsername"));

        UserPostDTO testUser = new UserPostDTO();
        testUser.setUsername("testUsername");
        testUser.setPassword("password");
        //User createdUser = userService.createUser(testUser);

        // attempt to create second user with same username
        UserPostDTO testUser2 = new UserPostDTO();

        // change the name but forget about the username
        testUser2.setUsername("testUsername");
        testUser2.setPassword("password2");

        // check that an error is thrown
        assertNull(userService.createUser(testUser2));
    }
    */
}
