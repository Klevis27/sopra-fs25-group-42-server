package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;

@Entity
@Table(name = "NOTE_PERMISSIONS")
public class NotePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "note_id", nullable = false)
    private Long noteId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "note_id", insertable = false, updatable = false)
    private Note note;

    // Constructors
    public NotePermission() {}

    public NotePermission(Long noteId, Long userId, String role) {
        this.noteId = noteId;
        this.userId = userId;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() { return id; }

    public Long getNoteId() { return noteId; }
    public void setNoteId(Long noteId) { this.noteId = noteId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Note getNote() { return note; }
    public void setNote(Note note) { this.note = note; }
}
