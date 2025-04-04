package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VaultService {
    // private static final Logger log = LoggerFactory.getLogger(UserService.class); // Maybe useful later
    private final VaultRepository vaultRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Autowired
    public VaultService(UserRepository userRepository, JwtUtil jwtUtil, VaultRepository vaultRepository) {
        this.jwtUtil = jwtUtil;
        this.vaultRepository = vaultRepository;
        this.userRepository = userRepository;
    }

    public Vault createVault(String userId, VaultPostDTO vaultPostDTO){
        // Check if the name already exists in the database
        if (vaultRepository.findVaultByName(vaultPostDTO.getName()) != null){
            return null;
        }

        // Set params
        Vault newVault = new Vault();
        newVault.setName(vaultPostDTO.getName());
        newVault.setOwner(userRepository.findUserById(Long.valueOf(userId)));
        newVault.setCreatedAt(LocalDateTime.now());

        // TODO save user permission in database

        // Return
        return vaultRepository.save(newVault);
    }
}
