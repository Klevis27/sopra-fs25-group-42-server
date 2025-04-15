package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.VaultRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;


import java.time.LocalDate;
import java.util.List;

@Service
public class UserService {

    // private static final Logger log = LoggerFactory.getLogger(UserService.class); // Maybe useful later
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder;

    @Autowired
    public UserService(UserRepository userRepository, JwtUtil jwtUtil, VaultRepository vaultRepository, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.encoder = encoder;
    }

    public User createUser(UserPostDTO userPostDTO) {

        // Check if the username already exists in the database
        if (userRepository.findByUsername(userPostDTO.getUsername()) != null) {
            return null; // Username already exists
        }

        // Set params
        User newUser = new User();
        newUser.setUsername(userPostDTO.getUsername());
        newUser.setPassword(new BCryptPasswordEncoder().encode(userPostDTO.getPassword()));
        newUser.setCreationDate(LocalDate.now());

        // Save to database
        User savedUser = userRepository.save(newUser);

        // Find user, use id to create access token and save
        User user = userRepository.findByUsername(savedUser.getUsername());
        user.setAccessToken(jwtUtil.generateAccessToken(user.getId()));
        return userRepository.save(user);
    }

    public User login(UserLoginDTO userLoginDTO) {
        User user = userRepository.findByUsername(userLoginDTO.getUsername());

        // Does user with username exist?
        if (user == null) {
            return null;
        }

        // Is password correct?
        if (userLoginDTO.getPassword() == null) {
            return null;
        }


        if (encoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            // Set and store data
            user.setAccessToken(jwtUtil.generateAccessToken(user.getId()));
            return userRepository.save(user);
        }
        return null;
    }

    public User editUser(UserEditDTO userEditDTO) {
        User user = getUser(userEditDTO.getId());
        if (user == null) {
            return null; // user not found
        }
        if (userEditDTO.getUsername() != null) {
            user.setUsername(userEditDTO.getUsername());
        }
        return userRepository.save(user);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public void logout(UserLogoutDTO userLogoutDTO) {
        User user = userRepository.findUserById(userLogoutDTO.getId());
        user.setAccessToken(null);
        userRepository.save(user);
    }
}
