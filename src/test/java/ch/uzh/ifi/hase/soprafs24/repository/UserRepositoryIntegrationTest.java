package ch.uzh.ifi.hase.soprafs24.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
public class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    /*
    @Test
    public void findByUsername_success() {
        // given
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setPassword("password");
        user.setCreationDate(LocalDate.parse("2025-03-06"));
        user.setStatus(UserStatus.ONLINE); // Set status explicitly
        user.setAccessToken("1");

        // Persist the user using the TestEntityManager
        entityManager.persist(user);
        entityManager.flush();

        // when
        User found = userRepository.findByUsername(user.getUsername());

        // then
        assertNotNull(found.getId());
        assertEquals(found.getUsername(), user.getUsername());
        assertEquals(found.getPassword(), user.getPassword());
        assertEquals(found.getAccessToken(), user.getAccessToken());
        assertEquals(found.getStatus(), user.getStatus());
    }
    */
}