package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class VaultPermissionDTO {

    private Long userId;
    private String username; 
    private String role;     

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}