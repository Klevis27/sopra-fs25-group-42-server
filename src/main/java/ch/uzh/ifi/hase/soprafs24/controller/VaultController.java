package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultsGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.VaultService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class VaultController {
    private final VaultService vaultService;
    private final JwtUtil jwtUtil;
    private final VaultRepository vaultRepository;
    private final UserRepository userRepository;

    public VaultController(VaultService vaultService,
                           JwtUtil jwtUtil,
                           VaultRepository vaultRepository,
                           UserRepository userRepository) {
        this.vaultService = vaultService;
        this.jwtUtil = jwtUtil;
        this.vaultRepository = vaultRepository;
        this.userRepository = userRepository;
    }

    // Create vault
    @PostMapping("/vaults")
    public ResponseEntity<Map<String, Object>> register(@RequestBody VaultPostDTO vaultPostDTO, HttpServletRequest request) {
        // Authentication
        String token = extractTokenFromRequest(request);
        String userId = jwtUtil.extractId(token);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Create vault
        Vault vault = vaultService.createVault(userId, vaultPostDTO);

        // Vault creation successful?
        if (vault == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("Error", "Creation of vault failed because vault name was already taken"));
        }

        // Map and return
        VaultPostDTO newVaultPostDTO = DTOMapper.INSTANCE.convertEntityToVaultPostDTO(vault);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Registration successful", "id", newVaultPostDTO.getId().toString()));
    }

    @GetMapping("/vaults")
    public ResponseEntity<List<VaultsGetDTO>> profile(HttpServletRequest request) {
        // Authentication
        String token = extractTokenFromRequest(request);
        String userId = jwtUtil.extractId(token);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Fetch vaults of user, map and return
        List<Vault> vaults = vaultRepository.findVaultByOwner(userRepository.findUserById(Long.valueOf(userId)));

        // TODO Show all vaults user actually has access to via permissions table

        List<VaultsGetDTO> vaultsGetDTOs = new ArrayList<>();

        for (Vault vault : vaults) {
            vaultsGetDTOs.add(DTOMapper.INSTANCE.convertEntityToVaultsGetDTO(vault));
        }

        return ResponseEntity.ok(vaultsGetDTOs);
    }

    // GET vault
    @GetMapping("/vaults/{vaultId}")
    public ResponseEntity<VaultPostDTO> getVault(@PathVariable Long vaultId, HttpServletRequest request) {
        // Authentication
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Get vault, map and return
        Vault vault = vaultService.getVaultById(vaultId);
        VaultPostDTO dto = DTOMapper.INSTANCE.convertEntityToVaultPostDTO(vault);
        return ResponseEntity.ok(dto);
    }

    // GET /vaults/{vaultId}/name
    @GetMapping("/vaults/{vaultId}/name")
    public ResponseEntity<Map<String, String>> getVaultName(@PathVariable("vaultId") Long vaultId, HttpServletRequest request) {
        // Authentication
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Fetch vault name
        String vaultName = vaultRepository.findVaultById(vaultId).getName();

        // Return as JSON object
        Map<String, String> response = new HashMap<>();
        response.put("name", vaultName);
        return ResponseEntity.ok(response);
    }

    // PUT /vaults/{vaultId}
    @PutMapping("/vaults/{vaultId}")
    public ResponseEntity<Void> updateVault(@PathVariable Long vaultId,
                                            @RequestBody VaultPostDTO vaultPostDTO,
                                            HttpServletRequest request) {
        // Authentication
        String token = extractTokenFromRequest(request);
        String userId = jwtUtil.extractId(token);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Update vault and return
        vaultService.updateVault(vaultId, userId, vaultPostDTO);
        return ResponseEntity.ok().build();
    }

    // DELETE vault
    @DeleteMapping("/vaults/{vaultId}/settings/delete")
    public ResponseEntity<Void> deleteVault(@PathVariable Long vaultId, HttpServletRequest request) {
        // Authentication
        String token = extractTokenFromRequest(request);
        String userId = jwtUtil.extractId(token);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Delete vault and return
        vaultService.deleteVault(vaultId, userId);
        return ResponseEntity.ok().build();
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
