package ch.uzh.ifi.hase.soprafs24.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "NOTE")
public class Note implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "vault_id", nullable = false)
    @JsonIgnoreProperties("notes") // Vault içindeki notes alanında sonsuz döngü engeller
    private Vault vault;

    // GETTER - SETTER
    public Long getId() {
        return id;
    }
    public void setId(Long id) { this.id = id; }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) { this.title = title; }

    public Vault getVault() {
        return vault;
    }
    public void setVault(Vault vault) { this.vault = vault; }
}