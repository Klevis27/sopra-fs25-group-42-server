package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public UserController(UserService userService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    // REGISTRATION
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserPostDTO userPostDTO) {
        User newUser = userService.createUser(userPostDTO);

        // Registration successful?
        if (newUser == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("Error", "Registration failed because username was already taken"));
        }

        // Map correctly
        UserProfileDTO user = DTOMapper.INSTANCE.convertEntityToUserProfileDTO(newUser);

        // Return success!
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Registration successful", "id", newUser.getId().toString(), "accessToken", newUser.getAccessToken(), "user", user));
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserLoginDTO userLoginDTO) {
        User user = userService.login(userLoginDTO);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        // Generate JWT token after successful login
        String id = user.getId().toString();
        String accessToken = jwtUtil.generateAccessToken(user.getId());

        // Return token to client
        return ResponseEntity.ok(Map.of("message", "Login successful", "accessToken", accessToken, "id", id));
    }

    // DASHBOARD
    @GetMapping("/users")
    public ResponseEntity<List<UserGetDTO>> dashboard(HttpServletRequest request) {
        // Extract token from the Authorization header
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Fetch all users for the dashboard
        List<User> users = userService.getUsers();
        List<UserGetDTO> userGetDTOs = new ArrayList<>();

        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }

        return ResponseEntity.ok(userGetDTOs);
    }

    //PROFILE
    @GetMapping("/users/{id}")
    public ResponseEntity<UserProfileDTO> profile(@PathVariable("id") Long id, HttpServletRequest request) {
        // Extract token from the Authorization header
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Fetch user with said ID, map and return
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // If user not found
        }
        UserProfileDTO resultUser = DTOMapper.INSTANCE.convertEntityToUserProfileDTO(user);
        return ResponseEntity.ok(resultUser);
    }

    //EDIT
    @PutMapping("/users/{id}")
    public ResponseEntity<Void> edit(@PathVariable("id") Long id, @RequestBody UserEditDTO userEditDTO, HttpServletRequest request) {
        // Extract token from the Authorization header
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Check if it's the right user who's trying to edit through the passed slug
        if (!Objects.equals(jwtUtil.extractId(token), id.toString())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Edit user
        User user = userService.editUser(userEditDTO);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Return 204
        return ResponseEntity.noContent().build();
    }

    // LOGOUT
    //Shouldn't this be a PUT method since we are not creating a new 
    //entity but rather changing an existing one?
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody UserLogoutDTO userLogoutDTO, HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        userService.logout(userLogoutDTO);
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    // Helper method to extract token from the Authorization header
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }
}
