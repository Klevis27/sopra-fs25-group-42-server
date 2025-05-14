package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.entity.Vault;

public class VaultDTO {
    public Long id;
    public String name;
    public String role; // ✅ required for frontend logic


    // This DTO needs to be refactored
    public static VaultDTO fromEntity(Vault vault) {
        VaultDTO dto = new VaultDTO();
        dto.id = vault.getId();
        dto.name = vault.getName();
        return dto;
    }

    public static VaultDTO fromEntityWithRole(Vault vault, String role) {
        VaultDTO dto = fromEntity(vault);
        dto.role = role;
        return dto;
    }
}
