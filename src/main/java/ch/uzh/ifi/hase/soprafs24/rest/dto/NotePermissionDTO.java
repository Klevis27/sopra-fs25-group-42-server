package ch.uzh.ifi.hase.soprafs24.rest.dto;


public class NotePermissionDTO {
    private String username;
    private String role;

    public NotePermissionDTO(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}

