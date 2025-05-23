package ch.uzh.ifi.hase.soprafs24.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private String baseURL = "http://localhost:";

    public static RestTemplate restTemplate;

    @BeforeAll
    public static void init() {
        restTemplate = new RestTemplate();
    }

    @BeforeEach
    public void setUp() {
        baseURL = baseURL.concat(port + "");
    }

    @Test
    public void registerUser_success() {
        baseURL = baseURL.concat("/users");

        // Erstellen Sie ein vollständigeres Benutzer-Objekt
        Map<String, Object> user = new HashMap<>();
        user.put("username", "testUser" + System.currentTimeMillis());
        user.put("password", "password123");
        user.put("creationDate", LocalDate.now().toString());
        user.put("status", "ONLINE");  // Fügen Sie den Status hinzu
        user.put("name", "Test User"); // Fügen Sie den Namen hinzu

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseURL,
                    user,
                    Map.class);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().get("id"));
            assertEquals(user.get("username"), response.getBody().get("username"));
        } catch (HttpServerErrorException e) {
            fail("Server-Fehler aufgetreten: " + e.getResponseBodyAsString());
        }
    }
}
