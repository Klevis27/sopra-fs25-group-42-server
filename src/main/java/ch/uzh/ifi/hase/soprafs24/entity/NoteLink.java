package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "NOTE_LINK")
public class NoteLink implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vault_id", nullable = false)
    private Vault vault;

    @ManyToOne
    @JoinColumn(name = "source_note_id", nullable = false)
    private Note sourceNote;

    @ManyToOne
    @JoinColumn(name = "target_note_id", nullable = false)
    private Note targetNote;

    @Column(nullable = false)
    private String linkType; // Could be "reference", "mention", etc.

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vault getVault() { return vault; }
    public void setVault(Vault vault) { this.vault = vault; }

    public Note getSourceNote() { return sourceNote; }
    public void setSourceNote(Note sourceNote) { this.sourceNote = sourceNote; }

    public Note getTargetNote() { return targetNote; }
    public void setTargetNote(Note targetNote) { this.targetNote = targetNote; }

    public String getLinkType() { return linkType; }
    public void setLinkType(String linkType) { this.linkType = linkType; }
}
