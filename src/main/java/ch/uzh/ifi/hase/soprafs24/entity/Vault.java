package ch.uzh.ifi.hase.soprafs24.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "VAULT")
public class Vault implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "vault", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("vault")
    private List<Note> notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "vault", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VaultPermission> permissions;

    // ✅ NEW: state column
    @Column(nullable = false)
    private String state;

    // --- GETTERS & SETTERS ---

    public Long getId() {
        return id;
    }
    public void setId(Long id) { this.id = id; }

    public String getName() {
        return name;
    }
    public void setName(String name) { this.name = name; }

    public User getOwner() {
        return owner;
    }
    public void setOwner(User owner) { this.owner = owner; }

    public List<Note> getNotes() {
        return notes;
    }
    public void setNotes(List<Note> notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<VaultPermission> getPermissions() { return permissions; }
    public void setPermissions(List<VaultPermission> permissions) { this.permissions = permissions; }

    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
}