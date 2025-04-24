package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.VaultRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserService {

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
        if (userRepository.findByUsername(userPostDTO.getUsername()) != null) {
            return null; // Username already exists
        }

        User newUser = new User();
        newUser.setUsername(userPostDTO.getUsername());
        newUser.setPassword(new BCryptPasswordEncoder().encode(userPostDTO.getPassword()));
        newUser.setCreationDate(LocalDate.now());
        newUser.setStatus(UserStatus.OFFLINE); // default offline

        User savedUser = userRepository.save(newUser);

        User user = userRepository.findByUsername(savedUser.getUsername());
        user.setAccessToken(jwtUtil.generateAccessToken(user.getId()));
        user.setStatus(UserStatus.ONLINE); // after register online
        return userRepository.save(user);
    }

    public User login(UserLoginDTO userLoginDTO) {
        User user = userRepository.findByUsername(userLoginDTO.getUsername());

        if (user == null || userLoginDTO.getPassword() == null) {
            return null;
        }

        if (encoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            user.setAccessToken(jwtUtil.generateAccessToken(user.getId()));
            user.setStatus(UserStatus.ONLINE); // after login online
            return userRepository.save(user);
        }
        return null;
    }

    public User editUser(UserEditDTO userEditDTO) {
        User user = getUser(userEditDTO.getId());
        if (user == null) {
            return null;
        }
        if (userEditDTO.getUsername() != null) {
            user.setUsername(userEditDTO.getUsername());
        }
        if (userEditDTO.getBirthday() != null) {
            user.setBirthday(userEditDTO.getBirthday());
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
        user.setStatus(UserStatus.OFFLINE); // after logout offline
        userRepository.save(user);
    }
}