package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class NoteStatePutDTO {
    private Long noteId;
    private byte[] content;

    public Long getNoteId() {
        return noteId;
    }
    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public byte[] getContent() {
        return content;
    }
    public void setContent(byte[] content) {
        this.content = content;
    }
}