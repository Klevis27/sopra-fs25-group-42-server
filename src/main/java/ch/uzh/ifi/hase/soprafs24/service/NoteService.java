package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultPostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NoteService {
    // private static final Logger log = LoggerFactory.getLogger(UserService.class); // Maybe useful later
    private final VaultRepository vaultRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Autowired
    public NoteService(UserRepository userRepository, JwtUtil jwtUtil, VaultRepository vaultRepository) {
        this.jwtUtil = jwtUtil;
        this.vaultRepository = vaultRepository;
        this.userRepository = userRepository;
    }

}
