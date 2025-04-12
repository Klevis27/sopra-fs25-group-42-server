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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class VaultController {

    private final VaultService vaultService;
    private final JwtUtil jwtUtil;
    private final VaultRepository vaultRepository;
    private final UserRepository userRepository;

    public VaultController(VaultService vaultService, JwtUtil jwtUtil,
                           VaultRepository vaultRepository, UserRepository userRepository) {
        this.vaultService = vaultService;
        this.jwtUtil = jwtUtil;
        this.vaultRepository = vaultRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/vaults")
    public ResponseEntity<Map<String, Object>> register(@RequestBody VaultPostDTO vaultPostDTO, HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String userId = jwtUtil.extractId(token);
        Vault vault = vaultService.createVault(userId, vaultPostDTO);

        if (vault == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("Error", "Creation of vault failed because vault name was already taken"));
        }

        VaultPostDTO newVaultPostDTO = DTOMapper.INSTANCE.convertEntityToVaultPostDTO(vault);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Registration successful", "id", newVaultPostDTO.getId().toString()));
    }

    @GetMapping("/vaults")
    public ResponseEntity<List<VaultsGetDTO>> profile(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String userId = jwtUtil.extractId(token);

        // tüm yetkili vaultlar
        List<Vault> vaults = vaultService.getVaultsForUser(userId);
        List<VaultsGetDTO> vaultsGetDTOs = new ArrayList<>();

        for (Vault vault : vaults) {
            vaultsGetDTOs.add(DTOMapper.INSTANCE.convertEntityToVaultsGetDTO(vault));
        }

        return ResponseEntity.ok(vaultsGetDTOs);
    }

    @GetMapping("/vaults/{vaultId}")
    public ResponseEntity<VaultPostDTO> getVault(@PathVariable Long vaultId, HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        Vault vault = vaultService.getVaultById(vaultId);
        VaultPostDTO dto = DTOMapper.INSTANCE.convertEntityToVaultPostDTO(vault);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/vaults/{vaultId}")
    public ResponseEntity<Void> updateVault(@PathVariable Long vaultId,
                                            @RequestBody VaultPostDTO vaultPostDTO,
                                            HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = jwtUtil.extractId(token);
        vaultService.updateVault(vaultId, userId, vaultPostDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/vaults/{vaultId}/settings/delete")
    public ResponseEntity<Void> deleteVault(@PathVariable Long vaultId, HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractId(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = jwtUtil.extractId(token);
        vaultService.deleteVault(vaultId, userId);
        return ResponseEntity.ok().build();
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}