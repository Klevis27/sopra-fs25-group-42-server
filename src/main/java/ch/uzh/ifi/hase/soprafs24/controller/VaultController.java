package ch.uzh.ifi.hase.soprafs24.controller;


import ch.uzh.ifi.hase.soprafs24.rest.dto.VaultDTO;

import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.jwt.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.VaultRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
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
    private static final String VAULT_NOT_FOUND_MESSAGE = "Vault not found";

    public VaultController(VaultService vaultService, JwtUtil jwtUtil, VaultRepository vaultRepository, UserRepository userRepository) {
        this.vaultService = vaultService;
        this.jwtUtil = jwtUtil;
        this.vaultRepository = vaultRepository;
        this.userRepository = userRepository;
    }
/*
    private ResponseEntity<Map<String, String>> createErrorResponse(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(Map.of("error", message));
    }
*/



    // Create Vault
    @PostMapping("/vaults")
    public ResponseEntity<Map<String, Object>> register(@RequestBody VaultPostDTO vaultPostDTO, HttpServletRequest request) {
        // Extract token from the Authorization header
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String userId = jwtUtil.extractId(token);

        Vault vault = vaultService.createVault(userId, vaultPostDTO);

        // Vault creation successful?
        if (vault == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("Error", "Creation of vault failed because vault name was already taken"));
        }

        // Map correctly
        VaultPostDTO newVaultPostDTO = DTOMapper.INSTANCE.convertEntityToVaultPostDTO(vault);

        // Return success!
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Registration successful", "id", newVaultPostDTO.getId().toString()));
    }

    // Get all vaults ✅
    @GetMapping("/vaults")
    public ResponseEntity<List<VaultDTO>> vaults(HttpServletRequest request) {
        // Extract token and validate
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    
        // Extract userId and fetch vaults with role
        String userId = jwtUtil.extractId(token);
        List<VaultDTO> vaults = vaultService.getVaultsForUser(userId);
        return ResponseEntity.ok(vaults);
    }
    
    // Get a specific vault
    @GetMapping("/vaults/{vault_id}")
    public ResponseEntity<VaultsGetDTO> vault(HttpServletRequest request, @PathVariable String vault_id) {
        // Extract token from the Authorization header
        String token = extractTokenFromRequest(request);
        //String userId = jwtUtil.extractId(token);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Fetch vaults of user, map and return
        Vault vault = vaultRepository.findVaultById(Long.valueOf(vault_id));

        // TODO Check if user has access via permissions table
        // TODO Null Pointer gard
        // Fetch vault

       // Check if vault exists
/*
        if (vault == null) {
            return createErrorResponse(HttpStatus.NOT_FOUND, "VAULT_NOT_FOUND_MESSAGE");
        }

*/

        // Map and return
        VaultsGetDTO vaultsGetDTO = DTOMapper.INSTANCE.convertEntityToVaultsGetDTO(vault);
        return ResponseEntity.ok(vaultsGetDTO);
    }

    // Get Vault name
    @GetMapping("/vaults/{vault_id}/name")
    public ResponseEntity<Map<String, String>> vaultName(@PathVariable("vault_id") Long vaultId, HttpServletRequest request) {
        // Extract token from the Authorization header
        String token = extractTokenFromRequest(request);
        //String userId = jwtUtil.extractId(token);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Fetch vault
        Vault vault = vaultRepository.findVaultById(vaultId);

        // Check if vault exists
        if (vault == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", VAULT_NOT_FOUND_MESSAGE);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        // TODO Show all vaults user actually has access to via permissions table

        // Return as a JSON object
        Map<String, String> response = new HashMap<>();
        response.put("name", vault.getName());
        return ResponseEntity.ok(response);
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