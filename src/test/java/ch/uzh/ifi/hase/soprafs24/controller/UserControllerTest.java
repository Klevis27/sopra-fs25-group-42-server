package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.security.SecurityConfig;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private UserService userService;

        @MockBean
        private JwtUtil jwtUtil;

        @MockBean
        private UserRepository userRepository;

        @MockBean
        private BCryptPasswordEncoder encoder;

        // Helper method to convert objects to JSON
        private String asJsonString(final Object object) {
                try {
                        return new ObjectMapper().writeValueAsString(object);
                } catch (JsonProcessingException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        String.format("The request body could not be created.%s", e.toString()));
                }
        }

        // Test if POST "/users" correctly creates a new user when valid input is
        // provided and gives back 201 CREATED
        @Test
        public void registerUser_validInput_userCreated() throws Exception {
                // given
                UserPostDTO userPostDTO = new UserPostDTO();
                userPostDTO.setUsername("testUsername");
                userPostDTO.setPassword("testPassword");

                User newUser = new User();
                newUser.setId(1L);
                newUser.setUsername("testUsername");
                newUser.setPassword("testPasswordEncrypted");
                newUser.setAccessToken("FILLER_ACCESS_TOKEN");

                given(userService.createUser(Mockito.any())).willReturn(newUser);

                // when/then -> do the request + validate the result
                MockHttpServletRequestBuilder postRequest = post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(userPostDTO));

                mockMvc.perform(postRequest)
                                .andDo(print()) // Print response for debugging
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.message", is("Registration successful")))
                                .andExpect(jsonPath("$.id", is(newUser.getId().toString())))
                                .andExpect(jsonPath("$.accessToken", is(newUser.getAccessToken())))
                                .andExpect(jsonPath("$.user.username", is(newUser.getUsername())));
        }

        // Test how POST "/users" handles a registration attempt with an existing
        // username and if it returns 409 CONFLICT
        @Test
        public void registerUser_existingUsername_conflict() throws Exception {
                // given
                UserPostDTO userPostDTO = new UserPostDTO();
                userPostDTO.setUsername("existingUsername");
                userPostDTO.setPassword("testPassword");

                given(userService.createUser(Mockito.any())).willReturn(null);

                // when/then -> do the request + validate the result
                MockHttpServletRequestBuilder postRequest = post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(userPostDTO));

                mockMvc.perform(postRequest)
                                .andDo(print()) // Print response for debugging
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.Error",
                                                is("Registration failed because username was already taken")));
        }

        // Test how GET "/users/{id}" handles a request with valid inputs and
        // authorization and if it returns 200 OK
        @Test
        public void getUserProfile_validInputAndToken_success() throws Exception {
                // given
                User user = new User();
                user.setId(1L);
                user.setUsername("testUsername");
                user.setPassword("testPassword");

                given(userRepository.findById(1L)).willReturn(Optional.of(user));
                given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
                given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);

                // when/then -> do the request + validate the result
                MockHttpServletRequestBuilder getRequest = get("/users/1")
                                .header("Authorization", "Bearer validToken")
                                .contentType(MediaType.APPLICATION_JSON);

                mockMvc.perform(getRequest)
                                .andDo(print()) // Print response for debugging
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.username", is(user.getUsername())));
        }

        // Test how GET "/users/{id}" handles a request for a user profile that does not
        // exist and if it gives back 404 NOT FOUND
        @Test
        public void getUserProfile_userNotFound_notFound() throws Exception {
                // given
                given(userRepository.findById(Mockito.eq(999L))).willReturn(Optional.empty());
                given(jwtUtil.extractId(Mockito.anyString())).willReturn("999");
                given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("999"))).willReturn(true);

                // when/then -> do the request + validate the result
                MockHttpServletRequestBuilder getRequest = get("/users/999")
                                .header("Authorization", "Bearer validToken")
                                .contentType(MediaType.APPLICATION_JSON);

                mockMvc.perform(getRequest)
                                .andDo(print()) // Print response for debugging
                                .andExpect(status().isNotFound());
        }

        // Test how PUT "/users/{id}" handles a request with valid inputs and if it
        // gives back 204 NO CONTENT
        @Test
        public void editUser_validInput_noContent() throws Exception {
                // given
                UserEditDTO userEditDTO = new UserEditDTO();
                userEditDTO.setUsername("updatedUsername");

                User updatedUser = new User();
                updatedUser.setId(1L);
                updatedUser.setUsername("updatedUsername");
                updatedUser.setPassword("updatedPassword");

                given(userService.editUser(Mockito.any())).willReturn(updatedUser);
                given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
                given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);

                // when/then -> do the request + validate the result
                MockHttpServletRequestBuilder putRequest = put("/users/1")
                                .header("Authorization", "Bearer validToken")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(userEditDTO));

                mockMvc.perform(putRequest)
                                .andDo(print()) // Print response for debugging
                                .andExpect(status().isNoContent());
        }

        // Test how PUT "/users/{id}" handles a request for a user profile that does not
        // exist and if it gives back 404 NOT FOUND
        @Test
        public void editUser_userNotFound_notFound() throws Exception {
                // given
                UserEditDTO userEditDTO = new UserEditDTO();
                userEditDTO.setUsername("updatedUsername");

                given(userService.editUser(Mockito.any())).willReturn(null);
                given(jwtUtil.extractId(Mockito.anyString())).willReturn("999");
                given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("999"))).willReturn(true);

                // when/then -> do the request + validate the result
                MockHttpServletRequestBuilder putRequest = put("/users/999")
                                .header("Authorization", "Bearer validToken")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(userEditDTO));

                mockMvc.perform(putRequest)
                                .andDo(print()) // Print response for debugging
                                .andExpect(status().isNotFound());
        }

        // Test how POST "/login" handles a request with valid inputs and if it gives
        // back 200 OK
        @Test
        public void loginUser_validInput_Ok() throws Exception {

                UserLoginDTO userLoginDTO = new UserLoginDTO();
                userLoginDTO.setUsername("testUsername");
                userLoginDTO.setPassword("testPassword");

                User existingUser = new User();
                existingUser.setId(1L);
                existingUser.setUsername("testUsername");
                existingUser.setPassword("testEncryptedPassword");
                existingUser.setAccessToken("FILLER_ACCESS_TOKEN");

                given(userService.login(Mockito.any())).willReturn(existingUser);
                given(jwtUtil.generateAccessToken(Mockito.any())).willReturn(existingUser.getAccessToken());

                // when/then -> do the request + validate the result
                MockHttpServletRequestBuilder postRequest = post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(userLoginDTO));

                mockMvc.perform(postRequest)
                                .andDo(print()) // Print response for debugging
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message", is("Login successful")))
                                .andExpect(jsonPath("$.accessToken", is(existingUser.getAccessToken())))
                                .andExpect(jsonPath("$.id", is(existingUser.getId().toString())));
        }

        // Test how POST "/login" handles a request with invalid inputs and if it gives
        // back 401 UNAUTHORIZED
        @Test
        public void loginUser_invalidInput_Unauthorized() throws Exception {

                UserLoginDTO userLoginDTO = new UserLoginDTO();
                userLoginDTO.setUsername("testUsername");
                userLoginDTO.setPassword("testPassword");

                given(userService.login(Mockito.any())).willReturn(null);

                MockHttpServletRequestBuilder postRequest = post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(userLoginDTO));

                mockMvc.perform(postRequest)
                                .andDo(print())
                                .andExpect(status().isUnauthorized());
        }

        // Test how GET "/users" handles a request with valid input and if it gives back
        // 200 OK
        @Test
        public void getUserDashboard_validInput_Ok() throws Exception {

                User user = new User();
                user.setId(1L);

                given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
                given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);

                MockHttpServletRequestBuilder getRequest = get("/users")
                                .header("Authorization", "Bearer validToken")
                                .contentType(MediaType.APPLICATION_JSON);

                mockMvc.perform(getRequest)
                                .andExpect((status().isOk()));
        }

        // Test how GET "/users" handles a request with valid input but invalid token
        // and gives back 401 UNAUTHORIZED
        @Test
        public void getUserDashboard_invalidInput_Unauthorized() throws Exception {

                User user = new User();
                user.setId(1L);

                given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
                given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(false);

                MockHttpServletRequestBuilder getRequest = get("/users")
                                .header("Authorization", "Bearer validToken")
                                .contentType(MediaType.APPLICATION_JSON);

                mockMvc.perform(getRequest)
                                .andExpect((status().isUnauthorized()));
        }

        // Test how POST "/logout" handles a request with valid input
        // and if it gives back 200 OK
        @Test
        public void logoutUser_validInput_Ok() throws Exception {

                UserLogoutDTO userLogoutDTO = new UserLogoutDTO();
                userLogoutDTO.setId(1L);

                given(jwtUtil.extractId(Mockito.anyString())).willReturn("1");
                given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);

                MockHttpServletRequestBuilder postRequest = post("/logout")
                                .header("Authorization", "Bearer validToken")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(userLogoutDTO));

                mockMvc.perform(postRequest)
                                .andExpect((status().isOk()))
                                .andExpect(jsonPath("$.message", is("Logout successful")));
        }


        // Test how POST "/logout" handles a request with invalid input
        // and if it gives back 401 UNAUTHORIZED
        @Test
        public void logoutUser_invalidInput_Unauthorized() throws Exception {

                UserLogoutDTO userLogoutDTO = new UserLogoutDTO();
                userLogoutDTO.setId(1L);

                given(jwtUtil.extractId(Mockito.anyString())).willReturn(null);
                given(jwtUtil.validateToken(Mockito.anyString(), Mockito.eq("1"))).willReturn(true);

                MockHttpServletRequestBuilder postRequest = post("/logout")
                                .header("Authorization", "Bearer invalidToken")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(userLogoutDTO));

                mockMvc.perform(postRequest)
                                .andExpect((status().isUnauthorized()));
        }
}
