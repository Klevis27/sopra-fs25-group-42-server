package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

@Entity
@Table(name = "NOTE_STATES")
public class NoteState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    @OneToOne
    private BigInteger noteId;

    @Lob
    @Column(nullable = false)
    private byte[] yjsState;

    public BigInteger getId() { return id; }
    public void setId(BigInteger id) { this.id = id; }

    public BigInteger getNoteId() { return noteId; }
    public void setNote(BigInteger noteId) { this.noteId = noteId; }

    public byte[] getYjsState() { return yjsState; }
    public void setYjsState(byte[] yjsState) { this.yjsState = yjsState; }
}
